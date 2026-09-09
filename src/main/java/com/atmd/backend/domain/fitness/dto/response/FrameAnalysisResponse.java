package com.atmd.backend.domain.fitness.dto.response;

import java.util.List;
import java.util.Map;

public record FrameAnalysisResponse(
        String type,
        Long sessionId,
        long sequence,
        String phase,
        int validCount,
        int invalidCount,
        long remainingTimeMs,
        long validDurationMs,
        Map<String, Double> metrics,
        List<PostureFeedback> feedback
) {
}
