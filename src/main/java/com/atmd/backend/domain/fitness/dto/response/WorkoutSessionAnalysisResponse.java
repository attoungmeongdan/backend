package com.atmd.backend.domain.fitness.dto.response;

import com.atmd.backend.domain.fitness.enums.ExerciseType;

import java.time.LocalDateTime;

public record WorkoutSessionAnalysisResponse(
        Long sessionId,
        ExerciseType exerciseType,
        LocalDateTime measuredAt,
        double measuredValue,
        double averageValue,
        String unit,
        double achievementRate,
        String comparison,
        String comparisonMessage
) {
}
