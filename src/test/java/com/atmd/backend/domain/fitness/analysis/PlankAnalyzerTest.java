package com.atmd.backend.domain.fitness.analysis;

import com.atmd.backend.domain.fitness.enums.PlankPhase;
import com.atmd.backend.domain.fitness.pose.PoseMetricCalculator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlankAnalyzerTest {
    private final PlankAnalyzer analyzer = new PlankAnalyzer(new PoseMetricCalculator());

    @Test
    void startsHoldingAfterThreeValidFrames() {
        PlankAnalysisState state = new PlankAnalysisState();

        repeat(state, new PlankMetrics(170, 175, 90), 2);
        assertThat(state.getPhase()).isEqualTo(PlankPhase.POSITIONING);

        analyzer.analyze(state, new PlankMetrics(170, 175, 90));
        assertThat(state.getPhase()).isEqualTo(PlankPhase.HOLDING);
    }

    @Test
    void breaksAfterFiveConsecutiveInvalidFrames() {
        PlankAnalysisState state = holdingState();

        repeat(state, new PlankMetrics(145, 170, 90), 4);
        assertThat(state.getPhase()).isEqualTo(PlankPhase.HOLDING);

        analyzer.analyze(state, new PlankMetrics(145, 170, 90));
        assertThat(state.getPhase()).isEqualTo(PlankPhase.BROKEN);
    }

    @Test
    void transientInvalidFrameDoesNotBreakHolding() {
        PlankAnalysisState state = holdingState();

        repeat(state, new PlankMetrics(145, 170, 90), 4);
        analyzer.analyze(state, new PlankMetrics(170, 170, 90));
        repeat(state, new PlankMetrics(145, 170, 90), 4);

        assertThat(state.getPhase()).isEqualTo(PlankPhase.HOLDING);
    }

    private PlankAnalysisState holdingState() {
        PlankAnalysisState state = new PlankAnalysisState();
        repeat(state, new PlankMetrics(170, 170, 90), 3);
        return state;
    }

    @Test
    void breaksWhenElbowIsExtendedBeyondMaximumAngle() {
        PlankAnalysisState state = holdingState();

        repeat(state, new PlankMetrics(170, 170, 131), 5);

        assertThat(state.getPhase()).isEqualTo(PlankPhase.BROKEN);
    }

    @Test
    void breaksAfterFiveConsecutiveFramesWithMissingLandmarks() {
        PlankAnalysisState state = holdingState();

        for (int frame = 0; frame < 4; frame++) {
            analyzer.analyzeMissingLandmarks(state);
        }
        assertThat(state.getPhase()).isEqualTo(PlankPhase.HOLDING);

        analyzer.analyzeMissingLandmarks(state);

        assertThat(state.getPhase()).isEqualTo(PlankPhase.BROKEN);
    }

    private void repeat(PlankAnalysisState state, PlankMetrics metrics, int frames) {
        for (int frame = 0; frame < frames; frame++) {
            analyzer.analyze(state, metrics);
        }
    }
}
