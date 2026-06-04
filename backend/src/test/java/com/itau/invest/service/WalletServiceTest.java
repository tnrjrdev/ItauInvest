package com.itau.invest.service;

import com.itau.invest.domain.entity.User;
import com.itau.invest.domain.entity.Wallet;
import com.itau.invest.dto.wallet.CashOperationRequest;
import com.itau.invest.exception.InsufficientBalanceException;
import com.itau.invest.exception.ResourceNotFoundException;
import com.itau.invest.repository.TransactionRepository;
import com.itau.invest.repository.WalletRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock private WalletRepository walletRepository;
    @Mock private TransactionRepository transactionRepository;

    @InjectMocks private WalletService walletService;

    private Wallet wallet(BigDecimal balance) {
        User user = User.builder().name("Cliente").build();
        user.setId(UUID.randomUUID());
        Wallet w = Wallet.builder().user(user).cashBalance(balance).build();
        w.setId(UUID.randomUUID());
        return w;
    }

    @Test
    void shouldDepositAndRecordTransaction() {
        UUID userId = UUID.randomUUID();
        Wallet wallet = wallet(new BigDecimal("100.0000"));
        when(walletRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.of(wallet));

        Wallet result = walletService.deposit(userId,
                new CashOperationRequest(new BigDecimal("250.0000"), "PIX"));

        assertThat(result.getCashBalance()).isEqualByComparingTo("350.0000");
        verify(transactionRepository).save(any());
    }

    @Test
    void shouldWithdrawWhenBalanceSufficient() {
        UUID userId = UUID.randomUUID();
        Wallet wallet = wallet(new BigDecimal("500.0000"));
        when(walletRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.of(wallet));

        Wallet result = walletService.withdraw(userId,
                new CashOperationRequest(new BigDecimal("200.0000"), null));

        assertThat(result.getCashBalance()).isEqualByComparingTo("300.0000");
        verify(transactionRepository).save(any());
    }

    @Test
    void shouldRejectWithdrawWhenInsufficientBalance() {
        UUID userId = UUID.randomUUID();
        Wallet wallet = wallet(new BigDecimal("100.0000"));
        when(walletRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> walletService.withdraw(userId,
                new CashOperationRequest(new BigDecimal("200.0000"), null)))
                .isInstanceOf(InsufficientBalanceException.class);

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenWalletNotFound() {
        UUID userId = UUID.randomUUID();
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> walletService.getByUserId(userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
