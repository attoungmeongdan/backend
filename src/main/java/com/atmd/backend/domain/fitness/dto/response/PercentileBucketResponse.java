package com.atmd.backend.domain.fitness.dto.response;

public record PercentileBucketResponse(
        int minimum,
        int maximum,
        long count,
        double percentage
) {
}
