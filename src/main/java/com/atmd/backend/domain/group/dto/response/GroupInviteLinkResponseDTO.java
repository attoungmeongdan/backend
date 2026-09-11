package com.atmd.backend.domain.group.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupInviteLinkResponseDTO {

    private String inviteCode;
    private String inviteLink;

    public static GroupInviteLinkResponseDTO of(String inviteCode, String inviteLink) {
        return GroupInviteLinkResponseDTO.builder()
                .inviteCode(inviteCode)
                .inviteLink(inviteLink)
                .build();
    }
}
