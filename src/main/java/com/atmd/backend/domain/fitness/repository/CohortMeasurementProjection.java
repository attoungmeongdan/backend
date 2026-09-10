package com.atmd.backend.domain.fitness.repository;

public interface CohortMeasurementProjection {
    Integer getMeasurementAge();

    Double getChairStandValue();

    Double getSitUpValue();

    Double getPushUpValue();

    Double getPlankValue();
}
