package com.atmd.backend.domain.calendar.repository;

import com.atmd.backend.domain.calendar.entity.Calendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CalendarRepository extends JpaRepository<Calendar, Long> {

    List<Calendar> findByUserIdAndExerciseDateBetweenAndIsDeletedFalse(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );

    // 특정 기간 동안 유저가 운동을 완료한 날짜 목록 조회
    @Query("""
            SELECT c.exerciseDate
            FROM Calendar c
            WHERE c.user.id = :userId
              AND c.exerciseDate BETWEEN :startDate AND :endDate
              AND c.isCompleted = true
              AND c.isDeleted = false
            """)
    List<LocalDate> findCompletedDatesByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}