package com.atmd.backend.domain.calendar.controller;

import com.atmd.backend.domain.calendar.dto.response.CalendarResponseDTO;
import com.atmd.backend.domain.calendar.dto.response.RecentExerciseStatusDTO;
import com.atmd.backend.domain.calendar.service.CalendarService;
import com.atmd.backend.global.auth.util.SecurityUtil;
import com.atmd.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Calendar", description = "캘린더 API (월별 운동 기록 및 달성률 조회)")
@RestController
@RequestMapping("/api/v1/calendars")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    @Operation(
            summary = "월별 캘린더 조회",
            description = "로그인한 유저의 특정 연월(year, month)에 해당하는 운동 기록, 달성률, "
                    + "날짜별 완료 측정 그룹 ID를 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "캘린더 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 연도 또는 월 입력"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<CalendarResponseDTO>> getMonthlyCalendar(
            @Parameter(description = "조회할 연도 (예: 2026)", example = "2026")
            @RequestParam(name = "year") int year,

            @Parameter(description = "조회할 월 (1~12)", example = "9")
            @RequestParam(name = "month") int month) {

        Long currentUserId = SecurityUtil.getCurrentUserId();
        CalendarResponseDTO response = calendarService.getMonthlyCalendar(currentUserId, year, month);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
            summary = "최근 7일 운동 여부 조회",
            description = "로그인한 유저의 오늘 포함 과거 7일간의 운동 완료 여부를 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "최근 7일 운동 여부 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    @GetMapping("/recent-7days")
    public ResponseEntity<ApiResponse<List<RecentExerciseStatusDTO>>> getRecentSevenDaysStatus() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        List<RecentExerciseStatusDTO> response = calendarService.getRecentSevenDaysStatus(currentUserId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
