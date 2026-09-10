package com.atmd.backend.domain.fitness.dto.response;

import com.atmd.backend.domain.user.entity.enums.Gender;

public record PerformanceGroupComparisonResponse(
        Gender gender,
        String ageGroup,
        String label,
        double similarityRate
) {
}
