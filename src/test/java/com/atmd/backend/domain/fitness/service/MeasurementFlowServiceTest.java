package com.atmd.backend.domain.fitness.service;

import com.atmd.backend.domain.fitness.dto.request.ExerciseSessionCreateRequest;
import com.atmd.backend.domain.fitness.dto.response.ExerciseSessionCreateResponse;
import com.atmd.backend.domain.fitness.dto.response.MeasurementProgressResponse;
import com.atmd.backend.domain.fitness.entity.ExerciseSession;
import com.atmd.backend.domain.fitness.enums.ExerciseSessionMode;
import com.atmd.backend.domain.fitness.enums.ExerciseSessionStatus;
import com.atmd.backend.domain.fitness.enums.ExerciseType;
import com.atmd.backend.domain.fitness.repository.ExerciseSessionRepository;
import com.atmd.backend.domain.user.entity.User;
import com.atmd.backend.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MeasurementFlowServiceTest {
    private final ExerciseSessionRepository repository = mock(ExerciseSessionRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final ExerciseSessionService sessionService = mock(ExerciseSessionService.class);
    private final MeasurementFlowService service = new MeasurementFlowService(repository, userRepository, sessionService);

    @Test
    void progressReturnsNextExerciseAfterChairStand() {
        ExerciseSession completedChairStand = session(
                ExerciseType.CHAIR_STAND, ExerciseSessionStatus.COMPLETED
        );
        when(repository.findAllByUserIdAndModeAndCreatedAtGreaterThanEqualAndCreatedAtLessThanAndIsDeletedFalseOrderByCreatedAtAsc(
                eq(1L), eq(ExerciseSessionMode.MEASUREMENT), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(List.of(completedChairStand));

        MeasurementProgressResponse response = service.getTodayProgress(1L);

        assertThat(response.measurementGroupId()).isEqualTo("group-id");
        assertThat(response.completedExercises()).containsExactly(ExerciseType.CHAIR_STAND);
        assertThat(response.nextExerciseType()).isEqualTo(ExerciseType.PUSH_UP);
    }

    @Test
    void resumeRetriesExpiredExerciseInSameMeasurementGroup() {
        ExerciseSession expiredChairStand = session(
                ExerciseType.CHAIR_STAND, ExerciseSessionStatus.EXPIRED
        );
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(mock(User.class)));
        when(repository.findAllByUserIdAndModeAndCreatedAtGreaterThanEqualAndCreatedAtLessThanAndIsDeletedFalseOrderByCreatedAtAsc(
                eq(1L), eq(ExerciseSessionMode.MEASUREMENT), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(List.of(expiredChairStand));
        when(sessionService.create(eq(1L), any(ExerciseSessionCreateRequest.class)))
                .thenReturn(mock(ExerciseSessionCreateResponse.class));

        service.resume(1L);

        ArgumentCaptor<ExerciseSessionCreateRequest> requestCaptor =
                ArgumentCaptor.forClass(ExerciseSessionCreateRequest.class);
        verify(sessionService).create(eq(1L), requestCaptor.capture());
        assertThat(requestCaptor.getValue().mode()).isEqualTo(ExerciseSessionMode.MEASUREMENT);
        assertThat(requestCaptor.getValue().exerciseType()).isEqualTo(ExerciseType.CHAIR_STAND);
        assertThat(requestCaptor.getValue().measurementGroupId()).isEqualTo("group-id");
    }

    @Test
    void restartDiscardsPreviousGroupAndCreatesChairStand() {
        ExerciseSession previous = session(ExerciseType.CHAIR_STAND, ExerciseSessionStatus.COMPLETED);
        when(userRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(mock(User.class)));
        when(repository.findAllByUserIdAndModeAndCreatedAtGreaterThanEqualAndCreatedAtLessThanAndIsDeletedFalseOrderByCreatedAtAsc(
                eq(1L), eq(ExerciseSessionMode.MEASUREMENT), any(LocalDateTime.class), any(LocalDateTime.class)
        )).thenReturn(List.of(previous));
        when(sessionService.create(eq(1L), any(ExerciseSessionCreateRequest.class)))
                .thenReturn(mock(ExerciseSessionCreateResponse.class));

        service.restart(1L);

        verify(previous).discardMeasurement(any(LocalDateTime.class));
        verify(repository).flush();
        ArgumentCaptor<ExerciseSessionCreateRequest> requestCaptor =
                ArgumentCaptor.forClass(ExerciseSessionCreateRequest.class);
        verify(sessionService).create(eq(1L), requestCaptor.capture());
        assertThat(requestCaptor.getValue().mode()).isEqualTo(ExerciseSessionMode.MEASUREMENT);
        assertThat(requestCaptor.getValue().exerciseType()).isEqualTo(ExerciseType.CHAIR_STAND);
        assertThat(requestCaptor.getValue().measurementGroupId()).isNull();
    }

    private ExerciseSession session(ExerciseType exerciseType, ExerciseSessionStatus status) {
        ExerciseSession session = mock(ExerciseSession.class);
        when(session.getMeasurementGroupId()).thenReturn("group-id");
        when(session.getExerciseType()).thenReturn(exerciseType);
        when(session.getStatus()).thenReturn(status);
        return session;
    }
}
