package com.atmd.backend.domain.calendar.service;

import com.atmd.backend.domain.calendar.dto.response.CalendarResponseDTO;
import com.atmd.backend.domain.calendar.dto.response.RecentExerciseStatusDTO;
import com.atmd.backend.domain.calendar.entity.Calendar;
import com.atmd.backend.domain.calendar.exception.CalendarErrorCode;
import com.atmd.backend.domain.calendar.repository.CalendarRepository;
import com.atmd.backend.global.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarService {

    private final CalendarRepository calendarRepository;

    public CalendarResponseDTO getMonthlyCalendar(Long userId, int year, int month) {
        // 1. 월 범위 및 연도 상한/하한 유효성 검증 (Fix: year 상한 검증 추가)
        if (month < 1 || month > 12 || year < 1900 || year > 9999) {
            throw new GeneralException(CalendarErrorCode.INVALID_YEAR_MONTH);
        }

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Calendar> records = calendarRepository
                .findByUserIdAndExerciseDateBetweenAndIsDeletedFalse(userId, startDate, endDate);

        LocalDate today = LocalDate.now();
        int totalTargetDays;
        boolean isCurrentMonth = (year == today.getYear() && month == today.getMonthValue());

        if (isCurrentMonth) {
            totalTargetDays = today.getDayOfMonth();
        } else {
            totalTargetDays = yearMonth.lengthOfMonth();
        }

        // 2. 완료 일수 계산 (Fix: 달성률 초과 방지를 위해 미래 날짜 완료 기록 제외)
        int completedDays = (int) records.stream()
                .filter(Calendar::getIsCompleted)
                .filter(record -> {
                    // 당월인 경우 오늘 이하의 기록만 완료 일수에 포함
                    if (isCurrentMonth) {
                        return !record.getExerciseDate().isAfter(today);
                    }
                    return true;
                })
                .count();

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

    public List<RecentExerciseStatusDTO> getRecentSevenDaysStatus(Long userId) {

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6);

        List<LocalDate> completedDates =
                calendarRepository.findCompletedDatesByUserIdAndDateRange(
                        userId,
                        startDate,
                        today
                );

        Set<LocalDate> completedDateSet = new HashSet<>(completedDates);

        List<RecentExerciseStatusDTO> result = new ArrayList<>();

        for (int i = 0; i < 7; i++) {

            LocalDate currentDate = startDate.plusDays(i);

            String dayOfWeek = currentDate.getDayOfWeek()
                    .getDisplayName(TextStyle.SHORT, Locale.KOREAN);

            boolean isCompleted = completedDateSet.contains(currentDate);

            result.add(
                    new RecentExerciseStatusDTO(
                            currentDate,
                            dayOfWeek,
                            isCompleted
                    )
            );
        }

        return result;
    }
}