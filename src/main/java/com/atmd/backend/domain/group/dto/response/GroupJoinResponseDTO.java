package com.atmd.backend.domain.group.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupJoinResponseDTO {

    private Long groupId;
    private String groupName;
    private long currentMemberCount;

    public static GroupJoinResponseDTO of(Long groupId, String groupName, long currentMemberCount) {
        return GroupJoinResponseDTO.builder()
                .groupId(groupId)
                .groupName(groupName)
                .currentMemberCount(currentMemberCount)
                .build();
    }
}
