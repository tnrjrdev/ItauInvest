package com.itau.invest.dto.wallet;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Requisicao de deposito ou saque na carteira")
public record CashOperationRequest(

        @Schema(example = "1000.00")
        @NotNull(message = "O valor e obrigatorio")
        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
        @Digits(integer = 15, fraction = 4)
        BigDecimal amount,

        @Schema(example = "Deposito via PIX")
        String description
) {
}
