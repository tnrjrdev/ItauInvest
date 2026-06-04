package com.itau.invest.service;

import com.itau.invest.domain.entity.Investment;
import com.itau.invest.domain.entity.Product;
import com.itau.invest.domain.entity.User;
import com.itau.invest.domain.entity.Wallet;
import com.itau.invest.domain.enums.InvestmentStatus;
import com.itau.invest.domain.enums.ProductType;
import com.itau.invest.domain.enums.RiskLevel;
import com.itau.invest.dto.investment.ApplicationRequest;
import com.itau.invest.dto.investment.PortfolioResponse;
import com.itau.invest.dto.investment.RedemptionRequest;
import com.itau.invest.exception.BusinessException;
import com.itau.invest.exception.InsufficientBalanceException;
import com.itau.invest.exception.ResourceNotFoundException;
import com.itau.invest.repository.InvestmentRepository;
import com.itau.invest.repository.ProductRepository;
import com.itau.invest.repository.TransactionRepository;
import com.itau.invest.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvestmentServiceTest {

    @Mock private InvestmentRepository investmentRepository;
    @Mock private ProductRepository productRepository;
    @Mock private WalletRepository walletRepository;
    @Mock private TransactionRepository transactionRepository;

    private InvestmentService service;
    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        service = new InvestmentService(investmentRepository, productRepository,
                walletRepository, transactionRepository, new ValuationService());
        userId = UUID.randomUUID();
        user = User.builder().name("Cliente").build();
        user.setId(userId);
    }

    private Product product(BigDecimal min, int liquidityDays, BigDecimal rate) {
        Product p = Product.builder().name("CDB Teste").type(ProductType.CDB)
                .riskLevel(RiskLevel.BAIXO).annualRatePercent(rate)
                .minimumAmount(min).liquidityDays(liquidityDays).active(true).build();
        p.setId(UUID.randomUUID());
        return p;
    }

    private Wallet wallet(BigDecimal balance) {
        Wallet w = Wallet.builder().user(user).cashBalance(balance).build();
        w.setId(UUID.randomUUID());
        return w;
    }

    @Test
    void shouldApplyAndDebitWallet() {
        Product product = product(new BigDecimal("100"), 0, new BigDecimal("10"));
        Wallet wallet = wallet(new BigDecimal("1000.0000"));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(walletRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.of(wallet));
        when(investmentRepository.save(any(Investment.class))).thenAnswer(inv -> {
            Investment i = inv.getArgument(0);
            i.setId(UUID.randomUUID());
            return i;
        });

        Investment result = service.apply(userId,
                new ApplicationRequest(product.getId(), new BigDecimal("500.0000")));

        assertThat(wallet.getCashBalance()).isEqualByComparingTo("500.0000");
        assertThat(result.getInvestedAmount()).isEqualByComparingTo("500.0000");
        assertThat(result.getStatus()).isEqualTo(InvestmentStatus.ACTIVE);
        verify(transactionRepository).save(any());
    }

    @Test
    void shouldRejectApplyBelowMinimum() {
        Product product = product(new BigDecimal("1000"), 0, new BigDecimal("10"));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> service.apply(userId,
                new ApplicationRequest(product.getId(), new BigDecimal("100"))))
                .isInstanceOf(BusinessException.class);

        verify(investmentRepository, never()).save(any());
    }

    @Test
    void shouldRejectApplyWhenInsufficientBalance() {
        Product product = product(new BigDecimal("100"), 0, new BigDecimal("10"));
        Wallet wallet = wallet(new BigDecimal("50.0000"));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(walletRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> service.apply(userId,
                new ApplicationRequest(product.getId(), new BigDecimal("100"))))
                .isInstanceOf(InsufficientBalanceException.class);
    }

    @Test
    void shouldRejectApplyInactiveProduct() {
        Product product = product(new BigDecimal("100"), 0, new BigDecimal("10"));
        product.setActive(false);
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> service.apply(userId,
                new ApplicationRequest(product.getId(), new BigDecimal("500"))))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldRedeemFullyAndCreditWallet() {
        Product product = product(new BigDecimal("100"), 0, new BigDecimal("10"));
        Investment inv = Investment.builder().user(user).product(product)
                .investedAmount(new BigDecimal("1000.0000")).status(InvestmentStatus.ACTIVE)
                .appliedAt(Instant.now().minus(365, ChronoUnit.DAYS)).build();
        inv.setId(UUID.randomUUID());
        Wallet wallet = wallet(new BigDecimal("0.0000"));

        when(investmentRepository.findByIdAndUserId(inv.getId(), userId))
                .thenReturn(Optional.of(inv));
        when(walletRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.of(wallet));

        BigDecimal gross = new ValuationService().currentGrossValue(inv, Instant.now());
        Investment result = service.redeem(userId, inv.getId(), new RedemptionRequest(gross));

        assertThat(result.getStatus()).isEqualTo(InvestmentStatus.REDEEMED);
        assertThat(wallet.getCashBalance()).isEqualByComparingTo(gross);
        verify(transactionRepository).save(any());
    }

    @Test
    void shouldRejectRedeemExceedingGross() {
        Product product = product(new BigDecimal("100"), 0, new BigDecimal("10"));
        Investment inv = Investment.builder().user(user).product(product)
                .investedAmount(new BigDecimal("1000.0000")).status(InvestmentStatus.ACTIVE)
                .appliedAt(Instant.now()).build();
        inv.setId(UUID.randomUUID());
        when(investmentRepository.findByIdAndUserId(inv.getId(), userId))
                .thenReturn(Optional.of(inv));

        assertThatThrownBy(() -> service.redeem(userId, inv.getId(),
                new RedemptionRequest(new BigDecimal("5000.0000"))))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldRejectRedeemBeforeLiquidity() {
        Product product = product(new BigDecimal("100"), 90, new BigDecimal("10"));
        Investment inv = Investment.builder().user(user).product(product)
                .investedAmount(new BigDecimal("1000.0000")).status(InvestmentStatus.ACTIVE)
                .appliedAt(Instant.now()).build();
        inv.setId(UUID.randomUUID());
        when(investmentRepository.findByIdAndUserId(inv.getId(), userId))
                .thenReturn(Optional.of(inv));

        assertThatThrownBy(() -> service.redeem(userId, inv.getId(),
                new RedemptionRequest(new BigDecimal("100.0000"))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("liquidez");
    }

    @Test
    void shouldRejectRedeemWhenNotFound() {
        UUID invId = UUID.randomUUID();
        when(investmentRepository.findByIdAndUserId(invId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.redeem(userId, invId,
                new RedemptionRequest(new BigDecimal("10"))))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldBuildConsolidatedPortfolio() {
        Product product = product(new BigDecimal("100"), 0, new BigDecimal("10"));
        Investment inv = Investment.builder().user(user).product(product)
                .investedAmount(new BigDecimal("1000.0000")).status(InvestmentStatus.ACTIVE)
                .appliedAt(Instant.now().minus(365, ChronoUnit.DAYS)).build();
        inv.setId(UUID.randomUUID());
        Wallet wallet = wallet(new BigDecimal("2000.0000"));

        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
        when(investmentRepository.findByUserIdAndStatus(userId, InvestmentStatus.ACTIVE))
                .thenReturn(List.of(inv));

        PortfolioResponse portfolio = service.getPortfolio(userId);

        assertThat(portfolio.cashBalance()).isEqualByComparingTo("2000.0000");
        assertThat(portfolio.totalInvested()).isEqualByComparingTo("1000.0000");
        assertThat(portfolio.totalGrossBalance()).isCloseTo(new BigDecimal("1100.0000"),
                org.assertj.core.data.Offset.offset(new BigDecimal("0.5")));
        assertThat(portfolio.positions()).hasSize(1);
    }
}
