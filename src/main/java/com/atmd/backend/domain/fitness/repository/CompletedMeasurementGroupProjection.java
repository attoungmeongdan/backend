package com.atmd.backend.domain.fitness.repository;

import java.time.LocalDateTime;

public interface CompletedMeasurementGroupProjection {
    String getMeasurementGroupId();

    LocalDateTime getCompletedAt();
}
