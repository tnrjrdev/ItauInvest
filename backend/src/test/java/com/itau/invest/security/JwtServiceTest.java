package com.itau.invest.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties(
                "dGVzdFNlY3JldEtleUZvckl0YXVJbnZlc3RKVW5pdFRlc3RzMjAyNFNlY3VyZUtleQ==",
                3600000L, "itau-invest-test");
        jwtService = new JwtService(props);
    }

    @Test
    void shouldGenerateAndValidateToken() {
        String token = jwtService.generateToken(userId, "user@itau.com.br", "CLIENT");

        assertThat(jwtService.isValid(token)).isTrue();
        assertThat(jwtService.extractUsername(token)).isEqualTo("user@itau.com.br");
        assertThat(jwtService.extractUserId(token)).isEqualTo(userId);
    }

    @Test
    void shouldRejectTamperedToken() {
        String token = jwtService.generateToken(userId, "user@itau.com.br", "CLIENT");
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThat(jwtService.isValid(tampered)).isFalse();
    }

    @Test
    void shouldRejectGarbageToken() {
        assertThat(jwtService.isValid("not-a-jwt")).isFalse();
    }

    @Test
    void shouldExposeExpirationInSeconds() {
        assertThat(jwtService.getExpirationSeconds()).isEqualTo(3600L);
    }
}
