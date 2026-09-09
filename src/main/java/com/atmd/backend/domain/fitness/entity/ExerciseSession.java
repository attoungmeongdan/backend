package com.atmd.backend.domain.fitness.entity;

import com.atmd.backend.domain.fitness.enums.EvaluationStandard;
import com.atmd.backend.domain.fitness.enums.ExerciseSessionStatus;
import com.atmd.backend.domain.fitness.enums.ExerciseSessionMode;
import com.atmd.backend.domain.fitness.enums.ExerciseType;
import com.atmd.backend.domain.fitness.enums.MeasurementType;
import com.atmd.backend.domain.user.entity.User;
import com.atmd.backend.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "exercise_session")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExerciseSession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "exercise_type", nullable = false, length = 30)
    private ExerciseType exerciseType;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_mode", nullable = false, length = 20,
            columnDefinition = "varchar(20) default 'MEASUREMENT'")
    private ExerciseSessionMode mode;

    @Column(name = "measurement_group_id", length = 36)
    private String measurementGroupId;

    @Enumerated(EnumType.STRING)
    @Column(name = "measurement_type", nullable = false, length = 30)
    private MeasurementType measurementType;

    @Enumerated(EnumType.STRING)
    @Column(name = "evaluation_standard", nullable = false, length = 20)
    private EvaluationStandard evaluationStandard;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExerciseSessionStatus status;

    @Column(name = "time_limit_seconds", nullable = false)
    private int timeLimitSeconds;

    @Column(name = "valid_count", nullable = false)
    private int validCount;

    @Column(name = "invalid_count", nullable = false)
    private int invalidCount;

    @Column(name = "valid_duration_ms", nullable = false)
    private long validDurationMs;

    @Column(name = "rule_version", nullable = false, length = 50)
    private String ruleVersion;

    @Column(name = "measurement_started_at")
    private LocalDateTime measurementStartedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    private ExerciseSession(
            User user,
            ExerciseSessionMode mode,
            ExerciseType exerciseType,
            MeasurementType measurementType,
            EvaluationStandard evaluationStandard,
            int timeLimitSeconds,
            String ruleVersion,
            String measurementGroupId
    ) {
        this.user = user;
        this.mode = mode;
        this.exerciseType = exerciseType;
        this.measurementGroupId = measurementGroupId;
        this.measurementType = measurementType;
        this.evaluationStandard = evaluationStandard;
        this.status = ExerciseSessionStatus.CREATED;
        this.timeLimitSeconds = timeLimitSeconds;
        this.ruleVersion = ruleVersion;
    }

    public static ExerciseSession create(
            User user,
            ExerciseSessionMode mode,
            ExerciseType exerciseType,
            String measurementGroupId
    ) {
        int timeLimitSeconds = mode == ExerciseSessionMode.WORKOUT ? 0 : switch (exerciseType) {
            case CHAIR_STAND -> 30;
            case PUSH_UP, SIT_UP -> 60;
            case PLANK -> 0;
        };

        return switch (exerciseType) {
            case CHAIR_STAND -> new ExerciseSession(user, mode, exerciseType, MeasurementType.REPETITION,
                    EvaluationStandard.KSPO, timeLimitSeconds, "CHAIR_STAND_V1", measurementGroupId);
            case PUSH_UP -> new ExerciseSession(user, mode, exerciseType, MeasurementType.REPETITION,
                    EvaluationStandard.FITPLE, timeLimitSeconds, "PUSH_UP_V1", measurementGroupId);
            case SIT_UP -> new ExerciseSession(user, mode, exerciseType, MeasurementType.REPETITION,
                    EvaluationStandard.KSPO, timeLimitSeconds, "SIT_UP_V1", measurementGroupId);
            case PLANK -> new ExerciseSession(user, mode, exerciseType, MeasurementType.VALID_DURATION,
                    EvaluationStandard.FITPLE, timeLimitSeconds, "PLANK_V1", measurementGroupId);
            default -> throw new IllegalArgumentException("Unsupported exercise: " + exerciseType);
        };
    }

    public void start(LocalDateTime startedAt) {
        if (status == ExerciseSessionStatus.CREATED) {
            status = ExerciseSessionStatus.MEASURING;
            measurementStartedAt = startedAt;
        }
    }

    public void complete(int validCount, int invalidCount, long validDurationMs, LocalDateTime completedAt) {
        this.validCount = validCount;
        this.invalidCount = invalidCount;
        this.validDurationMs = validDurationMs;
        this.completedAt = completedAt;
        this.status = ExerciseSessionStatus.COMPLETED;
    }

    public void expire(int validCount, int invalidCount, long validDurationMs, LocalDateTime completedAt) {
        this.validCount = validCount;
        this.invalidCount = invalidCount;
        this.validDurationMs = validDurationMs;
        this.completedAt = completedAt;
        this.status = ExerciseSessionStatus.EXPIRED;
    }

    public boolean belongsTo(Long userId) {
        return user.getId().equals(userId);
    }

    public void discardMeasurement(LocalDateTime discardedAt) {
        this.status = ExerciseSessionStatus.EXPIRED;
        this.completedAt = discardedAt;
        softDelete();
    }
}
