package com.atmd.backend.global.auth.signupToken;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SignupTokenProvider {

    private final SignupTokenProperties signupTokenProperties;

    public String generate(String provider, String providerId, String email) {
        Date now = new Date();
        return Jwts.builder()
                .claim("type", "SIGNUP")
                .claim("provider", provider)
                .claim("providerId", providerId)
                .claim("email", email)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + signupTokenProperties.getExpiration()))
                .signWith(getSigningKey())
                .compact();
    }

    public boolean validate(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, String> parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Map.of(
                "provider", claims.get("provider", String.class),
                "providerId", claims.get("providerId", String.class),
                "email", claims.get("email", String.class)
        );
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = signupTokenProperties.getSecretKey().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
