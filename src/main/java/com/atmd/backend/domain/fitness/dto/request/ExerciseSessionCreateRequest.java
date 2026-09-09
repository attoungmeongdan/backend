package com.atmd.backend.domain.fitness.dto.request;

import com.atmd.backend.domain.fitness.enums.ExerciseType;
import jakarta.validation.constraints.NotNull;

public record ExerciseSessionCreateRequest(
        @NotNull ExerciseType exerciseType
) {
}
