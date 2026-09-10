package com.atmd.backend.domain.fitness.controller;

import com.atmd.backend.domain.fitness.dto.response.MeasurementAnalysisResponse;
import com.atmd.backend.domain.fitness.dto.response.MeasurementHistoryResponse;
import com.atmd.backend.domain.fitness.dto.response.MeasurementInsightResponse;
import com.atmd.backend.domain.fitness.dto.response.MeasurementResultsResponse;
import com.atmd.backend.domain.fitness.dto.response.WorkoutSessionAnalysisResponse;
import com.atmd.backend.domain.fitness.service.ExerciseRecordService;
import com.atmd.backend.domain.fitness.service.MeasurementAnalysisService;
import com.atmd.backend.domain.fitness.service.MeasurementInsightService;
import com.atmd.backend.domain.fitness.service.WorkoutAnalysisService;
import com.atmd.backend.global.auth.util.SecurityUtil;
import com.atmd.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Exercise Record", description = "운동 측정 결과, 분석 및 AI 추천 API")
@RestController
@RequestMapping("/api/v1/exercise-records")
@RequiredArgsConstructor
public class ExerciseRecordController {
    private final ExerciseRecordService exerciseRecordService;
    private final MeasurementAnalysisService measurementAnalysisService;
    private final MeasurementInsightService measurementInsightService;
    private final WorkoutAnalysisService workoutAnalysisService;

    @Operation(summary = "측정 이력 조회", description = "로그인한 사용자의 완료된 체력 측정 이력을 최신순으로 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "측정 이력 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    @GetMapping("/measurement-history")
    public ResponseEntity<ApiResponse<MeasurementHistoryResponse>> getMeasurementHistory() {
        return ResponseEntity.ok(ApiResponse.success(exerciseRecordService.getMeasurementHistory(SecurityUtil.getCurrentUserId())));
    }

    @Operation(summary = "운동별 측정 결과 조회", description = "완료된 측정 그룹의 의자 앉았다 일어나기, 팔굽혀펴기, 윗몸일으키기 횟수와 플랭크 유지 시간을 조회합니다. 평균이나 비교 결과는 포함하지 않습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "운동별 측정 결과 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "측정 그룹을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "네 가지 운동 측정이 완료되지 않음")
    })
    @GetMapping("/measurements/{measurementGroupId}/results")
    public ResponseEntity<ApiResponse<MeasurementResultsResponse>> getMeasurementResults(
            @Parameter(description = "조회할 측정 그룹 ID", required = true) @PathVariable String measurementGroupId) {
        return ResponseEntity.ok(ApiResponse.success(exerciseRecordService.getMeasurementResults(SecurityUtil.getCurrentUserId(), measurementGroupId)));
    }

    @Operation(summary = "종합 측정 분석 조회", description = "운동별 동연령대 평균 비교, 동일 성별·연령대 사용자의 최신 완료 기록을 이용한 백분위 분포, 운동 수행 연령·성별 수준을 조회합니다. 비교 표본이 30명 미만이면 백분위는 제공하지 않습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "종합 측정 분석 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "사용자의 나이 또는 성별 정보가 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "측정 그룹 또는 운동 기준을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "네 가지 운동 측정이 완료되지 않음")
    })
    @GetMapping("/measurements/{measurementGroupId}/analysis")
    public ResponseEntity<ApiResponse<MeasurementAnalysisResponse>> getMeasurementAnalysis(
            @Parameter(description = "분석할 측정 그룹 ID", required = true) @PathVariable String measurementGroupId) {
        return ResponseEntity.ok(ApiResponse.success(measurementAnalysisService.getAnalysis(SecurityUtil.getCurrentUserId(), measurementGroupId)));
    }

    @Operation(summary = "완료된 운동 세션 분석 조회", description = "완료된 WORKOUT 세션의 횟수 또는 유지 시간과 사용자 성별·연령대 평균을 비교하여 낮음, 비슷함, 높음 결과를 제공합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "운동 세션 분석 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "사용자의 나이 또는 성별 정보가 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "다른 사용자의 운동 세션"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "운동 세션 또는 운동 기준을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "WORKOUT 모드의 완료된 세션이 아님")
    })
    @GetMapping("/workouts/{sessionId}/analysis")
    public ResponseEntity<ApiResponse<WorkoutSessionAnalysisResponse>> getWorkoutAnalysis(
            @Parameter(description = "완료된 WORKOUT 세션 ID", required = true) @PathVariable Long sessionId) {
        return ResponseEntity.ok(ApiResponse.success(workoutAnalysisService.getAnalysis(SecurityUtil.getCurrentUserId(), sessionId)));
    }

    @Operation(summary = "측정 결과 맞춤 운동 추천 생성", description = "완료된 네 가지 운동을 연령·성별 기준과 비교하고 KSPO 운동 처방을 검색하여 현재 체력에 맞는 추천 운동 3개를 생성합니다. 측정 그룹당 한 번만 생성하며 재요청하면 저장된 결과를 반환합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "추천 생성 또는 저장된 결과 반환 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "프로필의 나이 또는 성별 누락"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "측정 그룹을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "측정 미완료 또는 추천 생성 중"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "임베딩 또는 AI 추천 생성 실패")
    })
    @PostMapping("/measurements/{measurementGroupId}/insights")
    public ResponseEntity<ApiResponse<MeasurementInsightResponse>> generateMeasurementInsight(
            @Parameter(description = "추천을 생성할 측정 그룹 ID", required = true) @PathVariable String measurementGroupId) {
        return ResponseEntity.ok(ApiResponse.success(measurementInsightService.generate(SecurityUtil.getCurrentUserId(), measurementGroupId)));
    }

    @Operation(summary = "측정 결과 맞춤 운동 추천 조회", description = "측정 그룹에 저장된 맞춤 추천 운동 3개를 조회합니다. OpenAI API를 다시 호출하지 않습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "맞춤 운동 추천 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "저장된 맞춤 운동 추천을 찾을 수 없음")
    })
    @GetMapping("/measurements/{measurementGroupId}/insights")
    public ResponseEntity<ApiResponse<MeasurementInsightResponse>> getMeasurementInsight(
            @Parameter(description = "추천을 조회할 측정 그룹 ID", required = true) @PathVariable String measurementGroupId) {
        return ResponseEntity.ok(ApiResponse.success(measurementInsightService.get(SecurityUtil.getCurrentUserId(), measurementGroupId)));
    }
}
