package com.atmd.backend.domain.fitness.dto.response;

import com.atmd.backend.domain.user.entity.enums.Gender;

public record FitnessPerformanceResponse(
        Gender gender,
        String ageGroup,
        String label,
        String message
) {
}
