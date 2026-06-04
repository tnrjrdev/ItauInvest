package com.itau.invest.dto.transaction;

import com.itau.invest.domain.entity.Transaction;
import com.itau.invest.domain.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        TransactionType type,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String description,
        UUID investmentId,
        Instant createdAt
) {
    public static TransactionResponse from(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getType(),
                t.getAmount(),
                t.getBalanceAfter(),
                t.getDescription(),
                t.getInvestment() != null ? t.getInvestment().getId() : null,
                t.getCreatedAt()
        );
    }
}
