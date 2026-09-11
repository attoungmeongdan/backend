package com.atmd.backend.domain.fitness.analysis;

import com.atmd.backend.domain.fitness.enums.SitUpPhase;
import com.atmd.backend.domain.fitness.pose.PoseMetricCalculator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SitUpAnalyzerTest {
    private final SitUpAnalyzer analyzer = new SitUpAnalyzer(new PoseMetricCalculator());

    @Test
    void countsAfterACompleteSitUpCycle() {
        SitUpAnalysisState state = new SitUpAnalysisState();

        repeat(state, 165);
        repeat(state, 130);
        repeat(state, 90);
        assertThat(state.getPhase()).isEqualTo(SitUpPhase.UP);
        assertThat(state.getValidCount()).isZero();

        repeat(state, 120);
        repeat(state, 160);

        assertThat(state.getPhase()).isEqualTo(SitUpPhase.LYING);
        assertThat(state.getValidCount()).isEqualTo(1);
    }

    @Test
    void doesNotCountWhenUpperPositionIsNotReached() {
        SitUpAnalysisState state = new SitUpAnalysisState();

        repeat(state, 165);
        repeat(state, 130);
        repeat(state, 120);
        repeat(state, 160);

        assertThat(state.getPhase()).isEqualTo(SitUpPhase.RISING);
        assertThat(state.getValidCount()).isZero();
    }

    @Test
    void countsWithRelaxedHipAngles() {
        SitUpAnalysisState state = new SitUpAnalysisState();

        repeat(state, 146);
        repeat(state, 130);
        repeat(state, 113);
        repeat(state, 122);
        repeat(state, 146);

        assertThat(state.getPhase()).isEqualTo(SitUpPhase.LYING);
        assertThat(state.getValidCount()).isEqualTo(1);
    }

    private void repeat(SitUpAnalysisState state, double angle) {
        for (int frame = 0; frame < 3; frame++) {
            analyzer.analyzeAndCount(state, new SitUpMetrics(angle));
        }
    }
}
