package com.atmd.backend.domain.fitness.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record MeasurementInsightResponse(
        String measurementGroupId,
        List<AiInsightResponse> insights,
        LocalDateTime generatedAt
) {
}
