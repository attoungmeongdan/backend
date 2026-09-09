package com.atmd.backend.domain.fitness.service;

import com.atmd.backend.domain.fitness.dto.response.MeasurementHistoryResponse;
import com.atmd.backend.domain.fitness.entity.ExerciseSession;
import com.atmd.backend.domain.fitness.enums.ExerciseSessionMode;
import com.atmd.backend.domain.fitness.enums.ExerciseSessionStatus;
import com.atmd.backend.domain.fitness.enums.ExerciseType;
import com.atmd.backend.domain.fitness.repository.ExerciseSessionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExerciseRecordServiceTest {

    @Test
    void sumsCountsAndPlankSecondsForTodayMeasurement() {
        ExerciseSessionRepository repository = mock(ExerciseSessionRepository.class);
        ExerciseRecordService service = new ExerciseRecordService(repository);
        String groupId = "today-group";
        LocalDateTime completedAt = LocalDateTime.of(
                LocalDate.now(ZoneId.of("Asia/Seoul")), LocalTime.NOON
        );

        when(repository.findRecentCompletedMeasurementGroupIds(
                eq(1L), eq(ExerciseSessionMode.MEASUREMENT),
                eq(ExerciseSessionStatus.COMPLETED), any(Pageable.class)
        )).thenReturn(List.of(groupId));
        List<ExerciseSession> completedSessions = List.of(
                session(groupId, ExerciseType.CHAIR_STAND, 24, 0, completedAt),
                session(groupId, ExerciseType.PUSH_UP, 18, 0, completedAt),
                session(groupId, ExerciseType.SIT_UP, 31, 0, completedAt),
                session(groupId, ExerciseType.PLANK, 0, 48_700, completedAt)
        );
        when(repository.findAllByUserIdAndMeasurementGroupIdInAndStatusAndIsDeletedFalse(
                eq(1L), eq(List.of(groupId)), eq(ExerciseSessionStatus.COMPLETED)
        )).thenReturn(completedSessions);

        MeasurementHistoryResponse response = service.getMeasurementHistory(1L);

        assertThat(response.today()).isNotNull();
        assertThat(response.today().totalScore()).isEqualTo(121.7);
        assertThat(response.today().exercises().get(ExerciseType.PLANK).value()).isEqualTo(48.7);
        assertThat(response.previousMeasurements()).isEmpty();
    }

    private ExerciseSession session(
            String groupId,
            ExerciseType type,
            int count,
            long durationMs,
            LocalDateTime completedAt
    ) {
        ExerciseSession session = mock(ExerciseSession.class);
        when(session.getMeasurementGroupId()).thenReturn(groupId);
        when(session.getExerciseType()).thenReturn(type);
        when(session.getValidCount()).thenReturn(count);
        when(session.getValidDurationMs()).thenReturn(durationMs);
        when(session.getCompletedAt()).thenReturn(completedAt);
        return session;
    }
}
