package com.atmd.backend.domain.group.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.YearMonth;
import java.util.List;

@Getter
@Builder
public class GroupMonthlyWorkoutBarResponseDTO {

    private Long groupId;
    private String groupName;
    private YearMonth yearMonth;
    private int daysInMonth;
    private List<GroupMonthlyMemberSummaryDTO> members;
}
