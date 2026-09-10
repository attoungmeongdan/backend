package com.atmd.backend.domain.calendar.service;

import com.atmd.backend.domain.calendar.dto.response.CalendarResponseDTO;
import com.atmd.backend.domain.calendar.dto.response.RecentExerciseStatusDTO;
import com.atmd.backend.domain.calendar.entity.Calendar;
import com.atmd.backend.domain.calendar.exception.CalendarErrorCode;
import com.atmd.backend.domain.calendar.repository.CalendarRepository;
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
    private CalendarRepository calendarRepository;

    @Nested
    @DisplayName("월별 캘린더 조회 테스트")
    class GetMonthlyCalendarTest {

        @Test
        @DisplayName("정상적인 연월 요청 시 캘린더 데이터와 달성률을 정상 반환한다")
        void getMonthlyCalendar_Success() {
            // given
            Long userId = 1L;
            int year = 2026;
            int month = 9;

            User mockUser = User.builder()
                    .email("test@example.com")
                    .nickname("테스터")
                    .build();

            Calendar record1 = Calendar.builder()
                    .user(mockUser)
                    .exerciseDate(LocalDate.of(2026, 9, 1))
                    .isCompleted(true)
                    .build();

            Calendar record2 = Calendar.builder()
                    .user(mockUser)
                    .exerciseDate(LocalDate.of(2026, 9, 2))
                    .isCompleted(true)
                    .build();

            given(calendarRepository.findByUserIdAndExerciseDateBetweenAndIsDeletedFalse(
                    eq(userId), any(LocalDate.class), any(LocalDate.class)))
                    .willReturn(List.of(record1, record2));

            // when
            CalendarResponseDTO result = calendarService.getMonthlyCalendar(userId, year, month);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getYear()).isEqualTo(2026);
            assertThat(result.getMonth()).isEqualTo(9);
            assertThat(result.getCompletedDays()).isEqualTo(2);
            assertThat(result.getDailyRecords()).hasSize(2);
        }

        @Test
        @DisplayName("유효하지 않은 월(13월) 입력 시 INVALID_YEAR_MONTH 예외가 발생한다")
        void getMonthlyCalendar_InvalidMonth_ThrowsException() {
            // given
            Long userId = 1L;
            int invalidMonth = 13;

            // when & then
            assertThatThrownBy(() -> calendarService.getMonthlyCalendar(userId, 2026, invalidMonth))
                    .isInstanceOf(GeneralException.class)
                    .extracting("errorCode")
                    .isEqualTo(CalendarErrorCode.INVALID_YEAR_MONTH);
        }
        @Test
        @DisplayName("최근 7일 운동 완료 상태를 정상 반환한다")
        void getRecentSevenDaysStatus_Success() {
            // given
            Long userId = 1L;

            LocalDate today = LocalDate.now();
            LocalDate startDate = today.minusDays(6);

            LocalDate completedDate1 = startDate;
            LocalDate completedDate2 = startDate.plusDays(2);
            LocalDate completedDate3 = today;

            given(calendarRepository.findCompletedDatesByUserIdAndDateRange(
                    eq(userId),
                    eq(startDate),
                    eq(today)
            )).willReturn(List.of(
                    completedDate1,
                    completedDate2,
                    completedDate3
            ));

            // when
            List<RecentExerciseStatusDTO> result =
                    calendarService.getRecentSevenDaysStatus(userId);

            // then
            assertThat(result).hasSize(7);

            assertThat(result.get(0).date()).isEqualTo(startDate);
            assertThat(result.get(0).isCompleted()).isTrue();

            assertThat(result.get(1).date()).isEqualTo(startDate.plusDays(1));
            assertThat(result.get(1).isCompleted()).isFalse();

            assertThat(result.get(2).date()).isEqualTo(completedDate2);
            assertThat(result.get(2).isCompleted()).isTrue();

            assertThat(result.get(6).date()).isEqualTo(today);
            assertThat(result.get(6).isCompleted()).isTrue();
        }
    }
}