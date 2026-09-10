package com.atmd.backend.domain.fitness.dto.response;

public record AiInsightResponse(
        String emoji,
        String exerciseName,
        String description
) {
}
