package com.itau.invest.dto.investment;

import java.math.BigDecimal;
import java.util.List;

/**
 * Visao consolidada da carteira de investimentos do cliente.
 */
public record PortfolioResponse(
        BigDecimal cashBalance,
        BigDecimal totalInvested,
        BigDecimal totalGrossBalance,
        BigDecimal totalYield,
        BigDecimal totalEquity,
        List<InvestmentPositionResponse> positions
) {
}
