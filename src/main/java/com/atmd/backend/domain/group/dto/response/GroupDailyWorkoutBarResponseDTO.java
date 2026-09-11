package com.atmd.backend.domain.group.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class GroupDailyWorkoutBarResponseDTO {

    private Long groupId;
    private String groupName;
    private LocalDate date;
    private List<GroupMemberWorkoutBarResponseDTO> members;
}
