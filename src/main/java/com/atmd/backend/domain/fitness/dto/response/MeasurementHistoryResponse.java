package com.atmd.backend.domain.fitness.dto.response;

import java.util.List;

public record MeasurementHistoryResponse(
        MeasurementRecordResponse today,
        List<MeasurementRecordResponse> previousMeasurements
) {
}
