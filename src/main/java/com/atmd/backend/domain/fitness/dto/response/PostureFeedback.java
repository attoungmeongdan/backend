package com.atmd.backend.domain.fitness.dto.response;

public record PostureFeedback(
        String code,
        String severity,
        String message
) {
    public static PostureFeedback positioningRequired() {
        return new PostureFeedback(
                "POSITION_REQUIRED",
                "WARNING",
                "어깨, 엉덩이, 무릎과 발목이 보이도록 카메라 위치를 조정해주세요."
        );
    }

    public static PostureFeedback positioningRequired(String exerciseType) {
        String message = "PUSH_UP".equals(exerciseType)
                ? "어깨, 팔꿈치, 손목, 엉덩이와 발목이 보이도록 측면에서 촬영해주세요."
                : "어깨, 엉덩이, 무릎과 발목이 보이도록 카메라 위치를 조정해주세요.";
        return new PostureFeedback("POSITION_REQUIRED", "WARNING", message);
    }

    public static PostureFeedback plankPostureBroken() {
        return new PostureFeedback(
                "PLANK_POSTURE_BROKEN",
                "ERROR",
                "플랭크 자세가 무너져 측정을 종료했습니다."
        );
    }
}
