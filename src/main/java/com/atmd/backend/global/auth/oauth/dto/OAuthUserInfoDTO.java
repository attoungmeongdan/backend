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

    /**
     * 소셜 로그인 시작 시 authorize URL에 함께 전달된 그룹 초대 코드.
     * state 값을 통해 콜백까지 운반되며, 없으면 null.
     */
    private String inviteCode;
}
