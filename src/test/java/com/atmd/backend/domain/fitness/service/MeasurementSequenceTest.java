package com.atmd.backend.domain.fitness.service;

import com.atmd.backend.domain.fitness.enums.ExerciseType;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class MeasurementSequenceTest {

    @Test
    void returnsNextExerciseFromCompletedExercises() {
        assertThat(MeasurementSequence.first()).isEqualTo(ExerciseType.CHAIR_STAND);
        assertThat(MeasurementSequence.next(Set.of(ExerciseType.CHAIR_STAND)))
                .isEqualTo(ExerciseType.PUSH_UP);
        assertThat(MeasurementSequence.next(Set.of(
                ExerciseType.CHAIR_STAND,
                ExerciseType.PUSH_UP,
                ExerciseType.SIT_UP
        ))).isEqualTo(ExerciseType.PLANK);
        assertThat(MeasurementSequence.next(Set.of(ExerciseType.values()))).isNull();
    }
}
