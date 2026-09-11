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
    private static final double ELBOW_ANGLE_MAX = 130.0;
    private static final int HOLDING_CONFIRMATION_FRAMES = 3;
    private static final int BROKEN_CONFIRMATION_FRAMES = 5;

    private final PoseMetricCalculator metricCalculator;

    public PlankMetrics calculateMetrics(List<LandmarkDto> landmarks) {
        List<Double> bodyAngles = new ArrayList<>(2);
        List<Double> legAngles = new ArrayList<>(2);
        List<Double> elbowAngles = new ArrayList<>(2);
        addSideMetrics(landmarks, true, bodyAngles, legAngles, elbowAngles);
        addSideMetrics(landmarks, false, bodyAngles, legAngles, elbowAngles);
        if (bodyAngles.isEmpty() || elbowAngles.isEmpty()) {
            throw new IllegalArgumentException("Required plank landmarks are not visible");
        }
        return new PlankMetrics(average(bodyAngles), average(legAngles), average(elbowAngles));
    }

    public PlankPhase analyze(PlankAnalysisState state, PlankMetrics metrics) {
        boolean holding = metrics.bodyAlignmentAngle() >= HOLDING_ALIGNMENT_MIN
                && metrics.legAlignmentAngle() >= HOLDING_ALIGNMENT_MIN
                && metrics.elbowAngle() <= ELBOW_ANGLE_MAX;
        boolean broken = metrics.bodyAlignmentAngle() < BROKEN_ALIGNMENT_MAX
                || metrics.legAlignmentAngle() < BROKEN_ALIGNMENT_MAX
                || metrics.elbowAngle() > ELBOW_ANGLE_MAX;

        if (state.getPhase() == PlankPhase.POSITIONING && holding) {
            state.confirmHolding(HOLDING_CONFIRMATION_FRAMES);
        } else if (state.getPhase() == PlankPhase.HOLDING && broken) {
            state.confirmBroken(BROKEN_CONFIRMATION_FRAMES);
        } else {
            state.clearCandidates();
        }
        return state.getPhase();
    }

    public PlankPhase analyzeMissingLandmarks(PlankAnalysisState state) {
        if (state.getPhase() == PlankPhase.HOLDING) {
            state.confirmBroken(BROKEN_CONFIRMATION_FRAMES);
        } else {
            state.clearCandidates();
        }
        return state.getPhase();
    }

    private void addSideMetrics(List<LandmarkDto> landmarks, boolean left,
                                List<Double> bodyAngles, List<Double> legAngles,
                                List<Double> elbowAngles) {
        int shoulderIndex = left ? PoseLandmarkIndex.LEFT_SHOULDER : PoseLandmarkIndex.RIGHT_SHOULDER;
        int elbowIndex = left ? 13 : 14;
        int wristIndex = left ? 15 : 16;
        int hipIndex = left ? PoseLandmarkIndex.LEFT_HIP : PoseLandmarkIndex.RIGHT_HIP;
        int kneeIndex = left ? PoseLandmarkIndex.LEFT_KNEE : PoseLandmarkIndex.RIGHT_KNEE;
        int ankleIndex = left ? PoseLandmarkIndex.LEFT_ANKLE : PoseLandmarkIndex.RIGHT_ANKLE;
        LandmarkDto shoulder = landmarks.get(shoulderIndex);
        LandmarkDto elbow = landmarks.get(elbowIndex);
        LandmarkDto wrist = landmarks.get(wristIndex);
        LandmarkDto hip = landmarks.get(hipIndex);
        LandmarkDto knee = landmarks.get(kneeIndex);
        LandmarkDto ankle = landmarks.get(ankleIndex);
        if (visible(shoulder, elbow, wrist, hip, knee, ankle)) {
            bodyAngles.add(metricCalculator.calculateAngle(shoulder, hip, ankle));
            legAngles.add(metricCalculator.calculateAngle(hip, knee, ankle));
            elbowAngles.add(metricCalculator.calculateAngle(shoulder, elbow, wrist));
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
