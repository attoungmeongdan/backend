package com.atmd.backend.domain.fitness.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record MeasurementResultsResponse(
        String measurementGroupId,
        LocalDateTime measuredAt,
        List<MeasuredExerciseResponse> exercises
) {
}
