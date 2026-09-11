package com.atmd.backend.domain.fitness.service;

import com.atmd.backend.domain.fitness.enums.ExerciseType;
import com.atmd.backend.domain.fitness.enums.ExerciseSessionMode;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class RuntimeExerciseSessionTest {

    @Test
    void releasedTicketCanBeReservedAgainAfterFailedHandshake() {
        Instant now = Instant.parse("2026-09-09T00:00:00Z");
        RuntimeExerciseSession session = session(now);

        assertThat(session.reserveTicket("ticket", now)).isTrue();
        assertThat(session.reserveTicket("ticket", now)).isFalse();

        session.releaseTicket("ticket");

        assertThat(session.reserveTicket("ticket", now)).isTrue();
    }

    @Test
    void confirmedTicketCannotBeReservedAgain() {
        Instant now = Instant.parse("2026-09-09T00:00:00Z");
        RuntimeExerciseSession session = session(now);

        assertThat(session.reserveTicket("ticket", now)).isTrue();
        session.confirmTicket("ticket");

        assertThat(session.reserveTicket("ticket", now)).isFalse();
    }

    @Test
    void unlimitedPlankExpiresAfterFrameReceiveIdleTimeout() {
        Instant now = Instant.parse("2026-09-09T00:00:00Z");
        RuntimeExerciseSession session = session(now);
        session.start(now);
        session.acceptFrame(1, 1000, now);

        assertThat(session.isExpired(now.plusSeconds(15))).isFalse();
        assertThat(session.isExpired(now.plusSeconds(16))).isTrue();
    }

    private RuntimeExerciseSession session(Instant now) {
        return new RuntimeExerciseSession(
                1L,
                1L,
                ExerciseType.PLANK,
                ExerciseSessionMode.MEASUREMENT,
                "ticket",
                now.plusSeconds(60),
                0,
                15
        );
    }

    @Test
    void measurementRepetitionUsesResponsiveFramePolicy() {
        Instant now = Instant.parse("2026-09-09T00:00:00Z");
        RuntimeExerciseSession session = new RuntimeExerciseSession(
                1L, 1L, ExerciseType.PUSH_UP, ExerciseSessionMode.MEASUREMENT,
                "ticket", now.plusSeconds(60), 60, 15
        );

        assertThat(session.smoothingWindowSize()).isEqualTo(3);
        assertThat(session.confirmationFrames()).isEqualTo(2);
    }

    @Test
    void workoutAndPlankKeepStrictFramePolicy() {
        Instant now = Instant.parse("2026-09-09T00:00:00Z");
        RuntimeExerciseSession workout = new RuntimeExerciseSession(
                1L, 1L, ExerciseType.PUSH_UP, ExerciseSessionMode.WORKOUT,
                "ticket", now.plusSeconds(60), 0, 15
        );
        RuntimeExerciseSession plankMeasurement = session(now);

        assertThat(workout.smoothingWindowSize()).isEqualTo(5);
        assertThat(workout.confirmationFrames()).isEqualTo(3);
        assertThat(plankMeasurement.smoothingWindowSize()).isEqualTo(5);
        assertThat(plankMeasurement.confirmationFrames()).isEqualTo(3);
    }
}
