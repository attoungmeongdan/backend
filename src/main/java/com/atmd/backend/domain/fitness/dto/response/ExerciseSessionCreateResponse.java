package com.atmd.backend.domain.fitness.dto.response;

import com.atmd.backend.domain.fitness.entity.ExerciseSession;
import com.atmd.backend.domain.fitness.enums.ExerciseType;
import com.atmd.backend.domain.fitness.enums.MeasurementType;
import com.atmd.backend.domain.fitness.enums.ExerciseSessionMode;

public record ExerciseSessionCreateResponse(
        Long sessionId,
        ExerciseSessionMode mode,
        String measurementGroupId,
        ExerciseType exerciseType,
        MeasurementType measurementType,
        int timeLimitSeconds,
        String cameraOrientation,
        int calibrationSeconds,
        int transmissionFps,
        String webSocketPath,
        String socketTicket,
        String ruleVersion
) {
    public static ExerciseSessionCreateResponse from(ExerciseSession session, String socketTicket) {
        return new ExerciseSessionCreateResponse(
                session.getId(),
                session.getMode(),
                session.getMeasurementGroupId(),
                session.getExerciseType(),
                session.getMeasurementType(),
                session.getTimeLimitSeconds(),
                "SIDE",
                0,
                10,
                "/ws/v1/exercise-sessions/" + session.getId(),
                socketTicket,
                session.getRuleVersion()
        );
    }
}
