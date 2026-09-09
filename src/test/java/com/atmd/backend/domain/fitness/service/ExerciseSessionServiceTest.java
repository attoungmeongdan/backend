package com.atmd.backend.domain.fitness.service;

import com.atmd.backend.domain.fitness.analysis.ChairStandAnalyzer;
import com.atmd.backend.domain.fitness.analysis.PlankAnalyzer;
import com.atmd.backend.domain.fitness.analysis.PushUpAnalyzer;
import com.atmd.backend.domain.fitness.analysis.SitUpAnalyzer;
import com.atmd.backend.domain.fitness.entity.ExerciseSession;
import com.atmd.backend.domain.fitness.dto.request.ExerciseSessionCreateRequest;
import com.atmd.backend.domain.fitness.enums.ExerciseSessionMode;
import com.atmd.backend.domain.fitness.enums.ExerciseType;
import com.atmd.backend.domain.fitness.exception.FitnessErrorCode;
import com.atmd.backend.domain.fitness.pose.PoseFrameSmoother;
import com.atmd.backend.domain.fitness.pose.PoseFrameValidator;
import com.atmd.backend.domain.fitness.repository.ExerciseSessionRepository;
import com.atmd.backend.domain.user.repository.UserRepository;
import com.atmd.backend.domain.user.entity.User;
import com.atmd.backend.global.common.exception.GeneralException;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExerciseSessionServiceTest {

    @Test
    void newMeasurementMustStartWithChairStand() {
        ExerciseSessionRepository repository = mock(ExerciseSessionRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(mock(User.class)));
        when(repository.findAllByUserIdAndStatusIn(10L, List.of(
                com.atmd.backend.domain.fitness.enums.ExerciseSessionStatus.CREATED,
                com.atmd.backend.domain.fitness.enums.ExerciseSessionStatus.MEASURING
        ))).thenReturn(List.of());
        ExerciseSessionService service = service(repository, userRepository);

        assertThatThrownBy(() -> service.create(
                10L,
                new ExerciseSessionCreateRequest(ExerciseSessionMode.MEASUREMENT, ExerciseType.PUSH_UP, null)
        ))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorCode())
                .isEqualTo(FitnessErrorCode.INVALID_MEASUREMENT_ORDER);
    }

    @Test
    void measurementSessionCannotBeCompletedManually() {
        ExerciseSessionRepository repository = mock(ExerciseSessionRepository.class);
        ExerciseSession session = mock(ExerciseSession.class);
        when(repository.findById(1L)).thenReturn(Optional.of(session));
        when(session.belongsTo(10L)).thenReturn(true);
        when(session.getMode()).thenReturn(ExerciseSessionMode.MEASUREMENT);
        ExerciseSessionService service = service(repository, mock(UserRepository.class));

        assertThatThrownBy(() -> service.complete(10L, 1L))
                .isInstanceOf(GeneralException.class)
                .extracting(exception -> ((GeneralException) exception).getErrorCode())
                .isEqualTo(FitnessErrorCode.MEASUREMENT_MANUAL_COMPLETE_NOT_ALLOWED);
    }

    private ExerciseSessionService service(
            ExerciseSessionRepository repository,
            UserRepository userRepository
    ) {
        return new ExerciseSessionService(
                repository,
                userRepository,
                mock(PoseFrameValidator.class),
                mock(PoseFrameSmoother.class),
                mock(ChairStandAnalyzer.class),
                mock(PushUpAnalyzer.class),
                mock(SitUpAnalyzer.class),
                mock(PlankAnalyzer.class)
        );
    }
}
