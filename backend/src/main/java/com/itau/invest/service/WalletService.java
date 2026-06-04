package com.itau.invest.service;

import com.itau.invest.domain.entity.Transaction;
import com.itau.invest.domain.entity.Wallet;
import com.itau.invest.domain.enums.TransactionType;
import com.itau.invest.dto.wallet.CashOperationRequest;
import com.itau.invest.exception.InsufficientBalanceException;
import com.itau.invest.exception.ResourceNotFoundException;
import com.itau.invest.repository.TransactionRepository;
import com.itau.invest.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Operacoes da carteira de caixa: consulta de saldo, deposito, saque e extrato.
 * Cada movimentacao gera um lancamento imutavel no ledger.
 */
@Service
public class WalletService {

    private static final Logger log = LoggerFactory.getLogger(WalletService.class);

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public WalletService(WalletRepository walletRepository,
                         TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public Wallet getByUserId(UUID userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Carteira do usuario", userId));
    }

    @Transactional
    public Wallet deposit(UUID userId, CashOperationRequest request) {
        Wallet wallet = loadForUpdate(userId);
        wallet.credit(request.amount());

        registerTransaction(wallet, TransactionType.DEPOSIT, request.amount(),
                description(request, "Deposito em conta"));

        log.info("Deposito realizado: userId={} amount={} balance={}",
                userId, request.amount(), wallet.getCashBalance());
        return wallet;
    }

    @Transactional
    public Wallet withdraw(UUID userId, CashOperationRequest request) {
        Wallet wallet = loadForUpdate(userId);
        if (!wallet.hasSufficientBalance(request.amount())) {
            throw new InsufficientBalanceException(
                    "Saldo insuficiente para o saque solicitado");
        }
        wallet.debit(request.amount());

        registerTransaction(wallet, TransactionType.WITHDRAWAL, request.amount(),
                description(request, "Saque em conta"));

        log.info("Saque realizado: userId={} amount={} balance={}",
                userId, request.amount(), wallet.getCashBalance());
        return wallet;
    }

    @Transactional(readOnly = true)
    public Page<Transaction> statement(UUID userId, Pageable pageable) {
        Wallet wallet = getByUserId(userId);
        return transactionRepository.findByWalletId(wallet.getId(), pageable);
    }

    private Wallet loadForUpdate(UUID userId) {
        return walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Carteira do usuario", userId));
    }

    private void registerTransaction(Wallet wallet, TransactionType type,
                                     java.math.BigDecimal amount, String description) {
        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .type(type)
                .amount(amount)
                .balanceAfter(wallet.getCashBalance())
                .description(description)
                .build();
        transactionRepository.save(transaction);
    }

    private String description(CashOperationRequest request, String fallback) {
        return request.description() != null && !request.description().isBlank()
                ? request.description()
                : fallback;
    }
}
