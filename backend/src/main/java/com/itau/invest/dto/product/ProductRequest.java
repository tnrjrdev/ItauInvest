package com.itau.invest.dto.product;

import com.itau.invest.domain.enums.ProductType;
import com.itau.invest.domain.enums.RiskLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Requisicao para criacao/atualizacao de produto (ADMIN)")
public record ProductRequest(

        @Schema(example = "CDB Liquidez Diaria 110% CDI")
        @NotBlank(message = "O nome e obrigatorio")
        @Size(min = 3, max = 140)
        String name,

        @NotNull(message = "O tipo e obrigatorio")
        ProductType type,

        @NotNull(message = "O nivel de risco e obrigatorio")
        RiskLevel riskLevel,

        @Schema(example = "12.5000", description = "Rentabilidade nominal anual em %")
        @NotNull(message = "A rentabilidade anual e obrigatoria")
        @DecimalMin(value = "0.0", inclusive = false, message = "A rentabilidade deve ser positiva")
        @Digits(integer = 5, fraction = 4)
        BigDecimal annualRatePercent,

        @Schema(example = "100.00")
        @NotNull(message = "O valor minimo e obrigatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "O valor minimo deve ser positivo")
        @Digits(integer = 15, fraction = 4)
        BigDecimal minimumAmount,

        @Schema(example = "0", description = "Prazo de liquidez em dias (0 = D+0)")
        @NotNull(message = "A liquidez e obrigatoria")
        @PositiveOrZero(message = "A liquidez nao pode ser negativa")
        Integer liquidityDays,

        @Schema(example = "2027-12-31", description = "Data de vencimento (opcional)")
        LocalDate maturityDate
) {
}
