package com.atmd.backend.domain.fitness.repository;

import com.atmd.backend.domain.fitness.entity.MeasurementInsight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MeasurementInsightRepository extends JpaRepository<MeasurementInsight, Long> {
    Optional<MeasurementInsight> findByUserIdAndMeasurementGroupId(Long userId, String measurementGroupId);
}
