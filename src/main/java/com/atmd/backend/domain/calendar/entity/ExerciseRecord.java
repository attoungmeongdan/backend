package com.atmd.backend.domain.calendar.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "exercise_record",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_date",
                        columnNames = {"user_id", "exercise_date"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ExerciseRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "exercise_date", nullable = false)
    private LocalDate exerciseDate;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public ExerciseRecord(Long userId, LocalDate exerciseDate, Boolean isCompleted) {
        this.userId = userId;
        this.exerciseDate = exerciseDate;
        this.isCompleted = isCompleted != null ? isCompleted : true;
    }
}