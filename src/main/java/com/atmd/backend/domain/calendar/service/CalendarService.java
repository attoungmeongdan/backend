package com.atmd.backend.domain.calendar.service;

import com.atmd.backend.domain.calendar.dto.response.CalendarResponseDTO;
import com.atmd.backend.domain.calendar.dto.response.RecentExerciseStatusDTO;
import com.atmd.backend.domain.calendar.exception.CalendarErrorCode;
import com.atmd.backend.domain.fitness.entity.ExerciseSession;
import com.atmd.backend.domain.fitness.enums.ExerciseSessionMode;
import com.atmd.backend.domain.fitness.enums.ExerciseSessionStatus;
import com.atmd.backend.domain.fitness.enums.ExerciseType;
import com.atmd.backend.domain.fitness.repository.ExerciseSessionRepository;
import com.atmd.backend.global.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarService {

    private final ExerciseSessionRepository exerciseSessionRepository;
    private final Clock clock;

    public CalendarResponseDTO getMonthlyCalendar(Long userId, int year, int month) {

        if (month < 1 || month > 12 || year < 1900 || year > 9999) {
            throw new GeneralException(CalendarErrorCode.INVALID_YEAR_MONTH);
        }

        YearMonth yearMonth = YearMonth.of(year, month);

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        LocalDate today = LocalDate.now(clock);

        boolean isCurrentMonth =
                year == today.getYear() &&
                        month == today.getMonthValue();

        int totalTargetDays = isCurrentMonth
                ? today.getDayOfMonth()
                : yearMonth.lengthOfMonth();

        // 해당 월의 WORKOUT 완료 세션 조회
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        List<ExerciseSession> sessions =
                exerciseSessionRepository
                        .findAllByUserIdAndModeAndStatusAndCompletedAtGreaterThanEqualAndCompletedAtLessThanAndIsDeletedFalse(
                                userId,
                                ExerciseSessionMode.WORKOUT,
                                ExerciseSessionStatus.COMPLETED,
                                startDateTime,
                                endDateTime
                        );

        // 날짜별 서로 다른 운동 종류 개수 계산
        Map<LocalDate, Set<ExerciseType>> exercisesByDate = new HashMap<>();

        for (ExerciseSession session : sessions) {

            LocalDate exerciseDate = session.getCompletedAt().toLocalDate();

            // 당월인 경우 오늘 이후의 데이터는 캘린더 달성률에 포함하지 않음
            if (isCurrentMonth && exerciseDate.isAfter(today)) {
                continue;
            }

            exercisesByDate
                    .computeIfAbsent(exerciseDate, key -> new HashSet<>())
                    .add(session.getExerciseType());
        }

        // 날짜별 운동 종류 수
        Map<LocalDate, Integer> exerciseCountByDate = new HashMap<>();

        exercisesByDate.forEach(
                (date, exerciseTypes) ->
                        exerciseCountByDate.put(date, exerciseTypes.size())
        );

        // 운동을 한 번이라도 한 날짜 수
        int completedDays = (int) exerciseCountByDate.values().stream()
                .filter(count -> count > 0)
                .count();

        int achievementRate =
                totalTargetDays == 0
                        ? 0
                        : (int) Math.round(
                        ((double) completedDays / totalTargetDays) * 100
                );

        // 해당 월의 모든 날짜를 반환
        List<CalendarResponseDTO.DailyRecord> dailyRecords = new ArrayList<>();

        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate date = yearMonth.atDay(day);
            int exerciseCount = exerciseCountByDate.getOrDefault(date, 0);

            dailyRecords.add(
                    CalendarResponseDTO.DailyRecord.builder()
                            .date(date)
                            .exerciseCount(exerciseCount)
                            .build()
            );
        }

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

        LocalDate today = LocalDate.now(clock);
        LocalDate startDate = today.minusDays(6);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = today.plusDays(1).atStartOfDay();

        List<ExerciseSession> sessions =
                exerciseSessionRepository
                        .findAllByUserIdAndModeAndStatusAndCompletedAtGreaterThanEqualAndCompletedAtLessThanAndIsDeletedFalse(
                                userId,
                                ExerciseSessionMode.WORKOUT,
                                ExerciseSessionStatus.COMPLETED,
                                startDateTime,
                                endDateTime
                        );

        Set<LocalDate> completedDateSet = sessions.stream()
                .map(ExerciseSession::getCompletedAt)
                .map(LocalDateTime::toLocalDate)
                .collect(Collectors.toSet());

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