package com.atmd.backend.global.auth.oauth.dto;

import com.atmd.backend.domain.user.entity.enums.Provider;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuthUserInfoDTO {

    private String email;
    private String nickname;
    private Provider provider;
    private String providerId;
}
