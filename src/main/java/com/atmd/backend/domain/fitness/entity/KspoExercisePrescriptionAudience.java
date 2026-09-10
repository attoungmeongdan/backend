package com.atmd.backend.domain.fitness.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "kspo_exercise_prescription_audience")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KspoExercisePrescriptionAudience {

    @EmbeddedId
    private KspoPrescriptionAudienceId id;

    @MapsId("prescriptionId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prescription_id", nullable = false)
    private KspoExercisePrescription prescription;

    @Column(name = "source_count", nullable = false)
    private Integer sourceCount;
}
