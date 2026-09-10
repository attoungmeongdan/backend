package com.atmd.backend.domain.calendar.entity;

import com.atmd.backend.domain.user.entity.User;
import com.atmd.backend.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(
        name = "calendar",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_date",
                        columnNames = {"user_id", "exercise_date"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Calendar extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "calendar_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "exercise_date", nullable = false)
    private LocalDate exerciseDate;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted;

    @Builder
    public Calendar(User user, LocalDate exerciseDate, Boolean isCompleted) {
        this.user = user;
        this.exerciseDate = exerciseDate;
        this.isCompleted = isCompleted != null ? isCompleted : true;
    }
}