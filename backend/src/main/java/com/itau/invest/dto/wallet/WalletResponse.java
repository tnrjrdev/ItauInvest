package com.itau.invest.dto.wallet;

import com.itau.invest.domain.entity.Wallet;

import java.math.BigDecimal;
import java.util.UUID;

public record WalletResponse(
        UUID id,
        UUID userId,
        BigDecimal cashBalance
) {
    public static WalletResponse from(Wallet wallet) {
        return new WalletResponse(
                wallet.getId(),
                wallet.getUser().getId(),
                wallet.getCashBalance()
        );
    }
}
