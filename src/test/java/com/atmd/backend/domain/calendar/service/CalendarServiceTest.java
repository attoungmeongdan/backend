package com.atmd.backend.domain.calendar.service;

import com.atmd.backend.domain.calendar.dto.response.CalendarResponseDTO;
import com.atmd.backend.domain.calendar.dto.response.RecentExerciseStatusDTO;
import com.atmd.backend.domain.calendar.exception.CalendarErrorCode;
import com.atmd.backend.domain.fitness.entity.ExerciseSession;
import com.atmd.backend.domain.fitness.enums.ExerciseSessionMode;
import com.atmd.backend.domain.fitness.enums.ExerciseSessionStatus;
import com.atmd.backend.domain.fitness.enums.ExerciseType;
import com.atmd.backend.domain.fitness.repository.ExerciseSessionRepository;
import com.atmd.backend.domain.user.entity.User;
import com.atmd.backend.global.common.exception.GeneralException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CalendarServiceTest {

    @InjectMocks
    private CalendarService calendarService;

    @Mock
    private ExerciseSessionRepository exerciseSessionRepository;

    @Nested
    @DisplayName("월별 캘린더 조회 테스트")
    class GetMonthlyCalendarTest {

        @Test
        @DisplayName("정상적인 연월 요청 시 운동 종류 수와 달성률을 정상 반환한다")
        void getMonthlyCalendar_Success() {
            // given
            Long userId = 1L;
            int year = 2026;
            int month = 9;

            User mockUser = User.builder()
                    .email("test@example.com")
                    .nickname("테스터")
                    .build();

            ExerciseSession record1 = ExerciseSession.create(
                    mockUser,
                    ExerciseSessionMode.WORKOUT,
                    ExerciseType.PUSH_UP,
                    null
            );
            record1.complete(
                    10,
                    0,
                    0,
                    LocalDateTime.of(2026, 9, 1, 10, 0)
            );

            ExerciseSession record2 = ExerciseSession.create(
                    mockUser,
                    ExerciseSessionMode.WORKOUT,
                    ExerciseType.PLANK,
                    null
            );
            record2.complete(
                    0,
                    0,
                    30000,
                    LocalDateTime.of(2026, 9, 1, 11, 0)
            );

            given(exerciseSessionRepository
                    .findAllByUserIdAndModeAndStatusAndCompletedAtGreaterThanEqualAndCompletedAtLessThanAndIsDeletedFalse(
                            eq(userId),
                            eq(ExerciseSessionMode.WORKOUT),
                            eq(ExerciseSessionStatus.COMPLETED),
                            any(LocalDateTime.class),
                            any(LocalDateTime.class)
                    ))
                    .willReturn(List.of(record1, record2));

            // when
            CalendarResponseDTO result =
                    calendarService.getMonthlyCalendar(userId, year, month);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getYear()).isEqualTo(2026);
            assertThat(result.getMonth()).isEqualTo(9);

            assertThat(result.getDailyRecords()).hasSize(10);
            assertThat(result.getDailyRecords().get(0).getDate())
                    .isEqualTo(LocalDate.of(2026, 9, 1));
            assertThat(result.getDailyRecords().get(0).getExerciseCount())
                    .isEqualTo(2);

            // 운동을 한 날짜는 1일
            assertThat(result.getCompletedDays()).isEqualTo(1);
        }

        @Test
        @DisplayName("유효하지 않은 월(13월) 입력 시 INVALID_YEAR_MONTH 예외가 발생한다")
        void getMonthlyCalendar_InvalidMonth_ThrowsException() {
            // given
            Long userId = 1L;
            int invalidMonth = 13;

            // when & then
            assertThatThrownBy(() ->
                    calendarService.getMonthlyCalendar(
                            userId,
                            2026,
                            invalidMonth
                    )
            )
                    .isInstanceOf(GeneralException.class)
                    .extracting("errorCode")
                    .isEqualTo(CalendarErrorCode.INVALID_YEAR_MONTH);
        }
    }

    @Nested
    @DisplayName("최근 7일 운동 상태 조회 테스트")
    class GetRecentSevenDaysStatusTest {

        @Test
        @DisplayName("최근 7일 운동 완료 상태를 정상 반환한다")
        void getRecentSevenDaysStatus_Success() {
            // given
            Long userId = 1L;

            LocalDate today = LocalDate.now();
            LocalDate startDate = today.minusDays(6);

            User mockUser = User.builder()
                    .email("test@example.com")
                    .nickname("테스터")
                    .build();

            ExerciseSession session1 = ExerciseSession.create(
                    mockUser,
                    ExerciseSessionMode.WORKOUT,
                    ExerciseType.PUSH_UP,
                    null
            );
            session1.complete(
                    10,
                    0,
                    0,
                    startDate.atTime(10, 0)
            );

            ExerciseSession session2 = ExerciseSession.create(
                    mockUser,
                    ExerciseSessionMode.WORKOUT,
                    ExerciseType.PLANK,
                    null
            );
            session2.complete(
                    0,
                    0,
                    30000,
                    startDate.plusDays(2).atTime(11, 0)
            );

            ExerciseSession session3 = ExerciseSession.create(
                    mockUser,
                    ExerciseSessionMode.WORKOUT,
                    ExerciseType.SIT_UP,
                    null
            );
            session3.complete(
                    15,
                    0,
                    0,
                    today.atTime(12, 0)
            );

            given(exerciseSessionRepository
                    .findAllByUserIdAndModeAndStatusAndCompletedAtGreaterThanEqualAndCompletedAtLessThanAndIsDeletedFalse(
                            eq(userId),
                            eq(ExerciseSessionMode.WORKOUT),
                            eq(ExerciseSessionStatus.COMPLETED),
                            eq(startDate.atStartOfDay()),
                            eq(today.plusDays(1).atStartOfDay())
                    ))
                    .willReturn(List.of(session1, session2, session3));

            // when
            List<RecentExerciseStatusDTO> result =
                    calendarService.getRecentSevenDaysStatus(userId);

            // then
            assertThat(result).hasSize(7);

            assertThat(result.get(0).date()).isEqualTo(startDate);
            assertThat(result.get(0).isCompleted()).isTrue();

            assertThat(result.get(1).date())
                    .isEqualTo(startDate.plusDays(1));
            assertThat(result.get(1).isCompleted()).isFalse();

            assertThat(result.get(2).date())
                    .isEqualTo(startDate.plusDays(2));
            assertThat(result.get(2).isCompleted()).isTrue();

            assertThat(result.get(6).date()).isEqualTo(today);
            assertThat(result.get(6).isCompleted()).isTrue();
        }
    }
}