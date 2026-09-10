package com.atmd.backend.domain.fitness.dto.response;

import com.atmd.backend.domain.user.entity.enums.Gender;

import java.util.List;

public record MeasurementPercentileResponse(
        boolean available,
        Integer value,
        Integer topPercent,
        Gender comparisonGender,
        String comparisonAgeGroup,
        int sampleSize,
        String message,
        double userScore,
        Integer userBucketIndex,
        Long maximumBucketCount,
        List<PercentileBucketResponse> buckets
) {
}
