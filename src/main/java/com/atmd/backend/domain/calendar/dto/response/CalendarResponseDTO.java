package com.atmd.backend.domain.calendar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class CalendarResponseDTO {
    private int year;
    private int month;
    private int totalTargetDays;
    private int completedDays;
    private int achievementRate;
    private List<DailyRecord> dailyRecords;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class DailyRecord {
        private LocalDate date;
        private boolean isCompleted;
    }
}