package com.atmd.backend.domain.calendar.repository;

import com.atmd.backend.domain.calendar.entity.Calendar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CalendarRepository extends JpaRepository<Calendar, Long> {

    List<Calendar> findByUserIdAndExerciseDateBetweenAndIsDeletedFalse(
            Long userId, LocalDate startDate, LocalDate endDate);
}