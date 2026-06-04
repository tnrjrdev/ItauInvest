package com.itau.invest.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Requisicao de autenticacao")
public record LoginRequest(

        @Schema(example = "maria.silva@itau.com.br")
        @NotBlank(message = "O e-mail e obrigatorio")
        @Email(message = "E-mail invalido")
        String email,

        @Schema(example = "SenhaForte@123")
        @NotBlank(message = "A senha e obrigatoria")
        String password
) {
}
