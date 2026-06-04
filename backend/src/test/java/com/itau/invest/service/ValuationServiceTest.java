package com.itau.invest.service;

import com.itau.invest.domain.entity.Investment;
import com.itau.invest.domain.entity.Product;
import com.itau.invest.domain.enums.ProductType;
import com.itau.invest.domain.enums.RiskLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class ValuationServiceTest {

    private final ValuationService service = new ValuationService();

    private Product product(BigDecimal rate) {
        return Product.builder()
                .name("CDB Teste")
                .type(ProductType.CDB)
                .riskLevel(RiskLevel.BAIXO)
                .annualRatePercent(rate)
                .minimumAmount(BigDecimal.valueOf(100))
                .liquidityDays(0)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Apos 365 dias a 10% a.a., 1000 deve render para 1100")
    void shouldComputeOneYearCompound() {
        Instant now = Instant.now();
        Instant appliedAt = now.minus(365, ChronoUnit.DAYS);
        Investment inv = Investment.builder()
                .investedAmount(BigDecimal.valueOf(1000))
                .product(product(BigDecimal.valueOf(10)))
                .appliedAt(appliedAt)
                .build();

        BigDecimal gross = service.currentGrossValue(inv, now);

        assertThat(gross).isCloseTo(new BigDecimal("1100.0000"),
                org.assertj.core.data.Offset.offset(new BigDecimal("0.01")));
    }

    @Test
    @DisplayName("No dia da aplicacao (0 dias) o valor bruto e igual ao principal")
    void shouldReturnPrincipalOnDayZero() {
        Instant now = Instant.now();
        Investment inv = Investment.builder()
                .investedAmount(new BigDecimal("500.0000"))
                .product(product(BigDecimal.valueOf(12)))
                .appliedAt(now)
                .build();

        BigDecimal gross = service.currentGrossValue(inv, now);

        assertThat(gross).isEqualByComparingTo("500.0000");
    }

    @Test
    @DisplayName("Rendimento bruto e a diferenca entre valor bruto e principal")
    void shouldComputeGrossYield() {
        Instant now = Instant.now();
        Investment inv = Investment.builder()
                .investedAmount(BigDecimal.valueOf(1000))
                .product(product(BigDecimal.valueOf(10)))
                .appliedAt(now.minus(365, ChronoUnit.DAYS))
                .build();

        BigDecimal yield = service.grossYield(inv, now);

        assertThat(yield).isCloseTo(new BigDecimal("100.0000"),
                org.assertj.core.data.Offset.offset(new BigDecimal("0.01")));
    }
}
