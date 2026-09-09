package com.atmd.backend.domain.fitness.dto.request;

import com.atmd.backend.domain.fitness.enums.ExerciseType;
import com.atmd.backend.domain.fitness.enums.ExerciseSessionMode;
import jakarta.validation.constraints.NotNull;

public record ExerciseSessionCreateRequest(
        @NotNull ExerciseSessionMode mode,
        @NotNull ExerciseType exerciseType,
        String measurementGroupId
) {
}
