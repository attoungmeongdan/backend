package com.atmd.backend.domain.fitness.dto.request;

public record LandmarkDto(
        int index,
        double x,
        double y,
        double z,
        double visibility,
        Double presence
) {
}
