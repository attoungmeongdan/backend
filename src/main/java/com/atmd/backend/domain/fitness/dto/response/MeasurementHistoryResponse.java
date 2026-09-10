package com.atmd.backend.domain.fitness.dto.response;

public record MeasurementHistoryResponse(
        ExerciseMeasurementHistoryResponse chairStand,
        ExerciseMeasurementHistoryResponse sitUp,
        ExerciseMeasurementHistoryResponse pushUp,
        ExerciseMeasurementHistoryResponse plank
) {
}
