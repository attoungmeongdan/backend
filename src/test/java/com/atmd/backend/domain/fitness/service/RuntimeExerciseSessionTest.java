package com.atmd.backend.domain.fitness.service;

import com.atmd.backend.domain.fitness.enums.ExerciseType;
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
                "ticket",
                now.plusSeconds(60),
                0,
                15
        );
    }
}
