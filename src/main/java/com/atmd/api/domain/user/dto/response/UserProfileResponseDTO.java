package com.atmd.api.domain.user.dto.response;

import com.atmd.api.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfileResponseDTO {

    private Long id;
    private String email;
    private String nickname;
    private String provider;

    public static UserProfileResponseDTO from(User user) {
        return UserProfileResponseDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .provider(user.getProvider().name())
                .build();
    }
}
