package com.itau.invest.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriedades de configuracao do JWT (prefixo {@code app.security.jwt}).
 */
@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(
        String secret,
        long expirationMs,
        String issuer
) {
}
