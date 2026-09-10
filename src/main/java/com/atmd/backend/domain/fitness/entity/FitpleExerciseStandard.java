package com.atmd.backend.domain.fitness.entity;

import com.atmd.backend.domain.fitness.enums.ExerciseType;
import com.atmd.backend.domain.fitness.enums.MeasurementType;
import com.atmd.backend.domain.user.entity.enums.Gender;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@Table(
        name = "fitple_exercise_standard",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_fitple_exercise_standard_version",
                columnNames = {"exercise_type", "gender", "minimum_age", "maximum_age", "version"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FitpleExerciseStandard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "exercise_type", nullable = false, length = 30)
    private ExerciseType exerciseType;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 10)
    private Gender gender;

    @Column(name = "minimum_age", nullable = false)
    private Integer minimumAge;

    @Column(name = "maximum_age", nullable = false)
    private Integer maximumAge;

    @Column(name = "average_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal averageValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "measurement_type", nullable = false, length = 30)
    private MeasurementType measurementType;

    @Column(name = "protocol", nullable = false, length = 100)
    private String protocol;

    @Column(name = "version", nullable = false, length = 30)
    private String version;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    public boolean supportsAge(int age) {
        return minimumAge <= age && age <= maximumAge;
    }
}
