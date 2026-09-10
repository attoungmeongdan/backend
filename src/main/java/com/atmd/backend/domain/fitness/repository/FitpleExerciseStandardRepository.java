package com.atmd.backend.domain.fitness.repository;

import com.atmd.backend.domain.fitness.entity.FitpleExerciseStandard;
import com.atmd.backend.domain.fitness.enums.ExerciseType;
import com.atmd.backend.domain.user.entity.enums.Gender;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FitpleExerciseStandardRepository
        extends JpaRepository<FitpleExerciseStandard, Long> {

    Optional<FitpleExerciseStandard> findFirstByExerciseTypeAndGenderAndMinimumAgeLessThanEqualAndMaximumAgeGreaterThanEqualAndIsActiveTrueOrderByIdDesc(
            ExerciseType exerciseType,
            Gender gender,
            int maximumMinimumAge,
            int minimumMaximumAge
    );
}
