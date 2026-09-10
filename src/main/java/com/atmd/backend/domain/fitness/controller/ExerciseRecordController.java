package com.atmd.backend.domain.fitness.controller;

import com.atmd.backend.domain.fitness.dto.response.MeasurementHistoryResponse;
import com.atmd.backend.domain.fitness.dto.response.MeasurementInsightResponse;
import com.atmd.backend.domain.fitness.service.ExerciseRecordService;
import com.atmd.backend.domain.fitness.service.MeasurementInsightService;
import com.atmd.backend.global.auth.util.SecurityUtil;
import com.atmd.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/exercise-records")
@RequiredArgsConstructor
public class ExerciseRecordController {
    private final ExerciseRecordService exerciseRecordService;
    private final MeasurementInsightService measurementInsightService;

    @GetMapping("/measurement-history")
    public ResponseEntity<ApiResponse<MeasurementHistoryResponse>> getMeasurementHistory() {
        return ResponseEntity.ok(ApiResponse.success(
                exerciseRecordService.getMeasurementHistory(SecurityUtil.getCurrentUserId())
        ));
    }

    @Operation(
            summary = "측정 결과 맞춤 운동 추천 생성",
            description = "완료된 네 가지 운동을 연령·성별 기준과 비교하고, KSPO 운동 처방을 검색해 현재 체력에 맞는 추천 운동 3개를 생성합니다. 측정 그룹당 한 번만 생성되며 재요청 시 저장된 결과를 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인사이트 생성 또는 기존 결과 반환 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "프로필의 나이 또는 성별 누락"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "네 가지 운동 측정이 완료되지 않음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "임베딩 또는 AI 인사이트 생성 실패")
    })
    @PostMapping("/measurements/{measurementGroupId}/insights")
    public ResponseEntity<ApiResponse<MeasurementInsightResponse>> generateMeasurementInsight(
            @PathVariable String measurementGroupId
    ) {
        return ResponseEntity.ok(ApiResponse.success(measurementInsightService.generate(
                SecurityUtil.getCurrentUserId(), measurementGroupId
        )));
    }

    @Operation(
            summary = "측정 결과 맞춤 운동 추천 조회",
            description = "측정 그룹에 저장된 맞춤 추천 운동 3개를 조회합니다. OpenAI API는 다시 호출하지 않습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인사이트 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "측정 인사이트를 찾을 수 없음")
    })
    @GetMapping("/measurements/{measurementGroupId}/insights")
    public ResponseEntity<ApiResponse<MeasurementInsightResponse>> getMeasurementInsight(
            @PathVariable String measurementGroupId
    ) {
        return ResponseEntity.ok(ApiResponse.success(measurementInsightService.get(
                SecurityUtil.getCurrentUserId(), measurementGroupId
        )));
    }
}
