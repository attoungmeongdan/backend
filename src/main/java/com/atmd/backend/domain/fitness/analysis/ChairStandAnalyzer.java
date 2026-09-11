package com.atmd.backend.domain.fitness.analysis;

import com.atmd.backend.domain.fitness.dto.request.LandmarkDto;
import com.atmd.backend.domain.fitness.enums.ChairStandPhase;
import com.atmd.backend.domain.fitness.pose.PoseLandmarkIndex;
import com.atmd.backend.domain.fitness.pose.PoseMetricCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChairStandAnalyzer {
    private static final double MIN_VISIBILITY = 0.6;
    private static final int CONFIRMATION_FRAMES = 3;

    // CHAIR_STAND_V2 초기 임계값. 실제 측정 데이터로 보정해야 한다.
    private static final double SITTING_KNEE_MAX = 125.0;
    private static final double SITTING_HIP_MAX = 120.0;
    private static final double STANDING_KNEE_MIN = 150.0;
    private static final double STANDING_HIP_MIN = 150.0;

    private final PoseMetricCalculator metricCalculator;

    public ChairStandMetrics calculateMetrics(List<LandmarkDto> landmarks) {
        List<Double> kneeAngles = new ArrayList<>(2);
        List<Double> hipAngles = new ArrayList<>(2);

        addSideMetrics(landmarks, true, kneeAngles, hipAngles);
        addSideMetrics(landmarks, false, kneeAngles, hipAngles);

        if (kneeAngles.isEmpty() || hipAngles.isEmpty()) {
            throw new IllegalArgumentException("Required chair-stand landmarks are not visible");
        }

        return new ChairStandMetrics(average(kneeAngles), average(hipAngles));
    }

    public boolean analyze(ChairStandAnalysisState state, ChairStandMetrics metrics) {
        ChairStandPhase target = targetPhase(state.getPhase(), metrics);
        if (target == null) {
            state.clearCandidate();
            return false;
        }

        return state.confirm(target, CONFIRMATION_FRAMES);
    }

    public boolean analyzeAndCount(ChairStandAnalysisState state, ChairStandMetrics metrics) {
        ChairStandPhase previous = state.getPhase();
        boolean changed = analyze(state, metrics);
        if (changed && previous == ChairStandPhase.SITTING && state.getPhase() == ChairStandPhase.STANDING) {
            state.incrementValidCount();
            return true;
        }
        return false;
    }

    private ChairStandPhase targetPhase(ChairStandPhase current, ChairStandMetrics metrics) {
        return switch (current) {
            case UNKNOWN -> isStanding(metrics) ? ChairStandPhase.STANDING : null;
            case STANDING -> isSitting(metrics) ? ChairStandPhase.SITTING : null;
            case SITTING -> isStanding(metrics) ? ChairStandPhase.STANDING : null;
            case RISING, LOWERING -> null;
        };
    }

    private boolean isSitting(ChairStandMetrics metrics) {
        return metrics.kneeAngle() <= SITTING_KNEE_MAX && metrics.hipAngle() <= SITTING_HIP_MAX;
    }

    private boolean isStanding(ChairStandMetrics metrics) {
        return metrics.kneeAngle() >= STANDING_KNEE_MIN && metrics.hipAngle() >= STANDING_HIP_MIN;
    }

    private void addSideMetrics(
            List<LandmarkDto> landmarks,
            boolean left,
            List<Double> kneeAngles,
            List<Double> hipAngles
    ) {
        int shoulderIndex = left ? PoseLandmarkIndex.LEFT_SHOULDER : PoseLandmarkIndex.RIGHT_SHOULDER;
        int hipIndex = left ? PoseLandmarkIndex.LEFT_HIP : PoseLandmarkIndex.RIGHT_HIP;
        int kneeIndex = left ? PoseLandmarkIndex.LEFT_KNEE : PoseLandmarkIndex.RIGHT_KNEE;
        int ankleIndex = left ? PoseLandmarkIndex.LEFT_ANKLE : PoseLandmarkIndex.RIGHT_ANKLE;

        LandmarkDto shoulder = landmarks.get(shoulderIndex);
        LandmarkDto hip = landmarks.get(hipIndex);
        LandmarkDto knee = landmarks.get(kneeIndex);
        LandmarkDto ankle = landmarks.get(ankleIndex);
        if (visible(shoulder, hip, knee, ankle)) {
            kneeAngles.add(metricCalculator.calculateAngle(hip, knee, ankle));
            hipAngles.add(metricCalculator.calculateAngle(shoulder, hip, knee));
        }
    }

    private boolean visible(LandmarkDto... landmarks) {
        for (LandmarkDto landmark : landmarks) {
            if (landmark.visibility() < MIN_VISIBILITY) {
                return false;
            }
        }
        return true;
    }

    private double average(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElseThrow();
    }
}
