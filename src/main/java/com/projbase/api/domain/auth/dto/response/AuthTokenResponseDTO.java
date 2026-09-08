package com.projbase.api.domain.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthTokenResponseDTO {

    private String accessToken;
    private String tokenType;

    public static AuthTokenResponseDTO of(String accessToken) {
        return AuthTokenResponseDTO.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .build();
    }
}
