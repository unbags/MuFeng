package com.example.ordering.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;

@Component
public class JwtTokenUtil {

    private static final String DEFAULT_SECRET = "OrderingSystemJwtSecretKey2024!@#$%^&*()VeryLongAndSecure";

    private final String secret;
    private final long expirationMs;

    public JwtTokenUtil(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.expiration-ms:86400000}") long expirationMs,
        Environment environment
    ) {
        if (isProd(environment) && (DEFAULT_SECRET.equals(secret) || secret.length() < 32)) {
            throw new IllegalStateException("生产环境必须配置安全的令牌密钥");
        }
        this.secret = secret;
        this.expirationMs = expirationMs;
    }

    public String generateToken(String username) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(expiration)
            .signWith(SignatureAlgorithm.HS512, secret.getBytes())
            .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
            .setSigningKey(secret.getBytes())
            .parseClaimsJws(token)
            .getBody();
    }

    private boolean isProd(Environment environment) {
        return Arrays.asList(environment.getActiveProfiles()).contains("prod");
    }
}
