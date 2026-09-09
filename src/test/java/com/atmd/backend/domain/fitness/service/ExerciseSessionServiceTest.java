package com.atmd.backend.domain.fitness.service;

import com.atmd.backend.domain.fitness.analysis.ChairStandAnalyzer;
import com.atmd.backend.domain.fitness.analysis.PlankAnalyzer;
import com.atmd.backend.domain.fitness.analysis.PushUpAnalyzer;
import com.atmd.backend.domain.fitness.analysis.SitUpAnalyzer;
import com.atmd.backend.domain.fitness.entity.ExerciseSession;
import com.atmd.backend.domain.fitness.enums.ExerciseSessionMode;
import com.atmd.backend.domain.fitness.exception.FitnessErrorCode;
import com.atmd.backend.domain.fitness.pose.PoseFrameSmoother;
import com.atmd.backend.domain.fitness.pose.PoseFrameValidator;
import com.atmd.backend.domain.fitness.repository.ExerciseSessionRepository;
import com.atmd.backend.domain.user.repository.UserRepository;
import com.atmd.backend.global.common.exception.GeneralException;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExerciseSessionServiceTest {

    @Test
    void measurementSessionCannotBeCompletedManually() {
        ExerciseSessionRepository repository = mock(ExerciseSessionRepository.class);
        ExerciseSession session = mock(ExerciseSession.class);
        when(repository.findById(1L)).thenReturn(Optional.of(session));
        when(session.belongsTo(10L)).thenReturn(true);
        when(session.getMode()).thenReturn(ExerciseSessionMode.MEASUREMENT);
        ExerciseSessionService service = new ExerciseSessionService(
                repository,
                mock(UserRepository.class),
                mock(PoseFrameValidator.class),
                mock(PoseFrameSmoother.class),
                mock(ChairStandAnalyzer.class),
                mock(PushUpAnalyzer.class),
                mock(SitUpAnalyzer.class),
                mock(PlankAnalyzer.class)
        );

        assertThatThrownBy(() -> service.complete(10L, 1L))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorCode())
                .isEqualTo(FitnessErrorCode.MEASUREMENT_MANUAL_COMPLETE_NOT_ALLOWED);
    }
}
