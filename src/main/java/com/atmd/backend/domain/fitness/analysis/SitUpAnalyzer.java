package com.atmd.backend.domain.fitness.analysis;

import com.atmd.backend.domain.fitness.dto.request.LandmarkDto;
import com.atmd.backend.domain.fitness.enums.SitUpPhase;
import com.atmd.backend.domain.fitness.pose.PoseLandmarkIndex;
import com.atmd.backend.domain.fitness.pose.PoseMetricCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SitUpAnalyzer {
    private static final double MIN_VISIBILITY = 0.6;
    private static final int CONFIRMATION_FRAMES = 3;

    // SIT_UP_V7: count-only two-state thresholds.
    private static final double LYING_ANGLE_MIN = 145.0;
    private static final double UP_ANGLE_MAX = 115.0;

    private final PoseMetricCalculator metricCalculator;

    public SitUpMetrics calculateMetrics(List<LandmarkDto> landmarks) {
        List<Double> trunkAngles = new ArrayList<>(2);
        addSideMetric(landmarks, true, trunkAngles);
        addSideMetric(landmarks, false, trunkAngles);
        if (trunkAngles.isEmpty()) {
            throw new IllegalArgumentException("Required sit-up landmarks are not visible");
        }
        return new SitUpMetrics(average(trunkAngles));
    }

    public boolean analyzeAndCount(SitUpAnalysisState state, SitUpMetrics metrics) {
        SitUpPhase previous = state.getPhase();
        SitUpPhase target = targetPhase(previous, metrics);
        if (target == null) {
            state.clearCandidate();
            return false;
        }

        boolean changed = state.confirm(target, CONFIRMATION_FRAMES);
        if (changed && previous == SitUpPhase.LYING && state.getPhase() == SitUpPhase.UP) {
            state.incrementValidCount();
            return true;
        }
        return false;
    }

    private SitUpPhase targetPhase(SitUpPhase current, SitUpMetrics metrics) {
        double angle = metrics.trunkFlexionAngle();
        return switch (current) {
            case UNKNOWN -> angle >= LYING_ANGLE_MIN ? SitUpPhase.LYING : null;
            case LYING -> angle <= UP_ANGLE_MAX ? SitUpPhase.UP : null;
            case UP -> angle >= LYING_ANGLE_MIN ? SitUpPhase.LYING : null;
            case RISING, LOWERING -> null;
        };
    }

    private void addSideMetric(List<LandmarkDto> landmarks, boolean left, List<Double> trunkAngles) {
        int shoulderIndex = left ? PoseLandmarkIndex.LEFT_SHOULDER : PoseLandmarkIndex.RIGHT_SHOULDER;
        int hipIndex = left ? PoseLandmarkIndex.LEFT_HIP : PoseLandmarkIndex.RIGHT_HIP;
        int kneeIndex = left ? PoseLandmarkIndex.LEFT_KNEE : PoseLandmarkIndex.RIGHT_KNEE;
        LandmarkDto shoulder = landmarks.get(shoulderIndex);
        LandmarkDto hip = landmarks.get(hipIndex);
        LandmarkDto knee = landmarks.get(kneeIndex);
        if (shoulder.visibility() >= MIN_VISIBILITY
                && hip.visibility() >= MIN_VISIBILITY
                && knee.visibility() >= MIN_VISIBILITY) {
            trunkAngles.add(metricCalculator.calculateAngle(shoulder, hip, knee));
        }
    }

    private double average(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElseThrow();
    }
}
