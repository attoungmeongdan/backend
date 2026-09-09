package com.atmd.backend.domain.fitness.dto.request;

import com.atmd.backend.domain.fitness.enums.ExerciseType;

import java.util.List;

public record PoseFrameMessage(
        String type,
        Long sessionId,
        ExerciseType exerciseType,
        long sequence,
        long timestamp,
        boolean mirrored,
        List<LandmarkDto> landmarks,
        List<LandmarkDto> worldLandmarks
) {
}
