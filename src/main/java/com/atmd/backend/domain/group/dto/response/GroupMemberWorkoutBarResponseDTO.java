package com.atmd.backend.domain.group.dto.response;

import com.atmd.backend.domain.group.repository.GroupMemberWorkoutAggregateProjection;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupMemberWorkoutBarResponseDTO {

    private Long userId;
    private String nickname;
    private boolean isOwner;
    private long chairStandCount;
    private long pushUpCount;
    private long sitUpCount;
    private long plankDurationMs;

    public static GroupMemberWorkoutBarResponseDTO from(GroupMemberWorkoutAggregateProjection p) {
        return GroupMemberWorkoutBarResponseDTO.builder()
                .userId(p.getUserId())
                .nickname(p.getNickname())
                .isOwner(Boolean.TRUE.equals(p.getIsOwner()))
                .chairStandCount(p.getChairStandCount() == null ? 0L : p.getChairStandCount())
                .pushUpCount(p.getPushUpCount() == null ? 0L : p.getPushUpCount())
                .sitUpCount(p.getSitUpCount() == null ? 0L : p.getSitUpCount())
                .plankDurationMs(p.getPlankDurationMs() == null ? 0L : p.getPlankDurationMs())
                .build();
    }
}
