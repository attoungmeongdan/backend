package com.atmd.backend.global.auth.signupToken;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SignupTokenProvider {

    private final SignupTokenProperties signupTokenProperties;

    public String generate(String provider, String providerId, String email) {
        return generate(provider, providerId, email, null);
    }

    public String generate(String provider, String providerId, String email, String inviteCode) {
        Date now = new Date();
        var builder = Jwts.builder()
                .claim("type", "SIGNUP")
                .claim("provider", provider)
                .claim("providerId", providerId)
                .claim("email", email)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + signupTokenProperties.getExpiration()))
                .signWith(getSigningKey());
        if (inviteCode != null && !inviteCode.isBlank()) {
            builder.claim("inviteCode", inviteCode);
        }
        return builder.compact();
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
        Map<String, String> result = new HashMap<>();
        result.put("provider", claims.get("provider", String.class));
        result.put("providerId", claims.get("providerId", String.class));
        result.put("email", claims.get("email", String.class));
        String inviteCode = claims.get("inviteCode", String.class);
        if (inviteCode != null) {
            result.put("inviteCode", inviteCode);
        }
        return result;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = signupTokenProperties.getSecretKey().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
