package com.atmd.backend.domain.fitness.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "kspo_fitness_measurement",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_kspo_fitness_measurement_source",
                columnNames = {
                        "member_sequence_value",
                        "measurement_sequence_no",
                        "measurement_date"
                }
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KspoFitnessMeasurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_sequence_value", nullable = false, length = 100)
    private String memberSequenceValue;

    @Column(name = "measurement_sequence_no", nullable = false)
    private Integer measurementSequenceNo;

    @Column(name = "center_name", nullable = false, length = 100)
    private String centerName;

    @Column(name = "age_group_name", nullable = false, length = 30)
    private String ageGroupName;

    @Column(name = "measurement_place_type_name", nullable = false, length = 30)
    private String measurementPlaceTypeName;

    @Column(name = "measurement_age", nullable = false)
    private Integer measurementAge;

    @Column(name = "input_type_name", nullable = false, length = 30)
    private String inputTypeName;

    @Column(name = "certification_grade_name", length = 30)
    private String certificationGradeName;

    @Column(name = "measurement_date", nullable = false)
    private LocalDate measurementDate;

    @Column(name = "gender_code", nullable = false, length = 1)
    private String genderCode;

    @Column(name = "measurement_item_019_value", precision = 20, scale = 6)
    private BigDecimal measurementItem019Value;

    @Column(name = "measurement_item_023_value", precision = 20, scale = 6)
    private BigDecimal measurementItem023Value;

    @Column(name = "movement_prescription_content", columnDefinition = "text")
    private String movementPrescriptionContent;

    @Column(
            name = "imported_at",
            nullable = false,
            insertable = false,
            updatable = false,
            columnDefinition = "timestamp default current_timestamp"
    )
    private LocalDateTime importedAt;
}
