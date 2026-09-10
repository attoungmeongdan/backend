package com.atmd.backend.domain.fitness.service;

import com.atmd.backend.domain.fitness.dto.response.ExercisePeerComparisonResponse;
import com.atmd.backend.domain.fitness.dto.response.FitnessPerformanceResponse;
import com.atmd.backend.domain.fitness.dto.response.MeasurementAnalysisResponse;
import com.atmd.backend.domain.fitness.dto.response.MeasurementPercentileResponse;
import com.atmd.backend.domain.fitness.dto.response.PerformanceGroupComparisonResponse;
import com.atmd.backend.domain.fitness.dto.response.PercentileBucketResponse;
import com.atmd.backend.domain.fitness.entity.ExerciseSession;
import com.atmd.backend.domain.fitness.enums.ExerciseSessionMode;
import com.atmd.backend.domain.fitness.enums.ExerciseSessionStatus;
import com.atmd.backend.domain.fitness.enums.ExerciseType;
import com.atmd.backend.domain.fitness.exception.FitnessErrorCode;
import com.atmd.backend.domain.fitness.repository.CohortMeasurementProjection;
import com.atmd.backend.domain.fitness.repository.ExerciseSessionRepository;
import com.atmd.backend.domain.user.entity.User;
import com.atmd.backend.domain.user.entity.enums.Gender;
import com.atmd.backend.domain.user.exception.UserErrorCode;
import com.atmd.backend.domain.user.repository.UserRepository;
import com.atmd.backend.global.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MeasurementAnalysisService {

    private static final int MINIMUM_PERCENTILE_SAMPLE_SIZE = 30;
    private static final double MAX_EXERCISE_SCORE = 150.0;
    private static final int DISTRIBUTION_BUCKET_COUNT = 10;
    private static final int DISTRIBUTION_BUCKET_WIDTH = 15;
    private static final List<Gender> PERFORMANCE_GENDERS = List.of(Gender.MALE, Gender.FEMALE);
    private static final List<AgeStandard> AGE_STANDARDS = List.of(
            new AgeStandard("10대", 15),
            new AgeStandard("20대", 25),
            new AgeStandard("30대", 35),
            new AgeStandard("40대", 45),
            new AgeStandard("50대", 55),
            new AgeStandard("60대", 65),
            new AgeStandard("70대 이상", 75)
    );

    private final UserRepository userRepository;
    private final ExerciseSessionRepository exerciseSessionRepository;
    private final ExerciseStandardService exerciseStandardService;

    @Transactional(readOnly = true)
    public MeasurementAnalysisResponse getAnalysis(Long userId, String measurementGroupId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(UserErrorCode.USER_NOT_FOUND));
        validateProfile(user);
        Map<ExerciseType, Double> measuredValues = completedValues(userId, measurementGroupId);
        ExerciseStandardService.Snapshot standards = exerciseStandardService.loadSnapshot();

        List<ExercisePeerComparisonResponse> comparisons = buildPeerComparisons(user, measuredValues, standards);
        double overallScore = calculateOverallScore(user.getGender(), user.getAge(), measuredValues, standards);
        MeasurementPercentileResponse percentile = calculatePercentile(user, overallScore, standards);
        List<PerformanceGroupComparisonResponse> performanceComparisons = buildPerformanceComparisons(
                measuredValues, standards);
        PerformanceGroupComparisonResponse closestGroup = performanceComparisons.stream()
                .max(Comparator.comparingDouble(PerformanceGroupComparisonResponse::similarityRate))
                .orElseThrow();
        FitnessPerformanceResponse fitnessPerformance = new FitnessPerformanceResponse(
                closestGroup.gender(),
                closestGroup.ageGroup(),
                closestGroup.label(),
                "운동 수행 기록이 " + closestGroup.label() + " 평균과 가장 비슷해요"
        );

        return new MeasurementAnalysisResponse(
                measurementGroupId,
                comparisons,
                overallScore,
                percentile,
                performanceComparisons,
                fitnessPerformance
        );
    }

    private void validateProfile(User user) {
        if (user.getAge() == null || user.getGender() == null) {
            throw new GeneralException(FitnessErrorCode.MEASUREMENT_PROFILE_REQUIRED);
        }
    }

    private Map<ExerciseType, Double> completedValues(Long userId, String measurementGroupId) {
        List<ExerciseSession> allSessions = exerciseSessionRepository
                .findAllByUserIdAndMeasurementGroupIdAndIsDeletedFalse(userId, measurementGroupId);
        if (allSessions.isEmpty()) {
            throw new GeneralException(FitnessErrorCode.MEASUREMENT_RESULT_NOT_FOUND);
        }

        Map<ExerciseType, ExerciseSession> completedByType = new EnumMap<>(ExerciseType.class);
        allSessions.stream()
                .filter(session -> session.getMode() == ExerciseSessionMode.MEASUREMENT)
                .filter(session -> session.getStatus() == ExerciseSessionStatus.COMPLETED)
                .sorted(Comparator.comparing(ExerciseSession::getCompletedAt))
                .forEach(session -> completedByType.put(session.getExerciseType(), session));
        if (completedByType.size() != ExerciseType.values().length) {
            throw new GeneralException(FitnessErrorCode.MEASUREMENT_INCOMPLETE);
        }
        return sessionValues(completedByType);
    }

    private List<ExercisePeerComparisonResponse> buildPeerComparisons(
            User user,
            Map<ExerciseType, Double> measuredValues,
            ExerciseStandardService.Snapshot standards
    ) {
        List<ExercisePeerComparisonResponse> result = new ArrayList<>();
        for (ExerciseType type : ExerciseType.values()) {
            double measured = measuredValues.get(type);
            double average = standards.getAverage(type, user.getGender(), user.getAge());
            double rate = achievementRate(measured, average);
            ComparisonLevel level = comparisonLevel(rate);
            result.add(new ExercisePeerComparisonResponse(
                    type,
                    round(measured),
                    round(average),
                    type == ExerciseType.PLANK ? "SECOND" : "COUNT",
                    level.name(),
                    level.message
            ));
        }
        return List.copyOf(result);
    }

    private MeasurementPercentileResponse calculatePercentile(
            User user, double targetScore, ExerciseStandardService.Snapshot standards
    ) {
        int age = user.getAge();
        int decadeMinimum = age >= 70 ? 70 : age / 10 * 10;
        int decadeMaximum = age >= 70 ? 150 : decadeMinimum + 9;
        CohortRange range = new CohortRange(
                decadeMinimum,
                decadeMaximum,
                age >= 70 ? "70대 이상" : decadeMinimum + "대"
        );
        List<Double> comparisonScores = cohortScores(user.getId(), user.getGender(), range, standards);
        int comparisonSampleSize = comparisonScores.size();
        if (comparisonSampleSize >= MINIMUM_PERCENTILE_SAMPLE_SIZE) {
            List<Double> percentileScores = new ArrayList<>(comparisonScores);
            percentileScores.add(targetScore);
            return availablePercentile(
                    percentileScores,
                    comparisonSampleSize,
                    targetScore,
                    user.getGender(),
                    range.label()
            );
        }
        return new MeasurementPercentileResponse(
                false,
                null,
                null,
                user.getGender(),
                range.label(),
                comparisonSampleSize,
                "아직 동년배 측정 기록이 조금 부족해요",
                targetScore,
                null,
                null,
                List.of()
        );
    }

    private List<Double> cohortScores(
            Long targetUserId,
            Gender gender,
            CohortRange range,
            ExerciseStandardService.Snapshot standards
    ) {
        List<CohortMeasurementProjection> measurements =
                exerciseSessionRepository.findLatestCompletedMeasurementsForCohort(
                gender.name(),
                range.minimumAge(),
                range.maximumAge(),
                targetUserId
        );

        List<Double> scores = new ArrayList<>();
        for (CohortMeasurementProjection measurement : measurements) {
            Map<ExerciseType, Double> values = new EnumMap<>(ExerciseType.class);
            values.put(ExerciseType.CHAIR_STAND, measurement.getChairStandValue());
            values.put(ExerciseType.SIT_UP, measurement.getSitUpValue());
            values.put(ExerciseType.PUSH_UP, measurement.getPushUpValue());
            values.put(ExerciseType.PLANK, measurement.getPlankValue());
            scores.add(calculateOverallScore(gender, measurement.getMeasurementAge(), values, standards));
        }
        return scores;
    }

    private MeasurementPercentileResponse availablePercentile(
            List<Double> scores,
            int comparisonSampleSize,
            double targetScore,
            Gender gender,
            String ageGroup
    ) {
        long lower = scores.stream().filter(score -> score < targetScore).count();
        long equal = scores.stream().filter(score -> Double.compare(score, targetScore) == 0).count();
        int percentile = (int) Math.round((lower + equal * 0.5) / scores.size() * 100.0);
        int topPercent = 100 - percentile;
        List<PercentileBucketResponse> buckets = distributionBuckets(scores);
        int userBucketIndex = bucketIndex(targetScore);
        long maximumBucketCount = buckets.stream()
                .mapToLong(PercentileBucketResponse::count)
                .max()
                .orElse(0);
        return new MeasurementPercentileResponse(
                true,
                percentile,
                topPercent,
                gender,
                ageGroup,
                comparisonSampleSize,
                "동년배 중 상위 " + topPercent + "%예요",
                targetScore,
                userBucketIndex,
                maximumBucketCount,
                buckets
        );
    }

    private List<PercentileBucketResponse> distributionBuckets(List<Double> scores) {
        long[] counts = new long[DISTRIBUTION_BUCKET_COUNT];
        scores.forEach(score -> counts[bucketIndex(score)]++);
        List<PercentileBucketResponse> buckets = new ArrayList<>();
        for (int index = 0; index < DISTRIBUTION_BUCKET_COUNT; index++) {
            buckets.add(new PercentileBucketResponse(
                    index * DISTRIBUTION_BUCKET_WIDTH,
                    (index + 1) * DISTRIBUTION_BUCKET_WIDTH,
                    counts[index],
                    round(counts[index] * 100.0 / scores.size())
            ));
        }
        return List.copyOf(buckets);
    }

    private int bucketIndex(double score) {
        int index = Math.min((int) (score / DISTRIBUTION_BUCKET_WIDTH), DISTRIBUTION_BUCKET_COUNT - 1);
        return Math.max(index, 0);
    }

    private List<PerformanceGroupComparisonResponse> buildPerformanceComparisons(
            Map<ExerciseType, Double> measuredValues,
            ExerciseStandardService.Snapshot standards
    ) {
        List<PerformanceGroupComparisonResponse> result = new ArrayList<>();
        for (AgeStandard standard : AGE_STANDARDS) {
            for (Gender gender : PERFORMANCE_GENDERS) {
                String label = standard.label() + " " + genderLabel(gender);
                result.add(new PerformanceGroupComparisonResponse(
                        gender,
                        standard.label(),
                        label,
                        round(ageGroupSimilarity(
                                gender, standard.representativeAge(), measuredValues, standards))
                ));
            }
        }
        return List.copyOf(result);
    }

    private double ageGroupSimilarity(
            Gender gender,
            int representativeAge,
            Map<ExerciseType, Double> measuredValues,
            ExerciseStandardService.Snapshot standards
    ) {
        double averageDifference = 0;
        for (ExerciseType type : ExerciseType.values()) {
            double standard = standards.getAverage(type, gender, representativeAge);
            averageDifference += Math.abs(achievementRate(measuredValues.get(type), standard) - 100.0);
        }
        return Math.max(0, 100.0 - averageDifference / ExerciseType.values().length);
    }

    private double calculateOverallScore(
            Gender gender,
            int age,
            Map<ExerciseType, Double> measuredValues,
            ExerciseStandardService.Snapshot standards
    ) {
        double total = 0;
        for (ExerciseType type : ExerciseType.values()) {
            double average = standards.getAverage(type, gender, age);
            total += Math.min(achievementRate(measuredValues.get(type), average), MAX_EXERCISE_SCORE);
        }
        return round(total / ExerciseType.values().length);
    }

    private Map<ExerciseType, Double> sessionValues(Map<ExerciseType, ExerciseSession> sessionsByType) {
        Map<ExerciseType, Double> values = new EnumMap<>(ExerciseType.class);
        sessionsByType.forEach((type, session) -> values.put(
                type,
                type == ExerciseType.PLANK
                        ? session.getValidDurationMs() / 1000.0
                        : (double) session.getValidCount()
        ));
        return values;
    }

    private ComparisonLevel comparisonLevel(double achievementRate) {
        if (achievementRate < 90.0) return ComparisonLevel.LOW;
        if (achievementRate <= 110.0) return ComparisonLevel.SIMILAR;
        return ComparisonLevel.HIGH;
    }

    private double achievementRate(double measured, double average) {
        return average <= 0 ? 0 : measured / average * 100.0;
    }

    private String genderLabel(Gender gender) {
        return gender == Gender.MALE ? "남성" : "여성";
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private enum ComparisonLevel {
        LOW("동연령대보다 낮아요"),
        SIMILAR("동연령대와 비슷해요"),
        HIGH("동연령대보다 높아요");

        private final String message;

        ComparisonLevel(String message) {
            this.message = message;
        }
    }

    private record AgeStandard(String label, int representativeAge) {
    }

    private record CohortRange(int minimumAge, int maximumAge, String label) {
    }
}
