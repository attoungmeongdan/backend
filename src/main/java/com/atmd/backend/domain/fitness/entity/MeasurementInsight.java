package com.atmd.backend.domain.fitness.entity;

import com.atmd.backend.domain.user.entity.User;
import com.atmd.backend.global.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "measurement_insight", uniqueConstraints =
        @UniqueConstraint(name = "uk_measurement_insight_group", columnNames = "measurement_group_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeasurementInsight extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "measurement_group_id", nullable = false, length = 36)
    private String measurementGroupId;

    @Column(name = "comparisons_json", nullable = false, columnDefinition = "text")
    private String comparisonsJson;

    @Column(name = "insights_json", nullable = false, columnDefinition = "text")
    private String insightsJson;

    @Column(name = "reference_prescriptions_json", nullable = false, columnDefinition = "text")
    private String referencePrescriptionsJson;

    @Column(name = "generation_status", nullable = false, length = 20)
    private String generationStatus;

    private MeasurementInsight(User user, String groupId, String comparisonsJson,
                               String insightsJson, String referencePrescriptionsJson) {
        this.user = user;
        this.measurementGroupId = groupId;
        this.comparisonsJson = comparisonsJson;
        this.insightsJson = insightsJson;
        this.referencePrescriptionsJson = referencePrescriptionsJson;
        this.generationStatus = "COMPLETED";
    }

    public static MeasurementInsight create(User user, String groupId, String comparisonsJson,
                                            String insightsJson, String referencePrescriptionsJson) {
        return new MeasurementInsight(user, groupId, comparisonsJson, insightsJson, referencePrescriptionsJson);
    }

    public static MeasurementInsight startGenerating(User user, String groupId) {
        MeasurementInsight insight = new MeasurementInsight(user, groupId, "[]", "[]", "[]");
        insight.generationStatus = "GENERATING";
        return insight;
    }

    public boolean isGenerating() {
        return "GENERATING".equals(generationStatus);
    }

    public void completeGeneration(String comparisonsJson, String insightsJson, String prescriptionsJson) {
        this.comparisonsJson = comparisonsJson;
        this.insightsJson = insightsJson;
        this.referencePrescriptionsJson = prescriptionsJson;
        this.generationStatus = "COMPLETED";
    }
}
