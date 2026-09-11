package com.atmd.backend.domain.group.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GroupMonthlyMemberSummaryDTO {

    private int rank;
    private Long userId;
    private String nickname;
    private boolean isOwner;
    private double executionRate;
    private int executedDays;
    private int totalExerciseTypes;
    private List<GroupMemberDailyRecordDTO> days;
}
