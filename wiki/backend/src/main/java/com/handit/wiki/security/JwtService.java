package com.handit.wiki.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.handit.wiki.config.SecurityProperties;
import com.handit.wiki.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Service
public class JwtService {

    private final SecurityProperties properties;
    private SecretKey signingKey;

    public JwtService(SecurityProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void init() {
        this.signingKey = Keys.hmacShaKeyFor(
                properties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(properties.getJwt().getExpirationHours() * 3600);

        return Jwts.builder()
                .subject(user.getId())
                .claim("username", user.getUsername())
                .claim("name", user.getName())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(signingKey)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
