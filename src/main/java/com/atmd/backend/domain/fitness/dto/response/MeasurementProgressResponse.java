package com.atmd.backend.domain.fitness.dto.response;

import com.atmd.backend.domain.fitness.enums.ExerciseType;

import java.util.List;

public record MeasurementProgressResponse(
        String measurementGroupId,
        List<ExerciseType> completedExercises,
        ExerciseType nextExerciseType,
        boolean completed
) {
}
