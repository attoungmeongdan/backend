package com.atmd.backend.domain.fitness.analysis;

import com.atmd.backend.domain.fitness.enums.PushUpPhase;
import com.atmd.backend.domain.fitness.pose.PoseMetricCalculator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PushUpAnalyzerTest {
    private final PushUpAnalyzer analyzer = new PushUpAnalyzer(new PoseMetricCalculator());

    @Test
    void countsAfterACompletePushUpCycle() {
        PushUpAnalysisState state = new PushUpAnalysisState();

        repeat(state, new PushUpMetrics(170, 175));
        repeat(state, new PushUpMetrics(140, 170));
        repeat(state, new PushUpMetrics(85, 165));
        assertThat(state.getPhase()).isEqualTo(PushUpPhase.DOWN);
        assertThat(state.getValidCount()).isZero();

        repeat(state, new PushUpMetrics(115, 165));
        repeat(state, new PushUpMetrics(165, 170));

        assertThat(state.getPhase()).isEqualTo(PushUpPhase.UP);
        assertThat(state.getValidCount()).isEqualTo(1);
    }

    @Test
    void doesNotEnterDownPhaseWhenBodyIsNotAligned() {
        PushUpAnalysisState state = new PushUpAnalysisState();

        repeat(state, new PushUpMetrics(170, 175));
        repeat(state, new PushUpMetrics(140, 170));
        repeat(state, new PushUpMetrics(85, 140));

        assertThat(state.getPhase()).isEqualTo(PushUpPhase.UP);
        assertThat(state.getValidCount()).isZero();
    }

    @Test
    void countsWithRelaxedElbowAngles() {
        PushUpAnalysisState state = new PushUpAnalysisState();

        repeat(state, new PushUpMetrics(152, 170));
        repeat(state, new PushUpMetrics(142, 170));
        repeat(state, new PushUpMetrics(102, 165));
        repeat(state, new PushUpMetrics(118, 165));
        repeat(state, new PushUpMetrics(152, 170));

        assertThat(state.getPhase()).isEqualTo(PushUpPhase.UP);
        assertThat(state.getValidCount()).isEqualTo(1);
    }

    private void repeat(PushUpAnalysisState state, PushUpMetrics metrics) {
        for (int frame = 0; frame < 3; frame++) {
            analyzer.analyzeAndCount(state, metrics);
        }
    }
}
