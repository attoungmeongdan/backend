package com.atmd.backend.domain.fitness.dto.response;

import java.time.LocalDateTime;

public record MeasurementHistoryValueResponse(
        String measurementGroupId,
        LocalDateTime measuredAt,
        double value,
        String unit
) {
}
