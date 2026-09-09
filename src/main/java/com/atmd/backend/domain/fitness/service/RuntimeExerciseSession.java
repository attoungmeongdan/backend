package com.atmd.backend.domain.fitness.service;

import com.atmd.backend.domain.fitness.analysis.ChairStandAnalysisState;
import com.atmd.backend.domain.fitness.analysis.PushUpAnalysisState;
import com.atmd.backend.domain.fitness.analysis.SitUpAnalysisState;
import com.atmd.backend.domain.fitness.analysis.PlankAnalysisState;
import com.atmd.backend.domain.fitness.dto.request.LandmarkDto;
import com.atmd.backend.domain.fitness.enums.ExerciseType;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

@Getter
class RuntimeExerciseSession {
    private final Long sessionId;
    private final Long userId;
    private final ExerciseType exerciseType;
    private final String socketTicket;
    private final Instant ticketExpiresAt;
    private final int timeLimitSeconds;
    private final ChairStandAnalysisState analysisState = new ChairStandAnalysisState();
    private final PushUpAnalysisState pushUpAnalysisState = new PushUpAnalysisState();
    private final SitUpAnalysisState sitUpAnalysisState = new SitUpAnalysisState();
    private final PlankAnalysisState plankAnalysisState = new PlankAnalysisState();
    private final Deque<List<LandmarkDto>> recentFrames = new ArrayDeque<>();
    private long lastSequence = -1;
    private long lastFrameTimestamp = -1;
    private Instant startedAt;
    private Instant plankHoldingStartedAt;
    private long validDurationMs;
    private boolean socketTicketConsumed;
    private boolean completed;

    RuntimeExerciseSession(
            Long sessionId,
            Long userId,
            ExerciseType exerciseType,
            String socketTicket,
            Instant ticketExpiresAt,
            int timeLimitSeconds
    ) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.exerciseType = exerciseType;
        this.socketTicket = socketTicket;
        this.ticketExpiresAt = ticketExpiresAt;
        this.timeLimitSeconds = timeLimitSeconds;
    }

    boolean consumeTicket(String ticket, Instant now) {
        if (socketTicketConsumed || now.isAfter(ticketExpiresAt) || !socketTicket.equals(ticket)) {
            return false;
        }
        socketTicketConsumed = true;
        return true;
    }

    void start(Instant now) {
        if (startedAt == null) {
            startedAt = now;
        }
    }

    long remainingTimeMs(Instant now) {
        if (timeLimitSeconds == 0) {
            return -1;
        }
        if (startedAt == null) {
            return timeLimitSeconds * 1000L;
        }
        long elapsed = now.toEpochMilli() - startedAt.toEpochMilli();
        return Math.max(0, timeLimitSeconds * 1000L - elapsed);
    }

    void acceptFrame(long sequence, long timestamp) {
        lastSequence = sequence;
        lastFrameTimestamp = timestamp;
    }

    boolean isExpired(Instant now) {
        if (startedAt == null) {
            return now.isAfter(ticketExpiresAt);
        }
        return timeLimitSeconds > 0 && remainingTimeMs(now) == 0;
    }

    void complete() {
        completed = true;
    }

    void startPlankHolding(Instant now) {
        if (plankHoldingStartedAt == null) {
            plankHoldingStartedAt = now;
        }
    }

    void stopPlankHolding(Instant now) {
        if (plankHoldingStartedAt != null) {
            validDurationMs = Math.max(0, now.toEpochMilli() - plankHoldingStartedAt.toEpochMilli());
        }
    }

    long validDurationMs(Instant now) {
        if (exerciseType == ExerciseType.PLANK && plankHoldingStartedAt != null && !completed) {
            return Math.max(0, now.toEpochMilli() - plankHoldingStartedAt.toEpochMilli());
        }
        return validDurationMs;
    }

    String currentPhase() {
        return switch (exerciseType) {
            case PUSH_UP -> pushUpAnalysisState.getPhase().name();
            case SIT_UP -> sitUpAnalysisState.getPhase().name();
            case PLANK -> plankAnalysisState.getPhase().name();
            default -> analysisState.getPhase().name();
        };
    }

    int validCount() {
        return switch (exerciseType) {
            case PUSH_UP -> pushUpAnalysisState.getValidCount();
            case SIT_UP -> sitUpAnalysisState.getValidCount();
            case PLANK -> 0;
            default -> analysisState.getValidCount();
        };
    }

    int invalidCount() {
        return switch (exerciseType) {
            case PUSH_UP -> pushUpAnalysisState.getInvalidCount();
            case SIT_UP -> sitUpAnalysisState.getInvalidCount();
            case PLANK -> 0;
            default -> analysisState.getInvalidCount();
        };
    }
}
