package com.atmd.backend.domain.auth.controller;

import com.atmd.backend.domain.auth.dto.request.LoginRequestDTO;
import com.atmd.backend.domain.auth.dto.request.OAuthSignupRequestDTO;
import com.atmd.backend.domain.auth.dto.request.SignupRequestDTO;
import com.atmd.backend.domain.auth.dto.response.AuthTokenResponseDTO;
import com.atmd.backend.domain.auth.dto.response.OAuthCallbackResponseDTO;
import com.atmd.backend.domain.auth.service.AuthService;
import com.atmd.backend.global.auth.oauth.service.OAuthService;
import com.atmd.backend.global.auth.util.SecurityUtil;
import com.atmd.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Auth", description = "인증 API (로그인 · 회원가입 · 토큰 관리)")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OAuthService oAuthService;

    @Hidden
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthTokenResponseDTO>> signup(
            @Valid @RequestBody SignupRequestDTO request,
            HttpServletResponse response) {
        return ResponseEntity.ok(ApiResponse.success(authService.signup(request, response)));
    }

    @Hidden
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthTokenResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO request,
            HttpServletResponse response) {
        return ResponseEntity.ok(ApiResponse.success(authService.login(request, response)));
    }

    @Operation(
            summary = "로그아웃",
            description = "리프레시 토큰을 무효화하고 쿠키를 만료시킵니다. Authorization 헤더에 액세스 토큰이 필요합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        authService.logout(SecurityUtil.getCurrentUserId(), response);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(
            summary = "액세스 토큰 재발급",
            description = "쿠키에 담긴 리프레시 토큰으로 새 액세스 토큰과 리프레시 토큰을 발급합니다. refresh_token 쿠키가 필요합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 재발급 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "리프레시 토큰이 만료되었거나 유효하지 않음")
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthTokenResponseDTO>> refresh(
            @CookieValue(name = "refresh_token") String refreshToken,
            HttpServletResponse response) {
        return ResponseEntity.ok(ApiResponse.success(authService.refresh(refreshToken, response)));
    }

    @Operation(
            summary = "소셜 로그인 인증 URL 조회",
            description = "provider(kakao, google)에 맞는 OAuth 인증 URL을 반환합니다. 프론트에서 해당 URL로 리다이렉트하면 소셜 로그인이 시작됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인증 URL 반환 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "지원하지 않는 provider")
    })
    @GetMapping("/oauth2/{provider}/authorize")
    public ResponseEntity<ApiResponse<Map<String, String>>> getAuthorizeUrl(
            @Parameter(description = "소셜 로그인 제공자 (kakao | google)", example = "kakao")
            @PathVariable String provider) {
        return ResponseEntity.ok(ApiResponse.success(Map.of("authorizeUrl", oAuthService.getAuthorizeUrl(provider))));
    }

    @Operation(
            summary = "소셜 로그인 콜백 처리",
            description = """
                    소셜 로그인 후 provider가 리다이렉트하는 콜백 엔드포인트입니다.
                    - 기존 회원: 액세스 토큰 발급 후 registered: true 반환
                    - 신규 회원: signup_token 쿠키 발급 후 registered: false 반환 → /oauth2/signup 으로 추가 정보 입력 필요
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "콜백 처리 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 state 값 또는 미인증 이메일"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "소셜 로그인 처리 중 오류")
    })
    @GetMapping("/oauth2/{provider}/callback")
    public ResponseEntity<ApiResponse<OAuthCallbackResponseDTO>> oauthCallback(
            @Parameter(description = "소셜 로그인 제공자 (kakao | google)", example = "kakao")
            @PathVariable String provider,
            @Parameter(description = "소셜 provider가 발급한 인가 코드")
            @RequestParam String code,
            @Parameter(description = "CSRF 방지용 state 값")
            @RequestParam String state,
            HttpServletResponse response) {
        return ResponseEntity.ok(ApiResponse.success(authService.handleOAuthCallback(provider, code, state, response)));
    }

    @Operation(
            summary = "소셜 회원가입 추가 정보 입력",
            description = """
                    소셜 로그인 콜백에서 registered: false 를 받은 신규 회원이 추가 정보를 입력하여 가입을 완료합니다.
                    signup_token 쿠키가 필요하며, 만료 시간은 30분입니다.
                    가입 완료 후 액세스 토큰과 리프레시 토큰(쿠키)이 발급됩니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "소셜 회원가입 완료 및 토큰 발급"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "signup_token 누락"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "signup_token 이 유효하지 않거나 만료됨"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 가입된 이메일")
    })
    @PostMapping("/oauth2/signup")
    public ResponseEntity<ApiResponse<AuthTokenResponseDTO>> oauthSignup(
            @CookieValue(name = "signup_token", required = false) String signupToken,
            @Valid @RequestBody OAuthSignupRequestDTO request,
            HttpServletResponse response) {
        return ResponseEntity.ok(ApiResponse.success(authService.oauthSignup(signupToken, request, response)));
    }
}
