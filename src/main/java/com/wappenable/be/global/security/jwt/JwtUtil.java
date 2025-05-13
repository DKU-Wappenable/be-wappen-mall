package com.wappenable.be.global.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private final long ACCESS_TOKEN_EXP = 1000 * 60 * 60 * 24 * 7;     // 7일
    private final long REFRESH_TOKEN_EXP = 1000 * 60 * 60 * 24 * 14;   // 14일

    private SecretKey getSigningKey() {
        // byte[] keyBytes = Base64.getDecoder().decode(secret);
        // return Keys.hmacShaKeyFor(keyBytes);
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String subject,  String role) {
        return generateToken(subject, role, ACCESS_TOKEN_EXP);
    }

    public String generateRefreshToken(String subject,  String role) {
        return generateToken(subject, role, REFRESH_TOKEN_EXP);
    }

    private String generateToken(String subject, String role, long expirationTimeMs) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(subject)
                .claim("role", "ROLE_" + role) // 역할 정보를 토큰에 포함
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationTimeMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
