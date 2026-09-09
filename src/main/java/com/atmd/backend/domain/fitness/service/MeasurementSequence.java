package com.atmd.backend.domain.fitness.service;

import com.atmd.backend.domain.fitness.enums.ExerciseType;

import java.util.List;
import java.util.Set;

public final class MeasurementSequence {
    private static final List<ExerciseType> ORDER = List.of(
            ExerciseType.CHAIR_STAND,
            ExerciseType.PUSH_UP,
            ExerciseType.SIT_UP,
            ExerciseType.PLANK
    );

    private MeasurementSequence() {
    }

    public static ExerciseType first() {
        return ORDER.get(0);
    }

    public static ExerciseType next(Set<ExerciseType> completedExercises) {
        return ORDER.stream()
                .filter(exerciseType -> !completedExercises.contains(exerciseType))
                .findFirst()
                .orElse(null);
    }

    public static List<ExerciseType> orderedCompleted(Set<ExerciseType> completedExercises) {
        return ORDER.stream().filter(completedExercises::contains).toList();
    }
}
