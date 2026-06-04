package com.itau.invest.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Resposta de autenticacao com token de acesso")
public record AuthResponse(

        @Schema(description = "Token JWT de acesso (Bearer)")
        String accessToken,

        @Schema(example = "Bearer")
        String tokenType,

        @Schema(description = "Tempo de expiracao do token em segundos")
        long expiresIn,

        UUID userId,

        String name,

        String role
) {
    public static AuthResponse bearer(String token, long expiresInSeconds, UUID userId,
                                       String name, String role) {
        return new AuthResponse(token, "Bearer", expiresInSeconds, userId, name, role);
    }
}
