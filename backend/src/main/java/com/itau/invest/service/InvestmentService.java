package com.itau.invest.service;

import com.itau.invest.domain.entity.Investment;
import com.itau.invest.domain.entity.Product;
import com.itau.invest.domain.entity.Transaction;
import com.itau.invest.domain.entity.Wallet;
import com.itau.invest.domain.enums.InvestmentStatus;
import com.itau.invest.domain.enums.TransactionType;
import com.itau.invest.dto.investment.ApplicationRequest;
import com.itau.invest.dto.investment.InvestmentPositionResponse;
import com.itau.invest.dto.investment.PortfolioResponse;
import com.itau.invest.dto.investment.RedemptionRequest;
import com.itau.invest.exception.BusinessException;
import com.itau.invest.exception.InsufficientBalanceException;
import com.itau.invest.exception.ResourceNotFoundException;
import com.itau.invest.repository.InvestmentRepository;
import com.itau.invest.repository.ProductRepository;
import com.itau.invest.repository.TransactionRepository;
import com.itau.invest.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Regras de negocio de investimentos: aplicacao, resgate e consolidacao da
 * carteira. Cada lancamento financeiro e atomico ({@link Transactional}) e
 * registrado no ledger.
 */
@Service
public class InvestmentService {

    private static final Logger log = LoggerFactory.getLogger(InvestmentService.class);
    private static final MathContext MC = new MathContext(20, RoundingMode.HALF_EVEN);
    private static final int MONEY_SCALE = 4;

    private final InvestmentRepository investmentRepository;
    private final ProductRepository productRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final ValuationService valuationService;

    public InvestmentService(InvestmentRepository investmentRepository,
                             ProductRepository productRepository,
                             WalletRepository walletRepository,
                             TransactionRepository transactionRepository,
                             ValuationService valuationService) {
        this.investmentRepository = investmentRepository;
        this.productRepository = productRepository;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.valuationService = valuationService;
    }

    /**
     * Aplica recursos da carteira em um produto, criando uma nova posicao (lote)
     * com data-base propria para valorizacao.
     */
    @Transactional
    public Investment apply(UUID userId, ApplicationRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> ResourceNotFoundException.of("Produto", request.productId()));

        if (!product.isActive()) {
            throw new BusinessException("Produto indisponivel para aplicacao");
        }
        if (request.amount().compareTo(product.getMinimumAmount()) < 0) {
            throw new BusinessException(
                    "Valor abaixo do minimo de aplicacao (%s)".formatted(product.getMinimumAmount()));
        }

        Wallet wallet = walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Carteira do usuario", userId));
        if (!wallet.hasSufficientBalance(request.amount())) {
            throw new InsufficientBalanceException(
                    "Saldo em conta insuficiente para a aplicacao");
        }

        wallet.debit(request.amount());

        Investment investment = Investment.builder()
                .user(wallet.getUser())
                .product(product)
                .investedAmount(request.amount())
                .status(InvestmentStatus.ACTIVE)
                .appliedAt(Instant.now())
                .build();
        investment = investmentRepository.save(investment);

        registerLedger(wallet, investment, TransactionType.APPLICATION, request.amount(),
                "Aplicacao em " + product.getName());

        log.info("Aplicacao realizada: userId={} productId={} amount={} investmentId={}",
                userId, product.getId(), request.amount(), investment.getId());
        return investment;
    }

    /**
     * Resgata (parcial ou total) o valor bruto de uma posicao, respeitando o
     * prazo de liquidez do produto.
     */
    @Transactional
    public Investment redeem(UUID userId, UUID investmentId, RedemptionRequest request) {
        Investment investment = investmentRepository.findByIdAndUserId(investmentId, userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Investimento", investmentId));

        if (investment.getStatus() != InvestmentStatus.ACTIVE) {
            throw new BusinessException("Investimento ja resgatado");
        }

        Instant now = Instant.now();
        long elapsedDays = ChronoUnit.DAYS.between(investment.getAppliedAt(), now);
        if (elapsedDays < investment.getProduct().getLiquidityDays()) {
            throw new BusinessException(
                    "Resgate indisponivel antes do prazo de liquidez (D+%d)"
                            .formatted(investment.getProduct().getLiquidityDays()));
        }

        BigDecimal grossValue = valuationService.currentGrossValue(investment, now);
        if (request.amount().compareTo(grossValue) > 0) {
            throw new BusinessException(
                    "Valor solicitado excede o saldo bruto disponivel (%s)".formatted(grossValue));
        }

        // Reduz o principal proporcionalmente ao valor bruto resgatado.
        BigDecimal proportion = request.amount().divide(grossValue, MC);
        BigDecimal principalRedeemed = investment.getInvestedAmount()
                .multiply(proportion, MC).setScale(MONEY_SCALE, RoundingMode.HALF_EVEN);
        BigDecimal remainingPrincipal = investment.getInvestedAmount().subtract(principalRedeemed);

        boolean fullRedemption = grossValue.subtract(request.amount())
                .compareTo(BigDecimal.valueOf(0.0001)) <= 0;

        if (fullRedemption) {
            investment.setInvestedAmount(BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_EVEN));
            investment.setStatus(InvestmentStatus.REDEEMED);
        } else {
            investment.setInvestedAmount(remainingPrincipal);
        }

        Wallet wallet = walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Carteira do usuario", userId));
        wallet.credit(request.amount());

        registerLedger(wallet, investment, TransactionType.REDEMPTION, request.amount(),
                "Resgate de " + investment.getProduct().getName());

        log.info("Resgate realizado: userId={} investmentId={} amount={} full={}",
                userId, investmentId, request.amount(), fullRedemption);
        return investment;
    }

    /** Aplica e retorna ja a posicao valorizada criada. */
    @Transactional
    public InvestmentPositionResponse applyAndGetPosition(UUID userId, ApplicationRequest request) {
        Investment investment = apply(userId, request);
        return toPosition(investment, Instant.now());
    }

    @Transactional(readOnly = true)
    public List<InvestmentPositionResponse> listPositions(UUID userId) {
        Instant now = Instant.now();
        return investmentRepository.findByUserIdAndStatus(userId, InvestmentStatus.ACTIVE)
                .stream()
                .map(inv -> toPosition(inv, now))
                .toList();
    }

    @Transactional(readOnly = true)
    public PortfolioResponse getPortfolio(UUID userId) {
        Instant now = Instant.now();
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Carteira do usuario", userId));

        List<InvestmentPositionResponse> positions =
                investmentRepository.findByUserIdAndStatus(userId, InvestmentStatus.ACTIVE)
                        .stream()
                        .map(inv -> toPosition(inv, now))
                        .toList();

        BigDecimal totalInvested = positions.stream()
                .map(InvestmentPositionResponse::investedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalGross = positions.stream()
                .map(InvestmentPositionResponse::grossBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalYield = totalGross.subtract(totalInvested);
        BigDecimal totalEquity = wallet.getCashBalance().add(totalGross);

        return new PortfolioResponse(
                wallet.getCashBalance(),
                totalInvested,
                totalGross,
                totalYield,
                totalEquity,
                positions);
    }

    private InvestmentPositionResponse toPosition(Investment inv, Instant reference) {
        Product product = inv.getProduct();
        BigDecimal gross = valuationService.currentGrossValue(inv, reference);
        BigDecimal yield = gross.subtract(inv.getInvestedAmount());
        BigDecimal yieldPercent = inv.getInvestedAmount().compareTo(BigDecimal.ZERO) > 0
                ? yield.divide(inv.getInvestedAmount(), MC)
                    .multiply(BigDecimal.valueOf(100)).setScale(MONEY_SCALE, RoundingMode.HALF_EVEN)
                : BigDecimal.ZERO;

        return new InvestmentPositionResponse(
                inv.getId(),
                product.getId(),
                product.getName(),
                product.getType(),
                product.getRiskLevel(),
                product.getAnnualRatePercent(),
                inv.getInvestedAmount(),
                gross,
                yield,
                yieldPercent,
                inv.getStatus(),
                inv.getAppliedAt());
    }

    private void registerLedger(Wallet wallet, Investment investment, TransactionType type,
                                BigDecimal amount, String description) {
        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .investment(investment)
                .type(type)
                .amount(amount)
                .balanceAfter(wallet.getCashBalance())
                .description(description)
                .build();
        transactionRepository.save(transaction);
    }
}
