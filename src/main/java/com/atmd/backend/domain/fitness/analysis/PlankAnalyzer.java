package com.atmd.backend.domain.fitness.analysis;

import com.atmd.backend.domain.fitness.dto.request.LandmarkDto;
import com.atmd.backend.domain.fitness.enums.PlankPhase;
import com.atmd.backend.domain.fitness.pose.PoseLandmarkIndex;
import com.atmd.backend.domain.fitness.pose.PoseMetricCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PlankAnalyzer {
    private static final double MIN_VISIBILITY = 0.6;
    private static final double HOLDING_ALIGNMENT_MIN = 160.0;
    private static final double BROKEN_ALIGNMENT_MAX = 150.0;
    private static final int HOLDING_CONFIRMATION_FRAMES = 3;
    private static final int BROKEN_CONFIRMATION_FRAMES = 5;

    private final PoseMetricCalculator metricCalculator;

    public PlankMetrics calculateMetrics(List<LandmarkDto> landmarks) {
        List<Double> bodyAngles = new ArrayList<>(2);
        List<Double> legAngles = new ArrayList<>(2);
        addSideMetrics(landmarks, true, bodyAngles, legAngles);
        addSideMetrics(landmarks, false, bodyAngles, legAngles);
        if (bodyAngles.isEmpty()) {
            throw new IllegalArgumentException("Required plank landmarks are not visible");
        }
        return new PlankMetrics(average(bodyAngles), average(legAngles));
    }

    public PlankPhase analyze(PlankAnalysisState state, PlankMetrics metrics) {
        boolean holding = metrics.bodyAlignmentAngle() >= HOLDING_ALIGNMENT_MIN
                && metrics.legAlignmentAngle() >= HOLDING_ALIGNMENT_MIN;
        boolean broken = metrics.bodyAlignmentAngle() < BROKEN_ALIGNMENT_MAX
                || metrics.legAlignmentAngle() < BROKEN_ALIGNMENT_MAX;

        if (state.getPhase() == PlankPhase.POSITIONING && holding) {
            state.confirmHolding(HOLDING_CONFIRMATION_FRAMES);
        } else if (state.getPhase() == PlankPhase.HOLDING && broken) {
            state.confirmBroken(BROKEN_CONFIRMATION_FRAMES);
        } else {
            state.clearCandidates();
        }
        return state.getPhase();
    }

    private void addSideMetrics(List<LandmarkDto> landmarks, boolean left,
                                List<Double> bodyAngles, List<Double> legAngles) {
        int shoulderIndex = left ? PoseLandmarkIndex.LEFT_SHOULDER : PoseLandmarkIndex.RIGHT_SHOULDER;
        int hipIndex = left ? PoseLandmarkIndex.LEFT_HIP : PoseLandmarkIndex.RIGHT_HIP;
        int kneeIndex = left ? PoseLandmarkIndex.LEFT_KNEE : PoseLandmarkIndex.RIGHT_KNEE;
        int ankleIndex = left ? PoseLandmarkIndex.LEFT_ANKLE : PoseLandmarkIndex.RIGHT_ANKLE;
        LandmarkDto shoulder = landmarks.get(shoulderIndex);
        LandmarkDto hip = landmarks.get(hipIndex);
        LandmarkDto knee = landmarks.get(kneeIndex);
        LandmarkDto ankle = landmarks.get(ankleIndex);
        if (visible(shoulder, hip, knee, ankle)) {
            bodyAngles.add(metricCalculator.calculateAngle(shoulder, hip, ankle));
            legAngles.add(metricCalculator.calculateAngle(hip, knee, ankle));
        }
    }

    private boolean visible(LandmarkDto... landmarks) {
        for (LandmarkDto landmark : landmarks) {
            if (landmark.visibility() < MIN_VISIBILITY) return false;
        }
        return true;
    }

    private double average(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElseThrow();
    }
}
