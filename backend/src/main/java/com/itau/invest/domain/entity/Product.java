package com.itau.invest.domain.entity;

import com.itau.invest.domain.enums.ProductType;
import com.itau.invest.domain.enums.RiskLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Produto de investimento do catalogo.
 *
 * <p>{@code annualRatePercent} representa a rentabilidade nominal anual (% a.a.)
 * usada para a valorizacao simulada das posicoes.</p>
 */
@Entity
@Table(name = "tb_product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Column(name = "name", nullable = false, length = 140)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private ProductType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 10)
    private RiskLevel riskLevel;

    /** Rentabilidade nominal anual em pontos percentuais (ex.: 12.50 = 12,5% a.a.). */
    @Column(name = "annual_rate_percent", nullable = false, precision = 9, scale = 4)
    private BigDecimal annualRatePercent;

    /** Valor minimo de aplicacao. */
    @Column(name = "minimum_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal minimumAmount;

    /** Prazo de liquidez para resgate, em dias. 0 = liquidez diaria (D+0). */
    @Column(name = "liquidity_days", nullable = false)
    @Builder.Default
    private Integer liquidityDays = 0;

    /** Data de vencimento (nulo para produtos sem vencimento, como acoes). */
    @Column(name = "maturity_date")
    private LocalDate maturityDate;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
