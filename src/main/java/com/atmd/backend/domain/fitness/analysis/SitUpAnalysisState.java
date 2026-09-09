package com.atmd.backend.domain.fitness.analysis;

import com.atmd.backend.domain.fitness.enums.SitUpPhase;
import lombok.Getter;

@Getter
public class SitUpAnalysisState {
    private SitUpPhase phase = SitUpPhase.UNKNOWN;
    private SitUpPhase candidatePhase;
    private int candidateFrames;
    private int validCount;
    private int invalidCount;

    public boolean confirm(SitUpPhase next, int requiredFrames) {
        if (candidatePhase != next) {
            candidatePhase = next;
            candidateFrames = 1;
            return false;
        }
        if (++candidateFrames < requiredFrames) {
            return false;
        }
        phase = next;
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
