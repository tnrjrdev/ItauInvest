package com.itau.invest.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Servico responsavel por emitir e validar tokens JWT (HS256).
 */
@Service
public class JwtService {

    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secret()));
    }

    /**
     * Gera um access token contendo o e-mail (subject), o id do usuario e o
     * perfil como claims.
     */
    public String generateToken(UUID userId, String email, String role) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(properties.expirationMs());

        return Jwts.builder()
                .issuer(properties.issuer())
                .subject(email)
                .claim("uid", userId.toString())
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }

    /** Extrai o e-mail (subject) do token, validando assinatura e expiracao. */
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(parseClaims(token).get("uid", String.class));
    }

    /** Valida assinatura, emissor e expiracao do token. */
    public boolean isValid(String token) {
        try {
            Claims claims = parseClaims(token);
            return properties.issuer().equals(claims.getIssuer())
                    && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public long getExpirationSeconds() {
        return properties.expirationMs() / 1000;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
