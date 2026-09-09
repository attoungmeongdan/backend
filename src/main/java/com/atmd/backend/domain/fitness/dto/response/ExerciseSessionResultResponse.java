package com.atmd.backend.domain.fitness.dto.response;

import com.atmd.backend.domain.fitness.entity.ExerciseSession;
import com.atmd.backend.domain.fitness.enums.EvaluationStandard;
import com.atmd.backend.domain.fitness.enums.ExerciseSessionStatus;
import com.atmd.backend.domain.fitness.enums.ExerciseType;
import com.atmd.backend.domain.fitness.enums.ExerciseSessionMode;

import java.time.LocalDateTime;

public record ExerciseSessionResultResponse(
        Long sessionId,
        ExerciseSessionMode mode,
        String measurementGroupId,
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
                session.getMode(),
                session.getMeasurementGroupId(),
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
