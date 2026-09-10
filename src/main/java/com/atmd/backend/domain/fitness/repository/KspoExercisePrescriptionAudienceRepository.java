package com.atmd.backend.domain.fitness.repository;

import com.atmd.backend.domain.fitness.entity.KspoExercisePrescriptionAudience;
import com.atmd.backend.domain.fitness.entity.KspoPrescriptionAudienceId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KspoExercisePrescriptionAudienceRepository
        extends JpaRepository<KspoExercisePrescriptionAudience, KspoPrescriptionAudienceId> {
}
