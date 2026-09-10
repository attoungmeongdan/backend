package com.atmd.backend.domain.calendar.service;

import com.atmd.backend.domain.calendar.dto.response.CalendarResponseDTO;
import com.atmd.backend.domain.calendar.entity.Calendar;
import com.atmd.backend.domain.calendar.exception.CalendarErrorCode;
import com.atmd.backend.domain.calendar.repository.CalendarRepository;
import com.atmd.backend.global.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarService {

    private final CalendarRepository calendarRepository;

    public CalendarResponseDTO getMonthlyCalendar(Long userId, int year, int month) {
        if (month < 1 || month > 12 || year < 1900) {
            throw new GeneralException(CalendarErrorCode.INVALID_YEAR_MONTH);
        }

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Calendar> records = calendarRepository
                .findByUserIdAndExerciseDateBetweenAndIsDeletedFalse(userId, startDate, endDate);

        int completedDays = (int) records.stream()
                .filter(Calendar::getIsCompleted)
                .count();

        LocalDate today = LocalDate.now();
        int totalTargetDays;
        if (year == today.getYear() && month == today.getMonthValue()) {
            totalTargetDays = today.getDayOfMonth();
        } else {
            totalTargetDays = yearMonth.lengthOfMonth();
        }

        int achievementRate = totalTargetDays == 0 ? 0 : (int) Math.round(((double) completedDays / totalTargetDays) * 100);

        List<CalendarResponseDTO.DailyRecord> dailyRecords = records.stream()
                .map(record -> CalendarResponseDTO.DailyRecord.builder()
                        .date(record.getExerciseDate())
                        .isCompleted(record.getIsCompleted())
                        .build())
                .collect(Collectors.toList());

        return CalendarResponseDTO.builder()
                .year(year)
                .month(month)
                .totalTargetDays(totalTargetDays)
                .completedDays(completedDays)
                .achievementRate(achievementRate)
                .dailyRecords(dailyRecords)
                .build();
    }
}