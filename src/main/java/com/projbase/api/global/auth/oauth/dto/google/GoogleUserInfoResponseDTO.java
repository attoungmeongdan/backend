package com.projbase.api.global.auth.oauth.dto.google;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class GoogleUserInfoResponseDTO {

    private String sub;
    private String email;

    @JsonProperty("email_verified")
    private Boolean emailVerified;

    private String name;

    @JsonProperty("given_name")
    private String givenName;
}
