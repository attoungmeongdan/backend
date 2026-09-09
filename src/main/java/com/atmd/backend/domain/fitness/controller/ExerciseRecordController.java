package com.atmd.backend.domain.fitness.controller;

import com.atmd.backend.domain.fitness.dto.response.MeasurementHistoryResponse;
import com.atmd.backend.domain.fitness.service.ExerciseRecordService;
import com.atmd.backend.global.auth.util.SecurityUtil;
import com.atmd.backend.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/exercise-records")
@RequiredArgsConstructor
public class ExerciseRecordController {
    private final ExerciseRecordService exerciseRecordService;

    @GetMapping("/measurement-history")
    public ResponseEntity<ApiResponse<MeasurementHistoryResponse>> getMeasurementHistory() {
        return ResponseEntity.ok(ApiResponse.success(
                exerciseRecordService.getMeasurementHistory(SecurityUtil.getCurrentUserId())
        ));
    }
}
