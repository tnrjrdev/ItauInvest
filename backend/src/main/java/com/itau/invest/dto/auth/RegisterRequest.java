package com.itau.invest.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Dados para registro de um novo cliente. O perfil atribuido e sempre CLIENT.
 */
@Schema(description = "Requisicao de registro de novo cliente")
public record RegisterRequest(

        @Schema(example = "Maria Silva")
        @NotBlank(message = "O nome e obrigatorio")
        @Size(min = 3, max = 120, message = "O nome deve ter entre 3 e 120 caracteres")
        String name,

        @Schema(example = "maria.silva@itau.com.br")
        @NotBlank(message = "O e-mail e obrigatorio")
        @Email(message = "E-mail invalido")
        @Size(max = 180)
        String email,

        @Schema(example = "52998224725", description = "CPF com 11 digitos, somente numeros")
        @NotBlank(message = "O CPF e obrigatorio")
        @Pattern(regexp = "\\d{11}", message = "O CPF deve conter exatamente 11 digitos")
        String cpf,

        @Schema(example = "SenhaForte@123")
        @NotBlank(message = "A senha e obrigatoria")
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
        String password
) {
}
