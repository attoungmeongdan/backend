package com.atmd.backend.domain.fitness.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@Embeddable
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KspoPrescriptionAudienceId implements Serializable {

    @Column(name = "prescription_id", nullable = false)
    private Long prescriptionId;

    @Column(name = "gender_code", nullable = false, length = 1)
    private String genderCode;

    @Column(name = "measurement_age", nullable = false)
    private Integer measurementAge;

    @Column(name = "certification_grade", nullable = false, length = 30)
    private String certificationGrade;
}
