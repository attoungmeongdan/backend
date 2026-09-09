package com.atmd.backend.domain.fitness.dto.response;

import com.atmd.backend.domain.fitness.enums.ExerciseType;

import java.time.LocalDateTime;
import java.util.Map;

public record MeasurementRecordResponse(
        String measurementGroupId,
        LocalDateTime measuredAt,
        double totalScore,
        Map<ExerciseType, MeasurementExerciseValueResponse> exercises
) {
}
