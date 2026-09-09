package com.atmd.backend.domain.fitness.analysis;

import com.atmd.backend.domain.fitness.enums.PlankPhase;
import lombok.Getter;

@Getter
public class PlankAnalysisState {
    private PlankPhase phase = PlankPhase.POSITIONING;
    private int validFrames;
    private int brokenFrames;

    public boolean confirmHolding(int requiredFrames) {
        brokenFrames = 0;
        if (phase != PlankPhase.POSITIONING || ++validFrames < requiredFrames) {
            return false;
        }
        phase = PlankPhase.HOLDING;
        validFrames = 0;
        return true;
    }

    public boolean confirmBroken(int requiredFrames) {
        validFrames = 0;
        if (phase != PlankPhase.HOLDING || ++brokenFrames < requiredFrames) {
            return false;
        }
        phase = PlankPhase.BROKEN;
        brokenFrames = 0;
        return true;
    }

    public void clearCandidates() {
        validFrames = 0;
        brokenFrames = 0;
    }
}
