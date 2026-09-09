package com.atmd.backend.domain.auth.dto.response;

public record OAuthCallbackResponseDTO(String resultType) {

    public static OAuthCallbackResponseDTO registered() {
        return new OAuthCallbackResponseDTO("REGISTERED");
    }

    public static OAuthCallbackResponseDTO signupRequired() {
        return new OAuthCallbackResponseDTO("SIGNUP_REQUIRED");
    }
}
