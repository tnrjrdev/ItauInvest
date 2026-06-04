package com.itau.invest.dto.investment;

import com.itau.invest.domain.enums.InvestmentStatus;
import com.itau.invest.domain.enums.ProductType;
import com.itau.invest.domain.enums.RiskLevel;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Posicao de investimento com valorizacao calculada no momento da consulta.
 */
public record InvestmentPositionResponse(
        UUID investmentId,
        UUID productId,
        String productName,
        ProductType productType,
        RiskLevel riskLevel,
        BigDecimal annualRatePercent,
        BigDecimal investedAmount,
        BigDecimal grossBalance,
        BigDecimal grossYield,
        BigDecimal yieldPercent,
        InvestmentStatus status,
        Instant appliedAt
) {
}
