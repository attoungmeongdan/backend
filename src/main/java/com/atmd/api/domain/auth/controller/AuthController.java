package com.atmd.api.domain.auth.controller;

import com.atmd.api.domain.auth.dto.request.LoginRequestDTO;
import com.atmd.api.domain.auth.dto.request.SignupRequestDTO;
import com.atmd.api.domain.auth.dto.response.AuthTokenResponseDTO;
import com.atmd.api.domain.auth.service.AuthService;
import com.atmd.api.global.auth.oauth.service.OAuthService;
import com.atmd.api.global.auth.util.SecurityUtil;
import com.atmd.api.global.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OAuthService oAuthService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthTokenResponseDTO>> signup(
            @Valid @RequestBody SignupRequestDTO request,
            HttpServletResponse response) {
        return ResponseEntity.ok(ApiResponse.success(authService.signup(request, response)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthTokenResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO request,
            HttpServletResponse response) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request, response)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        authService.logout(SecurityUtil.getCurrentUserId(), response);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthTokenResponseDTO>> refresh(
            @CookieValue(name = "refresh_token") String refreshToken,
            HttpServletResponse response) {
        return ResponseEntity.ok(ApiResponse.success(authService.refresh(refreshToken, response)));
    }

    @GetMapping("/oauth2/{provider}/authorize")
    public ResponseEntity<ApiResponse<Map<String, String>>> getAuthorizeUrl(@PathVariable String provider) {
        return ResponseEntity.ok(ApiResponse.success(Map.of("authorizeUrl", oAuthService.getAuthorizeUrl(provider))));
    }

    @GetMapping("/oauth2/{provider}/callback")
    public ResponseEntity<ApiResponse<AuthTokenResponseDTO>> oauthCallback(
            @PathVariable String provider,
            @RequestParam String code,
            @RequestParam String state,
            HttpServletResponse response) {
        return ResponseEntity.ok(ApiResponse.success(authService.oauthLogin(provider, code, state, response)));
    }
}
