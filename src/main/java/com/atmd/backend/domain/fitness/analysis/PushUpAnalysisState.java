package com.atmd.backend.domain.fitness.analysis;

import com.atmd.backend.domain.fitness.enums.PushUpPhase;
import lombok.Getter;

@Getter
public class PushUpAnalysisState {
    private PushUpPhase phase = PushUpPhase.UNKNOWN;
    private PushUpPhase candidatePhase;
    private int candidateFrames;
    private int validCount;
    private int invalidCount;

    public boolean confirm(PushUpPhase next, int requiredFrames) {
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
