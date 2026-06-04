package com.itau.invest.dto.investment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Requisicao de resgate (parcial ou total) de um investimento")
public record RedemptionRequest(

        @Schema(example = "250.00", description = "Valor bruto a resgatar")
        @NotNull(message = "O valor e obrigatorio")
        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
        @Digits(integer = 15, fraction = 4)
        BigDecimal amount
) {
}
