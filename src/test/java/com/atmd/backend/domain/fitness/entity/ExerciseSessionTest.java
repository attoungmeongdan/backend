package com.atmd.backend.domain.fitness.entity;

import com.atmd.backend.domain.fitness.enums.ExerciseSessionMode;
import com.atmd.backend.domain.fitness.enums.ExerciseType;
import com.atmd.backend.domain.user.entity.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ExerciseSessionTest {

    @Test
    void measurementUsesExerciseTimeLimitAndGroupId() {
        ExerciseSession session = ExerciseSession.create(
                mock(User.class),
                ExerciseSessionMode.MEASUREMENT,
                ExerciseType.CHAIR_STAND,
                "measurement-group-id"
        );

        assertThat(session.getTimeLimitSeconds()).isEqualTo(30);
        assertThat(session.getMeasurementGroupId()).isEqualTo("measurement-group-id");
        assertThat(session.getMode()).isEqualTo(ExerciseSessionMode.MEASUREMENT);
    }

    @Test
    void workoutHasNoTimeLimitOrMeasurementGroup() {
        for (ExerciseType exerciseType : ExerciseType.values()) {
            ExerciseSession session = ExerciseSession.create(
                    mock(User.class),
                    ExerciseSessionMode.WORKOUT,
                    exerciseType,
                    null
            );

            assertThat(session.getTimeLimitSeconds()).isZero();
            assertThat(session.getMeasurementGroupId()).isNull();
            assertThat(session.getMode()).isEqualTo(ExerciseSessionMode.WORKOUT);
        }
    }
}
