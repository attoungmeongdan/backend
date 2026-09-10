package com.atmd.backend.domain.fitness.dto.response;

import com.atmd.backend.domain.fitness.enums.ExerciseType;

public record ExercisePeerComparisonResponse(
        ExerciseType exerciseType,
        double measuredValue,
        double averageValue,
        String unit,
        String level,
        String message
) {
}
