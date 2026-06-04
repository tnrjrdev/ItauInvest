package com.itau.invest.dto.investment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Requisicao de aplicacao em um produto de investimento")
public record ApplicationRequest(

        @NotNull(message = "O produto e obrigatorio")
        UUID productId,

        @Schema(example = "500.00")
        @NotNull(message = "O valor e obrigatorio")
        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
        @Digits(integer = 15, fraction = 4)
        BigDecimal amount
) {
}
