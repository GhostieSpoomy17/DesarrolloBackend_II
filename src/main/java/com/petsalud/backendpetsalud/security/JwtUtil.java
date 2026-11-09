package com.petsalud.backendpetsalud.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
@Profile("!semana1")
public class JwtUtil {
    private final SecretKey key;
    private final JwtParser parser;
    private final long minutes;
    private final String issuer;

    public JwtUtil(@Value("${petsalud.jwt.secret}") String secret,
                   @Value("${petsalud.jwt.minutes:15}") long minutes,
                   @Value("${petsalud.jwt.issuer:petsalud}") String issuer) {
        if (minutes < 1 || minutes > 60) {
            throw new IllegalArgumentException("La duración de JWT debe estar entre 1 y 60 minutos");
        }
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.issuer = issuer;
        this.minutes = minutes;
        this.parser = Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .requireAudience(issuer + "-api")
                .sig().clear().add(Jwts.SIG.HS256).and()
                .zip().clear().and()
                .build();
    }

    public Token emitir(UsuarioDetails usuario) {
        Instant now = Instant.now();
        long seconds = minutes * 60;
        String value = Jwts.builder()
                .issuer(issuer)
                .audience().add(issuer + "-api").and()
                .subject(usuario.getUsername())
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(seconds)))
                .claim("uid", usuario.getId())
                .signWith(key, Jwts.SIG.HS256)
                .compact();
        return new Token(value, "Bearer", seconds);
    }

    public Claims validar(String token) {
        Claims claims = parser.parseSignedClaims(token).getPayload();
        if (claims.getSubject() == null || claims.getExpiration() == null
                || claims.get("uid", Long.class) == null) {
            throw new IllegalArgumentException("Token incompleto");
        }
        return claims;
    }

    public record Token(String accessToken, String tokenType, long expiresIn) {}
}
