package com.atmd.backend.domain.fitness.dto.response;

import com.atmd.backend.domain.fitness.entity.ExerciseSession;
import com.atmd.backend.domain.fitness.enums.EvaluationStandard;
import com.atmd.backend.domain.fitness.enums.ExerciseSessionStatus;
import com.atmd.backend.domain.fitness.enums.ExerciseType;

import java.time.LocalDateTime;

public record ExerciseSessionResultResponse(
        Long sessionId,
        ExerciseType exerciseType,
        ExerciseSessionStatus status,
        EvaluationStandard evaluationStandard,
        int validCount,
        int invalidCount,
        long validDurationMs,
        int timeLimitSeconds,
        String ruleVersion,
        LocalDateTime measurementStartedAt,
        LocalDateTime completedAt
) {
    public static ExerciseSessionResultResponse from(ExerciseSession session) {
        return new ExerciseSessionResultResponse(
                session.getId(),
                session.getExerciseType(),
                session.getStatus(),
                session.getEvaluationStandard(),
                session.getValidCount(),
                session.getInvalidCount(),
                session.getValidDurationMs(),
                session.getTimeLimitSeconds(),
                session.getRuleVersion(),
                session.getMeasurementStartedAt(),
                session.getCompletedAt()
        );
    }
}
