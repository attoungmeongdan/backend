package com.atmd.backend.domain.fitness.controller;

import com.atmd.backend.domain.fitness.dto.request.ExerciseSessionCreateRequest;
import com.atmd.backend.domain.fitness.dto.response.ExerciseSessionCreateResponse;
import com.atmd.backend.domain.fitness.dto.response.ExerciseSessionResultResponse;
import com.atmd.backend.domain.fitness.dto.response.MeasurementProgressResponse;
import com.atmd.backend.domain.fitness.service.ExerciseSessionService;
import com.atmd.backend.domain.fitness.service.MeasurementFlowService;
import com.atmd.backend.global.auth.util.SecurityUtil;
import com.atmd.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Exercise Session", description = "웹캠 운동 및 체력 측정 세션 API")
@RestController
@RequestMapping("/api/v1/exercise-sessions")
@RequiredArgsConstructor
public class ExerciseSessionController {
    private final ExerciseSessionService exerciseSessionService;
    private final MeasurementFlowService measurementFlowService;

    @Operation(summary = "운동 세션 생성", description = "운동 유형과 모드에 맞는 세션을 생성하고 웹소켓 연결에 사용할 세션 ID와 일회용 티켓을 발급합니다. MEASUREMENT 모드는 정해진 측정 순서를 따라야 하며 WORKOUT 모드는 개별 운동을 자유롭게 시작할 수 있습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "운동 세션 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "지원하지 않는 운동 유형, 모드 또는 잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "진행 중인 세션이 있거나 측정 순서가 올바르지 않음")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ExerciseSessionCreateResponse>> create(@Valid @RequestBody ExerciseSessionCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(exerciseSessionService.create(SecurityUtil.getCurrentUserId(), request)));
    }

    @Operation(summary = "운동 세션 수동 종료", description = "진행 중인 WORKOUT 세션을 사용자가 직접 종료하고 최종 결과를 저장합니다. 자동 종료되는 MEASUREMENT 세션에는 사용하지 않습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "운동 세션 종료 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "다른 사용자의 운동 세션"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "운동 세션을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "수동 종료할 수 없는 세션 상태 또는 모드")
    })
    @PostMapping("/{sessionId}/complete")
    public ResponseEntity<ApiResponse<ExerciseSessionResultResponse>> complete(
            @Parameter(description = "종료할 WORKOUT 세션 ID", required = true) @PathVariable Long sessionId) {
        return ResponseEntity.ok(ApiResponse.success(exerciseSessionService.complete(SecurityUtil.getCurrentUserId(), sessionId)));
    }

    @Operation(summary = "운동 세션 결과 조회", description = "세션 ID로 운동 세션의 횟수, 무효 횟수, 유지 시간과 최종 상태를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "운동 세션 결과 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "다른 사용자의 운동 세션"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "운동 세션을 찾을 수 없음")
    })
    @GetMapping("/{sessionId}/result")
    public ResponseEntity<ApiResponse<ExerciseSessionResultResponse>> getResult(
            @Parameter(description = "조회할 운동 세션 ID", required = true) @PathVariable Long sessionId) {
        return ResponseEntity.ok(ApiResponse.success(exerciseSessionService.getResult(SecurityUtil.getCurrentUserId(), sessionId)));
    }

    @Operation(summary = "오늘의 체력 측정 진행 상태 조회", description = "오늘 진행 중이거나 완료된 측정 그룹과 운동별 완료 상태, 다음에 측정할 운동을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "측정 진행 상태 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    @GetMapping("/measurement/progress")
    public ResponseEntity<ApiResponse<MeasurementProgressResponse>> getMeasurementProgress() {
        return ResponseEntity.ok(ApiResponse.success(measurementFlowService.getTodayProgress(SecurityUtil.getCurrentUserId())));
    }

    @Operation(summary = "체력 측정 이어서 하기", description = "중단된 측정 그룹에서 아직 완료하지 않은 다음 운동 세션을 생성합니다. 기존 측정 결과와 측정 그룹 ID는 유지됩니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "다음 측정 세션 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "이어갈 측정 그룹을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "진행 중인 세션이 있거나 오늘 측정을 이미 완료함")
    })
    @PostMapping("/measurement/resume")
    public ResponseEntity<ApiResponse<ExerciseSessionCreateResponse>> resumeMeasurement() {
        return ResponseEntity.ok(ApiResponse.success(measurementFlowService.resume(SecurityUtil.getCurrentUserId())));
    }

    @Operation(summary = "체력 측정 처음부터 하기", description = "완료되지 않은 기존 측정 그룹을 폐기하고 새 측정 그룹을 발급하여 의자 앉았다 일어나기부터 다시 시작합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "새 측정 그룹 및 첫 세션 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "진행 중인 세션이 있거나 오늘 측정을 이미 완료함")
    })
    @PostMapping("/measurement/restart")
    public ResponseEntity<ApiResponse<ExerciseSessionCreateResponse>> restartMeasurement() {
        return ResponseEntity.ok(ApiResponse.success(measurementFlowService.restart(SecurityUtil.getCurrentUserId())));
    }
}
