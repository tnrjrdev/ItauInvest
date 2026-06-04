package com.itau.invest.dto.product;

import com.itau.invest.domain.entity.Product;
import com.itau.invest.domain.enums.ProductType;
import com.itau.invest.domain.enums.RiskLevel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        ProductType type,
        RiskLevel riskLevel,
        BigDecimal annualRatePercent,
        BigDecimal minimumAmount,
        Integer liquidityDays,
        LocalDate maturityDate,
        boolean active
) {
    public static ProductResponse from(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getType(),
                p.getRiskLevel(),
                p.getAnnualRatePercent(),
                p.getMinimumAmount(),
                p.getLiquidityDays(),
                p.getMaturityDate(),
                p.isActive()
        );
    }
}
