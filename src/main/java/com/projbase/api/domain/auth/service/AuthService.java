package com.projbase.api.domain.auth.service;

import com.projbase.api.domain.auth.dto.request.LoginRequestDTO;
import com.projbase.api.domain.auth.dto.request.SignupRequestDTO;
import com.projbase.api.domain.auth.dto.response.AuthTokenResponseDTO;
import com.projbase.api.domain.auth.exception.AuthErrorCode;
import com.projbase.api.domain.user.entity.User;
import com.projbase.api.domain.user.entity.enums.Provider;
import com.projbase.api.domain.user.repository.UserRepository;
import com.projbase.api.global.auth.cookie.CookieProvider;
import com.projbase.api.global.auth.jwt.JwtProvider;
import com.projbase.api.global.auth.jwt.JwtService;
import com.projbase.api.global.auth.oauth.dto.OAuthUserInfoDTO;
import com.projbase.api.global.auth.oauth.service.OAuthService;
import com.projbase.api.global.common.exception.GeneralException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final JwtService jwtService;
    private final OAuthService oAuthService;
    private final CookieProvider cookieProvider;

    @Transactional
    public AuthTokenResponseDTO signup(SignupRequestDTO request, HttpServletResponse response) {
        if (userRepository.existsByEmailAndIsDeletedFalse(request.getEmail())) {
            throw new GeneralException(AuthErrorCode.DUPLICATE_EMAIL);
        }
        User user = User.ofLocal(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getNickname()
        );
        userRepository.save(user);
        return issueTokens(user, response);
    }

    @Transactional(readOnly = true)
    public AuthTokenResponseDTO login(LoginRequestDTO request, HttpServletResponse response) {
        User user = userRepository.findByEmailAndIsDeletedFalse(request.getEmail())
                .filter(u -> u.getProvider() == Provider.LOCAL)
                .orElseThrow(() -> new GeneralException(AuthErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new GeneralException(AuthErrorCode.INVALID_CREDENTIALS);
        }
        return issueTokens(user, response);
    }

    @Transactional
    public AuthTokenResponseDTO oauthLogin(String provider, String code, String state, HttpServletResponse response) {
        OAuthUserInfoDTO userInfo = oAuthService.getUserInfo(provider, code, state);
        User user = userRepository.findByProviderAndProviderIdAndIsDeletedFalse(userInfo.getProvider(), userInfo.getProviderId())
                .orElseGet(() -> userRepository.save(
                        User.ofOAuth(userInfo.getEmail(), userInfo.getNickname(),
                                userInfo.getProvider(), userInfo.getProviderId())
                ));
        return issueTokens(user, response);
    }

    public void logout(Long userId, HttpServletResponse response) {
        jwtService.deleteRefreshToken(userId);
        response.addHeader("Set-Cookie", cookieProvider.expireRefreshTokenCookie().toString());
    }

    @Transactional(readOnly = true)
    public AuthTokenResponseDTO refresh(String refreshToken, HttpServletResponse response) {
        if (!jwtProvider.validateToken(refreshToken)) {
            if (jwtProvider.isTokenExpired(refreshToken)) {
                throw new GeneralException(AuthErrorCode.EXPIRED_TOKEN);
            }
            throw new GeneralException(AuthErrorCode.INVALID_TOKEN);
        }
        if (!jwtProvider.isRefreshToken(refreshToken)) {
            throw new GeneralException(AuthErrorCode.INVALID_TOKEN);
        }

        Long userId = jwtProvider.getUserIdFromToken(refreshToken);

        if (!jwtService.validateRefreshToken(userId, refreshToken)) {
            throw new GeneralException(AuthErrorCode.INVALID_TOKEN);
        }

        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new GeneralException(AuthErrorCode.INVALID_TOKEN));

        return issueTokens(user, response);
    }

    private AuthTokenResponseDTO issueTokens(User user, HttpServletResponse response) {
        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());
        jwtService.saveRefreshToken(user.getId(), refreshToken);
        response.addHeader("Set-Cookie", cookieProvider.createRefreshTokenCookie(refreshToken).toString());
        return AuthTokenResponseDTO.of(accessToken);
    }
}
