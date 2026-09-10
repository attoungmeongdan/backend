package com.atmd.backend.domain.fitness.repository;

import com.atmd.backend.domain.fitness.entity.KspoExercisePrescription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KspoExercisePrescriptionRepository
        extends JpaRepository<KspoExercisePrescription, Long> {
}
