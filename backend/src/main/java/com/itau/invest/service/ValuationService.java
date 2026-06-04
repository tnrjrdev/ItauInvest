package com.itau.invest.service;

import com.itau.invest.domain.entity.Investment;
import com.itau.invest.domain.entity.Product;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Calcula a valorizacao (juros compostos pro-rata) de uma posicao de
 * investimento.
 *
 * <p><b>Modelo de simulacao:</b> o valor bruto e dado por
 * {@code principal * (1 + taxaAnual)^(dias/365)}, onde {@code dias} e o numero
 * de dias corridos desde a aplicacao. Trata-se de uma simulacao deterministica
 * (nao reflete marcacao a mercado de produtos de renda variavel).</p>
 */
@Service
public class ValuationService {

    private static final MathContext MC = new MathContext(20, RoundingMode.HALF_EVEN);
    private static final BigDecimal DAYS_IN_YEAR = BigDecimal.valueOf(365);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    /** Valor bruto atual da posicao, com escala monetaria (4 casas). */
    public BigDecimal currentGrossValue(Investment investment, Instant reference) {
        return currentGrossValue(
                investment.getInvestedAmount(),
                investment.getProduct(),
                investment.getAppliedAt(),
                reference);
    }

    public BigDecimal currentGrossValue(BigDecimal principal, Product product,
                                        Instant appliedAt, Instant reference) {
        long days = Math.max(0, ChronoUnit.DAYS.between(appliedAt, reference));

        // fator = (1 + taxaAnual/100) ^ (dias/365)
        BigDecimal annualFactor = BigDecimal.ONE.add(
                product.getAnnualRatePercent().divide(HUNDRED, MC));
        double exponent = (double) days / DAYS_IN_YEAR.doubleValue();
        double factor = Math.pow(annualFactor.doubleValue(), exponent);

        return principal.multiply(BigDecimal.valueOf(factor), MC)
                .setScale(4, RoundingMode.HALF_EVEN);
    }

    /** Rendimento bruto acumulado (valor bruto - principal). */
    public BigDecimal grossYield(Investment investment, Instant reference) {
        return currentGrossValue(investment, reference)
                .subtract(investment.getInvestedAmount());
    }
}
