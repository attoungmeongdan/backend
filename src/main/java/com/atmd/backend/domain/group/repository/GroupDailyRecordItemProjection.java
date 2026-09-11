package com.atmd.backend.domain.group.repository;

import java.time.LocalDate;

public interface GroupDailyRecordItemProjection {
    Long getUserId();
    LocalDate getWorkoutDate();
    Long getChairStandCount();
    Long getPushUpCount();
    Long getSitUpCount();
    Long getPlankDurationMs();
}
