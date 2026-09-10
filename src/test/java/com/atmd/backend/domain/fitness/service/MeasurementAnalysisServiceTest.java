package com.atmd.backend.domain.fitness.service;

import com.atmd.backend.domain.fitness.dto.response.MeasurementAnalysisResponse;
import com.atmd.backend.domain.fitness.entity.ExerciseSession;
import com.atmd.backend.domain.fitness.enums.ExerciseSessionMode;
import com.atmd.backend.domain.fitness.enums.ExerciseSessionStatus;
import com.atmd.backend.domain.fitness.enums.ExerciseType;
import com.atmd.backend.domain.fitness.repository.ExerciseSessionRepository;
import com.atmd.backend.domain.user.entity.User;
import com.atmd.backend.domain.user.entity.enums.Gender;
import com.atmd.backend.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MeasurementAnalysisServiceTest {

    @Test
    void comparesExercisesAndWithholdsPercentileWhenCohortIsTooSmall() {
        UserRepository userRepository = mock(UserRepository.class);
        ExerciseSessionRepository sessionRepository = mock(ExerciseSessionRepository.class);
        ExerciseStandardService standardService = mock(ExerciseStandardService.class);
        MeasurementAnalysisService service = new MeasurementAnalysisService(
                userRepository, sessionRepository, standardService
        );
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(user.getAge()).thenReturn(35);
        when(user.getGender()).thenReturn(Gender.FEMALE);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(standardService.getAverage(any(ExerciseType.class), eq(Gender.FEMALE), anyInt()))
                .thenReturn(10.0);

        String groupId = "analysis-group";
        LocalDateTime completedAt = LocalDateTime.of(2026, 9, 11, 12, 0);
        List<ExerciseSession> completedSessions = List.of(
                session(ExerciseType.CHAIR_STAND, 8, 0, completedAt),
                session(ExerciseType.SIT_UP, 10, 0, completedAt),
                session(ExerciseType.PUSH_UP, 12, 0, completedAt),
                session(ExerciseType.PLANK, 0, 10000, completedAt)
        );
        when(sessionRepository.findAllByUserIdAndMeasurementGroupIdAndIsDeletedFalse(1L, groupId))
                .thenReturn(completedSessions);
        when(sessionRepository.findCompletedMeasurementsForCohort(
                eq(ExerciseSessionMode.MEASUREMENT),
                eq(ExerciseSessionStatus.COMPLETED),
                eq(Gender.FEMALE),
                anyInt(),
                anyInt()
        )).thenReturn(List.of());

        MeasurementAnalysisResponse response = service.getAnalysis(1L, groupId);

        assertThat(response.exerciseComparisons())
                .extracting(comparison -> comparison.level())
                .containsExactly("LOW", "SIMILAR", "HIGH", "SIMILAR");
        assertThat(response.overallScore()).isEqualTo(100.0);
        assertThat(response.percentile().available()).isFalse();
        assertThat(response.percentile().sampleSize()).isEqualTo(1);
        assertThat(response.percentile().comparisonGender()).isEqualTo(Gender.FEMALE);
        assertThat(response.percentile().comparisonAgeGroup()).isEqualTo("30대");
        assertThat(response.percentile().userScore()).isEqualTo(100.0);
        assertThat(response.percentile().userBucketIndex()).isNull();
        assertThat(response.percentile().buckets()).isEmpty();
        assertThat(response.performanceGroupComparisons()).hasSize(14);
        assertThat(response.fitnessPerformance().label()).containsAnyOf("남성", "여성");
    }

    private ExerciseSession session(
            ExerciseType type, int count, long durationMs, LocalDateTime completedAt
    ) {
        ExerciseSession session = mock(ExerciseSession.class);
        when(session.getExerciseType()).thenReturn(type);
        when(session.getValidCount()).thenReturn(count);
        when(session.getValidDurationMs()).thenReturn(durationMs);
        when(session.getCompletedAt()).thenReturn(completedAt);
        when(session.getMode()).thenReturn(ExerciseSessionMode.MEASUREMENT);
        when(session.getStatus()).thenReturn(ExerciseSessionStatus.COMPLETED);
        return session;
    }
}
