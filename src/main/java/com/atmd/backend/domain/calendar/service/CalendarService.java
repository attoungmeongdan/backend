package com.atmd.backend.domain.calendar.service;

import com.atmd.backend.domain.calendar.dto.response.CalendarResponseDTO;
import com.atmd.backend.domain.calendar.dto.response.CalendarResponseDTO;
import com.atmd.backend.domain.calendar.entity.ExerciseRecord;
import com.atmd.backend.domain.calendar.repository.ExerciseRecordRepository;
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

    private final ExerciseRecordRepository exerciseRecordRepository;

    public CalendarResponseDTO getMonthlyCalendar(Long userId, int year, int month) {
        // 1. 해당 연월의 시작일과 마지막 일자 계산
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // 2. DB에서 해당 월의 기록 조회
        List<ExerciseRecord> records = exerciseRecordRepository
                .findByUserIdAndExerciseDateBetween(userId, startDate, endDate);

        // 3. 완료한 일수 계산
        int completedDays = (int) records.stream()
                .filter(ExerciseRecord::getIsCompleted)
                .count();

        // 4. 당월 총 일수 (목표 일수) 및 달성률 계산
        int totalTargetDays = yearMonth.lengthOfMonth();
        int achievementRate = (int) Math.round(((double) completedDays / totalTargetDays) * 100);

        // 5. 일별 기록 DTO 변환
        List<CalendarResponseDTO.DailyRecord> dailyRecords = records.stream()
                .map(record -> CalendarResponseDTO.DailyRecord.builder()
                        .date(record.getExerciseDate())
                        .isCompleted(record.getIsCompleted())
                        .build())
                .collect(Collectors.toList());

        // 6. 최종 응답 객체 반환
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