package com.atmd.backend.domain.fitness.controller;

import com.atmd.backend.domain.fitness.dto.request.ExerciseSessionCreateRequest;
import com.atmd.backend.domain.fitness.dto.response.ExerciseSessionCreateResponse;
import com.atmd.backend.domain.fitness.dto.response.ExerciseSessionResultResponse;
import com.atmd.backend.domain.fitness.service.ExerciseSessionService;
import com.atmd.backend.global.auth.util.SecurityUtil;
import com.atmd.backend.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/exercise-sessions")
@RequiredArgsConstructor
public class ExerciseSessionController {
    private final ExerciseSessionService exerciseSessionService;

    @PostMapping
    public ResponseEntity<ApiResponse<ExerciseSessionCreateResponse>> create(
            @Valid @RequestBody ExerciseSessionCreateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                exerciseSessionService.create(SecurityUtil.getCurrentUserId(), request)
        ));
    }

    @PostMapping("/{sessionId}/complete")
    public ResponseEntity<ApiResponse<ExerciseSessionResultResponse>> complete(@PathVariable Long sessionId) {
        return ResponseEntity.ok(ApiResponse.success(
                exerciseSessionService.complete(SecurityUtil.getCurrentUserId(), sessionId)
        ));
    }

    @GetMapping("/{sessionId}/result")
    public ResponseEntity<ApiResponse<ExerciseSessionResultResponse>> getResult(@PathVariable Long sessionId) {
        return ResponseEntity.ok(ApiResponse.success(
                exerciseSessionService.getResult(SecurityUtil.getCurrentUserId(), sessionId)
        ));
    }
}
