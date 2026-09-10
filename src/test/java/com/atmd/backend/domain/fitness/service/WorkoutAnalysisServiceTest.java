package com.atmd.backend.domain.fitness.service;

import com.atmd.backend.domain.fitness.dto.response.WorkoutSessionAnalysisResponse;
import com.atmd.backend.domain.fitness.entity.ExerciseSession;
import com.atmd.backend.domain.fitness.enums.ExerciseSessionMode;
import com.atmd.backend.domain.fitness.enums.ExerciseSessionStatus;
import com.atmd.backend.domain.fitness.enums.ExerciseType;
import com.atmd.backend.domain.fitness.repository.ExerciseSessionRepository;
import com.atmd.backend.domain.user.entity.User;
import com.atmd.backend.domain.user.entity.enums.Gender;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WorkoutAnalysisServiceTest {

    @Test
    void analyzesTheExactCompletedWorkoutSession() {
        ExerciseSessionRepository repository = mock(ExerciseSessionRepository.class);
        ExerciseStandardService standardService = mock(ExerciseStandardService.class);
        WorkoutAnalysisService service = new WorkoutAnalysisService(repository, standardService);
        User user = mock(User.class);
        ExerciseSession session = mock(ExerciseSession.class);
        LocalDateTime completedAt = LocalDateTime.of(2026, 9, 11, 18, 0);

        when(repository.findById(10L)).thenReturn(Optional.of(session));
        when(session.belongsTo(1L)).thenReturn(true);
        when(session.getMode()).thenReturn(ExerciseSessionMode.WORKOUT);
        when(session.getStatus()).thenReturn(ExerciseSessionStatus.COMPLETED);
        when(session.getUser()).thenReturn(user);
        when(session.getId()).thenReturn(10L);
        when(session.getExerciseType()).thenReturn(ExerciseType.PUSH_UP);
        when(session.getValidCount()).thenReturn(18);
        when(session.getCompletedAt()).thenReturn(completedAt);
        when(user.getAge()).thenReturn(35);
        when(user.getGender()).thenReturn(Gender.FEMALE);
        when(standardService.getAverage(ExerciseType.PUSH_UP, Gender.FEMALE, 35))
                .thenReturn(14.0);

        WorkoutSessionAnalysisResponse response = service.getAnalysis(1L, 10L);

        assertThat(response.sessionId()).isEqualTo(10L);
        assertThat(response.measuredValue()).isEqualTo(18.0);
        assertThat(response.averageValue()).isEqualTo(14.0);
        assertThat(response.unit()).isEqualTo("COUNT");
        assertThat(response.achievementRate()).isEqualTo(128.6);
        assertThat(response.comparison()).isEqualTo("HIGH");
    }
}
