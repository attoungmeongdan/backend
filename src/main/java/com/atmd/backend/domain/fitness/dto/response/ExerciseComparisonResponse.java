package com.atmd.backend.domain.fitness.dto.response;

import com.atmd.backend.domain.fitness.enums.ExerciseType;

public record ExerciseComparisonResponse(
        ExerciseType exerciseType,
        double measuredValue,
        double referenceValue,
        double achievementRate,
        String unit,
        String referenceSource
) {
}
