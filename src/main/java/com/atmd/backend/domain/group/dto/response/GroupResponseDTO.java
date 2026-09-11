package com.atmd.backend.domain.group.dto.response;

import com.atmd.backend.domain.group.entity.Group;
import com.atmd.backend.domain.group.entity.enums.GroupMembership;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupResponseDTO {

    private Long id;
    private String name;
    private GroupMembership membership;
    private int maxMemberCount;
    private long currentMemberCount;
    private int price;
    private Long ownerId;
    private boolean isOwner;

    public static GroupResponseDTO of(Group group, long currentMemberCount, Long viewerId) {
        return GroupResponseDTO.builder()
                .id(group.getId())
                .name(group.getName())
                .membership(group.getMembership())
                .maxMemberCount(group.getMaxMemberCount())
                .currentMemberCount(currentMemberCount)
                .price(group.getPrice())
                .ownerId(group.getOwner().getId())
                .isOwner(group.isOwner(viewerId))
                .build();
    }
}
