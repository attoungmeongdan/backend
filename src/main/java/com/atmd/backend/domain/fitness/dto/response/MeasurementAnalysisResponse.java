package com.atmd.backend.domain.fitness.dto.response;

import java.util.List;

public record MeasurementAnalysisResponse(
        String measurementGroupId,
        List<ExercisePeerComparisonResponse> exerciseComparisons,
        double overallScore,
        MeasurementPercentileResponse percentile,
        List<PerformanceGroupComparisonResponse> performanceGroupComparisons,
        FitnessPerformanceResponse fitnessPerformance
) {
}
