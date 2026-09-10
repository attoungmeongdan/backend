package com.atmd.backend.domain.fitness.dto.response;

import com.atmd.backend.domain.fitness.enums.ExerciseType;

public record MeasuredExerciseResponse(
        ExerciseType exerciseType,
        double value,
        String unit
) {
}
