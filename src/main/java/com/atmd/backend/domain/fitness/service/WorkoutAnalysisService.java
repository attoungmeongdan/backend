package com.atmd.backend.domain.fitness.service;

import com.atmd.backend.domain.fitness.dto.response.WorkoutSessionAnalysisResponse;
import com.atmd.backend.domain.fitness.entity.ExerciseSession;
import com.atmd.backend.domain.fitness.enums.ExerciseSessionMode;
import com.atmd.backend.domain.fitness.enums.ExerciseSessionStatus;
import com.atmd.backend.domain.fitness.enums.ExerciseType;
import com.atmd.backend.domain.fitness.exception.FitnessErrorCode;
import com.atmd.backend.domain.fitness.repository.ExerciseSessionRepository;
import com.atmd.backend.domain.user.entity.User;
import com.atmd.backend.global.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkoutAnalysisService {

    private final ExerciseSessionRepository exerciseSessionRepository;
    private final ExerciseStandardService exerciseStandardService;

    @Transactional(readOnly = true)
    public WorkoutSessionAnalysisResponse getAnalysis(Long userId, Long sessionId) {
        ExerciseSession session = exerciseSessionRepository.findById(sessionId)
                .orElseThrow(() -> new GeneralException(FitnessErrorCode.SESSION_NOT_FOUND));
        if (!session.belongsTo(userId)) {
            throw new GeneralException(FitnessErrorCode.SESSION_ACCESS_DENIED);
        }
        if (session.getMode() != ExerciseSessionMode.WORKOUT
                || session.getStatus() != ExerciseSessionStatus.COMPLETED) {
            throw new GeneralException(FitnessErrorCode.WORKOUT_RESULT_NOT_AVAILABLE);
        }

        User user = session.getUser();
        if (user.getAge() == null || user.getGender() == null) {
            throw new GeneralException(FitnessErrorCode.MEASUREMENT_PROFILE_REQUIRED);
        }

        ExerciseType exerciseType = session.getExerciseType();
        boolean plank = exerciseType == ExerciseType.PLANK;
        double measuredValue = plank
                ? session.getValidDurationMs() / 1000.0
                : session.getValidCount();
        double averageValue = exerciseStandardService.getAverage(
                exerciseType, user.getGender(), user.getAge()
        );
        double achievementRate = averageValue <= 0
                ? 0
                : measuredValue / averageValue * 100.0;
        Comparison comparison = comparison(achievementRate);

        return new WorkoutSessionAnalysisResponse(
                session.getId(),
                exerciseType,
                session.getCompletedAt(),
                round(measuredValue),
                round(averageValue),
                plank ? "SECOND" : "COUNT",
                round(achievementRate),
                comparison.name(),
                comparison.message
        );
    }

    private Comparison comparison(double achievementRate) {
        if (achievementRate < 90.0) return Comparison.LOW;
        if (achievementRate <= 110.0) return Comparison.SIMILAR;
        return Comparison.HIGH;
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private enum Comparison {
        LOW("동연령대보다 낮아요"),
        SIMILAR("동연령대와 비슷해요"),
        HIGH("동연령대보다 높아요");

        private final String message;

        Comparison(String message) {
            this.message = message;
        }
    }
}
