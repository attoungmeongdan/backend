package com.atmd.backend.domain.group.entity;

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

import java.time.LocalDate;

@Getter
@Entity
@Table(
        name = "group_daily_workout_records",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_group_daily_workout_group_user_date",
                columnNames = {"group_id", "user_id", "workout_date"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupDailyWorkoutRecord extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "workout_date", nullable = false)
    private LocalDate workoutDate;

    @Column(name = "chair_stand_count", nullable = false)
    private long chairStandCount;

    @Column(name = "push_up_count", nullable = false)
    private long pushUpCount;

    @Column(name = "sit_up_count", nullable = false)
    private long sitUpCount;

    @Column(name = "plank_duration_ms", nullable = false)
    private long plankDurationMs;
}
