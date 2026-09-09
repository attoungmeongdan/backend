package com.atmd.backend.domain.fitness.analysis;

import com.atmd.backend.domain.fitness.enums.ChairStandPhase;
import com.atmd.backend.domain.fitness.pose.PoseMetricCalculator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChairStandAnalyzerTest {
    private final ChairStandAnalyzer analyzer = new ChairStandAnalyzer(new PoseMetricCalculator());

    @Test
    void countsOnlyAfterACompleteChairStandCycle() {
        ChairStandAnalysisState state = new ChairStandAnalysisState();

        repeat(state, new ChairStandMetrics(90, 90));
        assertThat(state.getPhase()).isEqualTo(ChairStandPhase.SITTING);

        repeat(state, new ChairStandMetrics(130, 130));
        repeat(state, new ChairStandMetrics(170, 160));
        assertThat(state.getPhase()).isEqualTo(ChairStandPhase.STANDING);
        assertThat(state.getValidCount()).isZero();

        repeat(state, new ChairStandMetrics(140, 140));
        repeat(state, new ChairStandMetrics(90, 90));

        assertThat(state.getPhase()).isEqualTo(ChairStandPhase.SITTING);
        assertThat(state.getValidCount()).isEqualTo(1);
    }

    @Test
    void doesNotCountAnIncompleteMovement() {
        ChairStandAnalysisState state = new ChairStandAnalysisState();

        repeat(state, new ChairStandMetrics(90, 90));
        repeat(state, new ChairStandMetrics(130, 130));
        repeat(state, new ChairStandMetrics(170, 160));

        assertThat(state.getValidCount()).isZero();
    }

    private void repeat(ChairStandAnalysisState state, ChairStandMetrics metrics) {
        for (int frame = 0; frame < 3; frame++) {
            analyzer.analyzeAndCount(state, metrics);
        }
    }
}
