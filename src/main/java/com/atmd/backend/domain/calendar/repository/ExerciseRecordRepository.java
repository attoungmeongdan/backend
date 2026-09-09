package com.atmd.backend.domain.calendar.repository;

import com.atmd.backend.domain.calendar.entity.ExerciseRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExerciseRecordRepository extends JpaRepository<ExerciseRecord, Long> {

    // 특정 사용자의, 특정 기간(해당 월의 1일 ~ 말일) 내의 기록 조회
    List<ExerciseRecord> findByUserIdAndExerciseDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
}