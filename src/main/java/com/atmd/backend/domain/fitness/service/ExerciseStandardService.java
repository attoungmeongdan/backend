package com.atmd.backend.domain.fitness.service;

import com.atmd.backend.domain.fitness.entity.FitpleExerciseStandard;
import com.atmd.backend.domain.fitness.enums.ExerciseType;
import com.atmd.backend.domain.fitness.exception.FitnessErrorCode;
import com.atmd.backend.domain.fitness.repository.FitpleExerciseStandardRepository;
import com.atmd.backend.domain.user.entity.enums.Gender;
import com.atmd.backend.global.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExerciseStandardService {

    private final FitpleExerciseStandardRepository fitpleExerciseStandardRepository;

    @Transactional(readOnly = true)
    public double getAverage(ExerciseType exerciseType, Gender gender, int age) {
        boolean male = gender == Gender.MALE;
        return switch (exerciseType) {
            case CHAIR_STAND -> chairStandAverage(age, male);
            case SIT_UP -> sitUpAverage(age, male);
            case PUSH_UP, PLANK -> fitpleAverage(exerciseType, gender, age);
        };
    }

    @Transactional(readOnly = true)
    public Snapshot loadSnapshot() {
        return new Snapshot(fitpleExerciseStandardRepository.findAllByIsActiveTrueOrderByIdDesc());
    }

    private double fitpleAverage(ExerciseType exerciseType, Gender gender, int age) {
        return fitpleExerciseStandardRepository
                .findFirstByExerciseTypeAndGenderAndMinimumAgeLessThanEqualAndMaximumAgeGreaterThanEqualAndIsActiveTrueOrderByIdDesc(
                        exerciseType, gender, age, age)
                .map(FitpleExerciseStandard::getAverageValue)
                .map(Number::doubleValue)
                .orElseThrow(() -> new GeneralException(FitnessErrorCode.EXERCISE_STANDARD_NOT_FOUND));
    }

    private double chairStandAverage(int age, boolean male) {
        if (age >= 80) return male ? 14 : 13;
        if (age >= 70) return male ? 16 : 15;
        if (age >= 60) return 19;
        if (age >= 50) return male ? 23 : 22;
        if (age >= 40) return male ? 26 : 25;
        if (age >= 30) return male ? 28 : 27;
        if (age >= 20) return male ? 26 : 27;
        return male ? 26 : 24;
    }

    private double sitUpAverage(int age, boolean male) {
        if (age >= 60) return male ? 20 : 8;
        if (age >= 50) return male ? 26 : 17;
        if (age >= 40) return male ? 31 : 22;
        if (age >= 30) return male ? 36 : 27;
        if (age >= 20) return male ? 40 : 35;
        return male ? 40 : 34;
    }

    public final class Snapshot {
        private final List<FitpleExerciseStandard> activeStandards;

        private Snapshot(List<FitpleExerciseStandard> activeStandards) {
            this.activeStandards = List.copyOf(activeStandards);
        }

        public double getAverage(ExerciseType exerciseType, Gender gender, int age) {
            boolean male = gender == Gender.MALE;
            return switch (exerciseType) {
                case CHAIR_STAND -> chairStandAverage(age, male);
                case SIT_UP -> sitUpAverage(age, male);
                case PUSH_UP, PLANK -> activeStandards.stream()
                        .filter(standard -> standard.getExerciseType() == exerciseType)
                        .filter(standard -> standard.getGender() == gender)
                        .filter(standard -> standard.supportsAge(age))
                        .findFirst()
                        .map(FitpleExerciseStandard::getAverageValue)
                        .map(Number::doubleValue)
                        .orElseThrow(() -> new GeneralException(FitnessErrorCode.EXERCISE_STANDARD_NOT_FOUND));
            };
        }
    }
}
