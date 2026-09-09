package com.atmd.backend.domain.auth.service;

import com.atmd.backend.domain.address.entity.Address;
import com.atmd.backend.domain.address.service.AddressService;
import com.atmd.backend.domain.auth.dto.request.AddressCreateRequest;
import com.atmd.backend.domain.auth.dto.request.LoginRequestDTO;
import com.atmd.backend.domain.auth.dto.request.OAuthSignupRequestDTO;
import com.atmd.backend.domain.auth.dto.request.SignupRequestDTO;
import com.atmd.backend.domain.auth.dto.response.AuthTokenResponseDTO;
import com.atmd.backend.domain.auth.dto.response.OAuthCallbackResponseDTO;
import com.atmd.backend.domain.auth.exception.AuthErrorCode;
import com.atmd.backend.domain.user.entity.User;
import com.atmd.backend.domain.user.entity.enums.Provider;
import com.atmd.backend.domain.user.repository.UserRepository;
import com.atmd.backend.global.auth.cookie.CookieProvider;
import com.atmd.backend.global.auth.jwt.JwtProvider;
import com.atmd.backend.global.auth.jwt.JwtService;
import com.atmd.backend.global.auth.oauth.dto.OAuthUserInfoDTO;
import com.atmd.backend.global.auth.oauth.service.OAuthService;
import com.atmd.backend.global.auth.signupToken.SignupTokenProvider;
import com.atmd.backend.global.auth.signupToken.SignupTokenService;
import com.atmd.backend.global.common.exception.GeneralException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final JwtService jwtService;
    private final OAuthService oAuthService;
    private final CookieProvider cookieProvider;
    private final AddressService addressService;
    private final SignupTokenProvider signupTokenProvider;
    private final SignupTokenService signupTokenService;

    @Transactional
    public AuthTokenResponseDTO signup(SignupRequestDTO request, HttpServletResponse response) {
        throw new GeneralException(AuthErrorCode.LOCAL_SIGNUP_DISABLED);
    }

    @Transactional(readOnly = true)
    public AuthTokenResponseDTO login(LoginRequestDTO request, HttpServletResponse response) {
        throw new GeneralException(AuthErrorCode.LOCAL_LOGIN_DISABLED);
    }

    @Transactional
    public OAuthCallbackResponseDTO handleOAuthCallback(String provider, String code, String state, HttpServletResponse response) {
        OAuthUserInfoDTO userInfo = oAuthService.getUserInfo(provider, code, state);

        Optional<User> existingUser = userRepository.findByProviderAndProviderIdAndIsDeletedFalse(
                userInfo.getProvider(), userInfo.getProviderId()
        );

        if (existingUser.isPresent()) {
            issueTokens(existingUser.get(), response);
            return OAuthCallbackResponseDTO.registered();
        }

        String signupToken = signupTokenProvider.generate(
                userInfo.getProvider().name(), userInfo.getProviderId(), userInfo.getEmail()
        );
        signupTokenService.save(userInfo.getProvider().name(), userInfo.getProviderId(), signupToken);
        response.addHeader("Set-Cookie", cookieProvider.createSignupTokenCookie(signupToken).toString());

        return OAuthCallbackResponseDTO.signupRequired();
    }

    @Transactional
    public AuthTokenResponseDTO oauthSignup(String signupToken, OAuthSignupRequestDTO request, HttpServletResponse response) {
        if (signupToken == null || signupToken.isBlank()) {
            throw new GeneralException(AuthErrorCode.MISSING_SIGNUP_TOKEN);
        }
        if (!signupTokenProvider.validate(signupToken)) {
            throw new GeneralException(AuthErrorCode.INVALID_SIGNUP_TOKEN);
        }

        Map<String, String> info = signupTokenProvider.parse(signupToken);
        String providerStr = info.get("provider");
        String providerId = info.get("providerId");
        String email = info.get("email");

        if (!signupTokenService.validate(providerStr, providerId, signupToken)) {
            throw new GeneralException(AuthErrorCode.EXPIRED_SIGNUP_TOKEN);
        }

        if (userRepository.existsByEmailAndIsDeletedFalse(email)) {
            throw new GeneralException(AuthErrorCode.DUPLICATE_EMAIL);
        }

        Provider providerEnum = Provider.valueOf(providerStr);
        if (userRepository.findByProviderAndProviderIdAndIsDeletedFalse(providerEnum, providerId).isPresent()) {
            throw new GeneralException(AuthErrorCode.DUPLICATE_PROVIDER);
        }

        Address address = buildAddress(request.getAddress());

        User user = User.ofOAuth(email, request.getNickname(), providerEnum, providerId);
        user.updateProfile(request.getAge(), request.getGender(), request.getHeight(), request.getWeight());
        user.updateAddress(address);
        userRepository.save(user);

        signupTokenService.delete(providerStr, providerId);
        response.addHeader("Set-Cookie", cookieProvider.expireSignupTokenCookie().toString());

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
            throw new GeneralException(AuthErrorCode.TOKEN_TYPE_MISMATCH);
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

    private Address buildAddress(AddressCreateRequest req) {
        if (req == null) return null;
        return addressService.create(req.getRoadNameAddress(), req.getLotNumberAddress(), req.getDetailAddress());
    }
}
