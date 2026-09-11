package com.atmd.backend.domain.group.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class GroupMemberDailyRecordDTO {

    private LocalDate date;
    private int exerciseTypeCount;
    private long chairStandCount;
    private long pushUpCount;
    private long sitUpCount;
    private long plankDurationMs;
}
