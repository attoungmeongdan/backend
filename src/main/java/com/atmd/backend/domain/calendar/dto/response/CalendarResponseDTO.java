package com.atmd.backend.domain.calendar.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "월별 캘린더 조회 응답 DTO")
public class CalendarResponseDTO {

    @Schema(description = "조회 연도", example = "2026")
    private int year;

    @Schema(description = "조회 월", example = "9")
    private int month;

    @Schema(description = "당월 총 목표 일수", example = "30")
    private int totalTargetDays;

    @Schema(description = "수행 완료한 일수", example = "9")
    private int completedDays;

    @Schema(description = "월간 달성률 (%)", example = "30")
    private int achievementRate;

    @Schema(description = "일별 운동 수행 기록 목록")
    private List<DailyRecord> dailyRecords;

    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(description = "일별 운동 수행 상세")
    public static class DailyRecord {

        @Schema(description = "운동 날짜", example = "2026-09-01")
        private LocalDate date;

        @Schema(description = "운동 완료 종류 수", example = "3")
        private int exerciseCount;

        @Schema(
                description = "해당 날짜에 완료한 체력 측정 그룹 ID. 완료된 측정이 없으면 null",
                example = "550e8400-e29b-41d4-a716-446655440000",
                nullable = true
        )
        private String measurementGroupId;
    }
}
