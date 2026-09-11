package com.atmd.backend.domain.group.dto.response;

import com.atmd.backend.domain.group.entity.GroupUser;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupMemberResponseDTO {

    private Long userId;
    private String nickname;
    private boolean isOwner;

    public static GroupMemberResponseDTO of(GroupUser groupUser, Long ownerId) {
        return GroupMemberResponseDTO.builder()
                .userId(groupUser.getUser().getId())
                .nickname(groupUser.getUser().getNickname())
                .isOwner(groupUser.getUser().getId().equals(ownerId))
                .build();
    }
}
