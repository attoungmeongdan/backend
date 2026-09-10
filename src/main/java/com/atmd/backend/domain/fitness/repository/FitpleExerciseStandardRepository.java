package com.atmd.backend.domain.fitness.repository;

import com.atmd.backend.domain.fitness.entity.FitpleExerciseStandard;
import com.atmd.backend.domain.fitness.enums.ExerciseType;
import com.atmd.backend.domain.user.entity.enums.Gender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FitpleExerciseStandardRepository
        extends JpaRepository<FitpleExerciseStandard, Long> {

    @Query("""
            SELECT standard
            FROM FitpleExerciseStandard standard
            WHERE standard.exerciseType = :exerciseType
              AND standard.gender = :gender
              AND :age BETWEEN standard.minimumAge AND standard.maximumAge
              AND standard.isActive = true
            """)
    Optional<FitpleExerciseStandard> findActiveStandard(
            @Param("exerciseType") ExerciseType exerciseType,
            @Param("gender") Gender gender,
            @Param("age") int age
    );
}
