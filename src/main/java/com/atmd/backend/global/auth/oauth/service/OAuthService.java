package com.atmd.backend.global.auth.oauth.service;

import com.atmd.backend.domain.auth.exception.AuthErrorCode;
import com.atmd.backend.domain.user.entity.enums.Provider;
import com.atmd.backend.global.auth.oauth.dto.OAuthUserInfoDTO;
import com.atmd.backend.global.auth.oauth.dto.google.GoogleTokenResponseDTO;
import com.atmd.backend.global.auth.oauth.dto.google.GoogleUserInfoResponseDTO;
import com.atmd.backend.global.auth.oauth.dto.kakao.KakaoTokenResponseDTO;
import com.atmd.backend.global.auth.oauth.dto.kakao.KakaoUserInfoResponseDTO;
import com.atmd.backend.global.auth.oauth.properties.GoogleOAuthProperties;
import com.atmd.backend.global.auth.oauth.properties.KakaoOAuthProperties;
import com.atmd.backend.global.common.exception.GeneralException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class OAuthService {

    private final KakaoOAuthProperties kakaoProps;
    private final GoogleOAuthProperties googleProps;
    private final RedisTemplate<String, String> redisTemplate;

    private final RestClient restClient;

    public OAuthService(KakaoOAuthProperties kakaoProps, GoogleOAuthProperties googleProps,
                        RedisTemplate<String, String> redisTemplate) {
        this.kakaoProps = kakaoProps;
        this.googleProps = googleProps;
        this.redisTemplate = redisTemplate;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);
        this.restClient = RestClient.builder().requestFactory(factory).build();
    }

    private static final String STATE_PREFIX = "OAUTH_STATE:";
    private static final long STATE_TTL_SECONDS = 300L;

    public String getAuthorizeUrl(String provider) {
        String baseUrl = buildBaseAuthorizeUrl(provider);
        String state = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(STATE_PREFIX + state, provider, STATE_TTL_SECONDS, TimeUnit.SECONDS);
        return baseUrl + "&state=" + state;
    }

    private String buildBaseAuthorizeUrl(String provider) {
        return switch (provider.toLowerCase()) {
            case "kakao" -> kakaoProps.getAuthorizeUri()
                    + "?response_type=code"
                    + "&client_id=" + kakaoProps.getClientId()
                    + "&redirect_uri=" + kakaoProps.getRedirectUri();
            case "google" -> googleProps.getAuthorizeUri()
                    + "?response_type=code"
                    + "&client_id=" + googleProps.getClientId()
                    + "&redirect_uri=" + googleProps.getRedirectUri()
                    + "&scope=" + googleProps.getScope();
            default -> throw new GeneralException(AuthErrorCode.INVALID_PROVIDER);
        };
    }

    public OAuthUserInfoDTO getUserInfo(String provider, String code, String state) {
        validateAndConsumeState(state, provider);
        return switch (provider.toLowerCase()) {
            case "kakao" -> getKakaoUserInfo(code);
            case "google" -> getGoogleUserInfo(code);
            default -> throw new GeneralException(AuthErrorCode.INVALID_PROVIDER);
        };
    }

    private void validateAndConsumeState(String state, String provider) {
        if (!StringUtils.hasText(state)) {
            throw new GeneralException(AuthErrorCode.OAUTH_INVALID_STATE);
        }
        String stored = redisTemplate.opsForValue().getAndDelete(STATE_PREFIX + state);
        if (stored == null || !stored.equalsIgnoreCase(provider)) {
            throw new GeneralException(AuthErrorCode.OAUTH_INVALID_STATE);
        }
    }

    private OAuthUserInfoDTO getKakaoUserInfo(String code) {
        KakaoTokenResponseDTO token = exchangeKakaoToken(code);
        KakaoUserInfoResponseDTO userInfo;
        try {
            userInfo = restClient.get()
                    .uri(kakaoProps.getUserInfoUri())
                    .header("Authorization", "Bearer " + token.getAccessToken())
                    .retrieve()
                    .body(KakaoUserInfoResponseDTO.class);
        } catch (RestClientException e) {
            log.error("Kakao 사용자 정보 조회 실패", e);
            throw new GeneralException(AuthErrorCode.OAUTH_PROCESS_FAILED);
        }

        if (userInfo == null) throw new GeneralException(AuthErrorCode.OAUTH_PROCESS_FAILED);

        String email = userInfo.getEmail() != null
                ? userInfo.getEmail()
                : "kakao_" + userInfo.getId() + "@kakao.user";

        return OAuthUserInfoDTO.builder()
                .email(email)
                .nickname(userInfo.getNickname())
                .provider(Provider.KAKAO)
                .providerId(String.valueOf(userInfo.getId()))
                .build();
    }

    private KakaoTokenResponseDTO exchangeKakaoToken(String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoProps.getClientId());
        params.add("client_secret", kakaoProps.getClientSecret());
        params.add("redirect_uri", kakaoProps.getRedirectUri());
        params.add("code", code);

        try {
            KakaoTokenResponseDTO token = restClient.post()
                    .uri(kakaoProps.getTokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(params)
                    .retrieve()
                    .body(KakaoTokenResponseDTO.class);
            if (token == null) throw new GeneralException(AuthErrorCode.OAUTH_PROCESS_FAILED);
            return token;
        } catch (GeneralException e) {
            throw e;
        } catch (RestClientException e) {
            log.error("Kakao 토큰 교환 실패", e);
            throw new GeneralException(AuthErrorCode.OAUTH_PROCESS_FAILED);
        }
    }

    private OAuthUserInfoDTO getGoogleUserInfo(String code) {
        GoogleTokenResponseDTO token = exchangeGoogleToken(code);
        GoogleUserInfoResponseDTO userInfo;
        try {
            userInfo = restClient.get()
                    .uri(googleProps.getUserInfoUri())
                    .header("Authorization", "Bearer " + token.getAccessToken())
                    .retrieve()
                    .body(GoogleUserInfoResponseDTO.class);
        } catch (RestClientException e) {
            log.error("Google 사용자 정보 조회 실패", e);
            throw new GeneralException(AuthErrorCode.OAUTH_PROCESS_FAILED);
        }

        if (userInfo == null) throw new GeneralException(AuthErrorCode.OAUTH_PROCESS_FAILED);
        if (!Boolean.TRUE.equals(userInfo.getEmailVerified())) {
            throw new GeneralException(AuthErrorCode.OAUTH_UNVERIFIED_EMAIL);
        }

        return OAuthUserInfoDTO.builder()
                .email(userInfo.getEmail())
                .nickname(userInfo.getName() != null ? userInfo.getName() : userInfo.getGivenName())
                .provider(Provider.GOOGLE)
                .providerId(userInfo.getSub())
                .build();
    }

    private GoogleTokenResponseDTO exchangeGoogleToken(String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", googleProps.getClientId());
        params.add("client_secret", googleProps.getClientSecret());
        params.add("redirect_uri", googleProps.getRedirectUri());
        params.add("code", code);

        try {
            GoogleTokenResponseDTO token = restClient.post()
                    .uri(googleProps.getTokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(params)
                    .retrieve()
                    .body(GoogleTokenResponseDTO.class);
            if (token == null) throw new GeneralException(AuthErrorCode.OAUTH_PROCESS_FAILED);
            return token;
        } catch (GeneralException e) {
            throw e;
        } catch (RestClientException e) {
            log.error("Google 토큰 교환 실패", e);
            throw new GeneralException(AuthErrorCode.OAUTH_PROCESS_FAILED);
        }
    }
}
