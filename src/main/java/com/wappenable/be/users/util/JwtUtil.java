package com.wappenable.be.users.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;


import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private final long ACCESS_TOKEN_EXP = 1000 * 60 * 60 * 24 * 7;     // 7일
    private final long REFRESH_TOKEN_EXP = 1000 * 60 * 60 * 24 * 14;   // 14일

    private SecretKey getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(String subject) {
        return generateToken(subject, ACCESS_TOKEN_EXP);
    }

    public String generateRefreshToken(String subject) {
        return generateToken(subject, REFRESH_TOKEN_EXP);
    }

    private String generateToken(String subject, long expirationTimeMs) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(subject)
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
