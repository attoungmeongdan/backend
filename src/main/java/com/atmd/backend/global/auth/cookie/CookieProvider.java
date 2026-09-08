package com.atmd.backend.global.auth.cookie;

import com.atmd.backend.global.auth.jwt.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class CookieProvider {

    private final JwtProperties jwtProperties;

    public ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/api/v1/auth")
                .maxAge(Duration.ofMillis(jwtProperties.getRefreshTokenExpiration()))
                .sameSite("None")
                .build();
    }

    public ResponseCookie expireRefreshTokenCookie() {
        return ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/api/v1/auth")
                .maxAge(0)
                .sameSite("None")
                .build();
    }
}
