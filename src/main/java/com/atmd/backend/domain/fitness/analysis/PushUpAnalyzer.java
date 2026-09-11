package com.atmd.backend.domain.fitness.analysis;

import com.atmd.backend.domain.fitness.dto.request.LandmarkDto;
import com.atmd.backend.domain.fitness.enums.PushUpPhase;
import com.atmd.backend.domain.fitness.pose.PoseMetricCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PushUpAnalyzer {
    private static final double MIN_VISIBILITY = 0.6;
    private static final int CONFIRMATION_FRAMES = 3;
    private static final double UP_ELBOW_MIN = 150;
    private static final double DESCENDING_ELBOW_MAX = 145;
    private static final double DOWN_ELBOW_MAX = 105;
    private static final double ASCENDING_ELBOW_MIN = 115;
    private static final double BODY_ALIGNMENT_MIN = 160;

    private final PoseMetricCalculator metricCalculator;

    public PushUpMetrics calculateMetrics(List<LandmarkDto> landmarks) {
        List<Double> elbowAngles = new ArrayList<>(2);
        List<Double> bodyAngles = new ArrayList<>(2);
        addSideMetrics(landmarks, true, elbowAngles, bodyAngles);
        addSideMetrics(landmarks, false, elbowAngles, bodyAngles);
        if (elbowAngles.isEmpty()) {
            throw new IllegalArgumentException("Required push-up landmarks are not visible");
        }
        return new PushUpMetrics(average(elbowAngles), average(bodyAngles));
    }

    public boolean analyzeAndCount(PushUpAnalysisState state, PushUpMetrics metrics) {
        PushUpPhase previous = state.getPhase();
        PushUpPhase target = targetPhase(previous, metrics);
        if (target == null) {
            state.clearCandidate();
            return false;
        }
        boolean changed = state.confirm(target, CONFIRMATION_FRAMES);
        if (changed && previous == PushUpPhase.ASCENDING && state.getPhase() == PushUpPhase.UP) {
            state.incrementValidCount();
            return true;
        }
        return false;
    }

    private PushUpPhase targetPhase(PushUpPhase current, PushUpMetrics metrics) {
        boolean aligned = metrics.bodyAlignmentAngle() >= BODY_ALIGNMENT_MIN;
        return switch (current) {
            case UNKNOWN -> aligned && metrics.elbowAngle() >= UP_ELBOW_MIN ? PushUpPhase.UP : null;
            case UP -> metrics.elbowAngle() <= DESCENDING_ELBOW_MAX ? PushUpPhase.DESCENDING : null;
            case DESCENDING -> aligned && metrics.elbowAngle() <= DOWN_ELBOW_MAX ? PushUpPhase.DOWN : null;
            case DOWN -> metrics.elbowAngle() >= ASCENDING_ELBOW_MIN ? PushUpPhase.ASCENDING : null;
            case ASCENDING -> aligned && metrics.elbowAngle() >= UP_ELBOW_MIN ? PushUpPhase.UP : null;
        };
    }

    private void addSideMetrics(List<LandmarkDto> landmarks, boolean left,
                                List<Double> elbowAngles, List<Double> bodyAngles) {
        int shoulderIndex = left ? 11 : 12;
        int elbowIndex = left ? 13 : 14;
        int wristIndex = left ? 15 : 16;
        int hipIndex = left ? 23 : 24;
        int ankleIndex = left ? 27 : 28;
        LandmarkDto shoulder = landmarks.get(shoulderIndex);
        LandmarkDto elbow = landmarks.get(elbowIndex);
        LandmarkDto wrist = landmarks.get(wristIndex);
        LandmarkDto hip = landmarks.get(hipIndex);
        LandmarkDto ankle = landmarks.get(ankleIndex);
        if (visible(shoulder, elbow, wrist, hip, ankle)) {
            elbowAngles.add(metricCalculator.calculateAngle(shoulder, elbow, wrist));
            bodyAngles.add(metricCalculator.calculateAngle(shoulder, hip, ankle));
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
