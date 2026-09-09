package com.atmd.backend.domain.fitness.service;

import com.atmd.backend.domain.fitness.dto.request.ExerciseSessionCreateRequest;
import com.atmd.backend.domain.fitness.dto.response.ExerciseSessionCreateResponse;
import com.atmd.backend.domain.fitness.dto.response.MeasurementProgressResponse;
import com.atmd.backend.domain.fitness.entity.ExerciseSession;
import com.atmd.backend.domain.fitness.enums.ExerciseSessionMode;
import com.atmd.backend.domain.fitness.enums.ExerciseSessionStatus;
import com.atmd.backend.domain.fitness.enums.ExerciseType;
import com.atmd.backend.domain.fitness.exception.FitnessErrorCode;
import com.atmd.backend.domain.fitness.repository.ExerciseSessionRepository;
import com.atmd.backend.domain.user.repository.UserRepository;
import com.atmd.backend.global.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeasurementFlowService {
    private static final ZoneId SERVER_ZONE = ZoneId.of("Asia/Seoul");
    private final ExerciseSessionRepository exerciseSessionRepository;
    private final UserRepository userRepository;
    private final ExerciseSessionService exerciseSessionService;

    @Transactional(readOnly = true)
    public MeasurementProgressResponse getTodayProgress(Long userId) {
        return progress(todaySessions(userId));
    }

    @Transactional
    public ExerciseSessionCreateResponse resume(Long userId) {
        userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new GeneralException(FitnessErrorCode.SESSION_ACCESS_DENIED));
        MeasurementProgressResponse progress = progress(todaySessions(userId));
        if (progress.measurementGroupId() == null) {
            throw new GeneralException(FitnessErrorCode.MEASUREMENT_NOT_STARTED);
        }
        if (progress.completed()) {
            throw new GeneralException(FitnessErrorCode.MEASUREMENT_ALREADY_COMPLETED);
        }
        return exerciseSessionService.create(
                userId,
                new ExerciseSessionCreateRequest(
                        ExerciseSessionMode.MEASUREMENT,
                        progress.nextExerciseType(),
                        progress.measurementGroupId()
                )
        );
    }

    @Transactional
    public ExerciseSessionCreateResponse restart(Long userId) {
        userRepository.findByIdForUpdate(userId)
                .orElseThrow(() -> new GeneralException(FitnessErrorCode.SESSION_ACCESS_DENIED));
        LocalDateTime now = LocalDateTime.now(SERVER_ZONE);
        todaySessions(userId).forEach(session -> session.discardMeasurement(now));
        exerciseSessionRepository.flush();
        return exerciseSessionService.create(
                userId,
                new ExerciseSessionCreateRequest(
                        ExerciseSessionMode.MEASUREMENT,
                        ExerciseType.CHAIR_STAND,
                        null
                )
        );
    }

    private MeasurementProgressResponse progress(List<ExerciseSession> sessions) {
        if (sessions.isEmpty()) {
            return new MeasurementProgressResponse(null, List.of(), MeasurementSequence.first(), false);
        }
        String groupId = sessions.get(0).getMeasurementGroupId();
        Set<ExerciseType> completed = sessions.stream()
                .filter(session -> session.getStatus() == ExerciseSessionStatus.COMPLETED)
                .map(ExerciseSession::getExerciseType)
                .collect(Collectors.toSet());
        List<ExerciseType> orderedCompleted = MeasurementSequence.orderedCompleted(completed);
        ExerciseType next = MeasurementSequence.next(completed);
        return new MeasurementProgressResponse(groupId, orderedCompleted, next, next == null);
    }

    private List<ExerciseSession> todaySessions(Long userId) {
        LocalDateTime start = LocalDateTime.now(SERVER_ZONE).toLocalDate().atStartOfDay();
        return exerciseSessionRepository
                .findAllByUserIdAndModeAndCreatedAtGreaterThanEqualAndCreatedAtLessThanAndIsDeletedFalseOrderByCreatedAtAsc(
                        userId, ExerciseSessionMode.MEASUREMENT, start, start.plusDays(1)
                );
    }
}
