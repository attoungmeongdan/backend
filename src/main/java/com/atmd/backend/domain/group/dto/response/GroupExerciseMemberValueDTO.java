package com.atmd.backend.domain.group.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupExerciseMemberValueDTO {

    private Long userId;
    private String nickname;
    private boolean isOwner;
    private long value;
}
