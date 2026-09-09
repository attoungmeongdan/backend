package com.atmd.backend.domain.fitness.analysis;

import com.atmd.backend.domain.fitness.enums.ChairStandPhase;
import lombok.Getter;

@Getter
public class ChairStandAnalysisState {
    private ChairStandPhase phase = ChairStandPhase.UNKNOWN;
    private ChairStandPhase candidatePhase;
    private int candidateFrames;
    private int validCount;
    private int invalidCount;

    public boolean confirm(ChairStandPhase nextPhase, int requiredFrames) {
        if (candidatePhase != nextPhase) {
            candidatePhase = nextPhase;
            candidateFrames = 1;
            return false;
        }

        candidateFrames++;
        if (candidateFrames < requiredFrames) {
            return false;
        }

        phase = nextPhase;
        candidatePhase = null;
        candidateFrames = 0;
        return true;
    }

    public void clearCandidate() {
        candidatePhase = null;
        candidateFrames = 0;
    }

    public void incrementValidCount() {
        validCount++;
    }
}
