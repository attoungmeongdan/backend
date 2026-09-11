package com.atmd.backend.domain.fitness.pose;

import com.atmd.backend.domain.fitness.dto.request.LandmarkDto;
import com.atmd.backend.domain.fitness.dto.request.PoseFrameMessage;
import com.atmd.backend.domain.fitness.enums.ExerciseType;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class PoseFrameValidator {
    private static final int LANDMARK_COUNT = 33;
    private static final double MIN_VISIBILITY = 0.6;
    private static final double XY_ABSOLUTE_MAX = Math.sqrt(Double.MAX_VALUE / 8.0);
    private static final double Z_ABSOLUTE_MAX = Double.MAX_VALUE / 5.0;

    private static final Set<Integer> CHAIR_STAND_REQUIRED = Set.of(
            PoseLandmarkIndex.LEFT_SHOULDER,
            PoseLandmarkIndex.RIGHT_SHOULDER,
            PoseLandmarkIndex.LEFT_HIP,
            PoseLandmarkIndex.RIGHT_HIP,
            PoseLandmarkIndex.LEFT_KNEE,
            PoseLandmarkIndex.RIGHT_KNEE,
            PoseLandmarkIndex.LEFT_ANKLE,
            PoseLandmarkIndex.RIGHT_ANKLE
    );

    private static final Set<Integer> PUSH_UP_REQUIRED = Set.of(11, 12, 13, 14, 15, 16, 23, 24, 27, 28);
    private static final Set<Integer> SIT_UP_REQUIRED = Set.of(11, 12, 23, 24, 25, 26);
    private static final Set<Integer> PLANK_REQUIRED = Set.of(11, 12, 13, 14, 15, 16, 23, 24, 25, 26, 27, 28);

    public ValidationResult validate(
            PoseFrameMessage frame,
            Long expectedSessionId,
            ExerciseType expectedExerciseType,
            long previousSequence,
            long previousTimestamp
    ) {
        if (frame == null
                || !"POSE_FRAME".equals(frame.type())
                || !expectedSessionId.equals(frame.sessionId())
                || frame.exerciseType() != expectedExerciseType
                || frame.sequence() <= previousSequence
                || frame.timestamp() <= previousTimestamp
                || frame.landmarks() == null
                || frame.landmarks().size() != LANDMARK_COUNT) {
            return ValidationResult.invalid("INVALID_FRAME", "관절 프레임 형식이 올바르지 않습니다.");
        }

        Set<Integer> indexes = new HashSet<>();
        for (int position = 0; position < frame.landmarks().size(); position++) {
            LandmarkDto landmark = frame.landmarks().get(position);
            if (!isValidLandmark(landmark)
                    || landmark.index() != position
                    || !indexes.add(landmark.index())) {
                return ValidationResult.invalid("INVALID_LANDMARK", "관절 좌표가 올바르지 않습니다.");
            }
        }

        if (indexes.size() != LANDMARK_COUNT
                || !indexes.containsAll(java.util.stream.IntStream.range(0, LANDMARK_COUNT).boxed().toList())) {
            return ValidationResult.invalid("MISSING_LANDMARK", "33개 관절 좌표가 모두 필요합니다.");
        }

        if (!hasRequiredVisibility(frame.landmarks(), expectedExerciseType)) {
            return ValidationResult.forPositionRequired();
        }

        return ValidationResult.success();
    }

    public boolean hasRequiredVisibility(List<LandmarkDto> landmarks, ExerciseType exerciseType) {
        if (landmarks == null || landmarks.size() != LANDMARK_COUNT) {
            return false;
        }

        Set<Integer> required = switch (exerciseType) {
            case PUSH_UP -> PUSH_UP_REQUIRED;
            case SIT_UP -> SIT_UP_REQUIRED;
            case PLANK -> PLANK_REQUIRED;
            default -> CHAIR_STAND_REQUIRED;
        };
        long visibleRequired = landmarks.stream()
                .filter(landmark -> required.contains(landmark.index()))
                .filter(landmark -> landmark.visibility() >= MIN_VISIBILITY)
                .count();

        boolean leftVisible = switch (exerciseType) {
            case PUSH_UP -> sideVisible(landmarks, 11, 13, 15, 23, 27);
            case SIT_UP -> sideVisible(landmarks, 11, 23, 25);
            case PLANK -> sideVisible(landmarks, 11, 13, 15, 23, 25, 27);
            default -> sideVisible(landmarks, 11, 23, 25, 27);
        };
        boolean rightVisible = switch (exerciseType) {
            case PUSH_UP -> sideVisible(landmarks, 12, 14, 16, 24, 28);
            case SIT_UP -> sideVisible(landmarks, 12, 24, 26);
            case PLANK -> sideVisible(landmarks, 12, 14, 16, 24, 26, 28);
            default -> sideVisible(landmarks, 12, 24, 26, 28);
        };
        int minimumVisible = switch (exerciseType) {
            case PUSH_UP -> 5;
            case SIT_UP -> 3;
            case PLANK -> 6;
            default -> 4;
        };
        return visibleRequired >= minimumVisible && (leftVisible || rightVisible);
    }

    private boolean sideVisible(List<LandmarkDto> landmarks, int... indexes) {
        for (int index : indexes) {
            LandmarkDto landmark = landmarks.get(index);
            if (landmark.index() != index || landmark.visibility() < MIN_VISIBILITY) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidLandmark(LandmarkDto landmark) {
        return landmark != null
                && landmark.index() >= 0
                && landmark.index() < LANDMARK_COUNT
                && Double.isFinite(landmark.x())
                && Double.isFinite(landmark.y())
                && Double.isFinite(landmark.z())
                && Math.abs(landmark.x()) <= XY_ABSOLUTE_MAX
                && Math.abs(landmark.y()) <= XY_ABSOLUTE_MAX
                && Math.abs(landmark.z()) <= Z_ABSOLUTE_MAX
                && Double.isFinite(landmark.visibility())
                && landmark.visibility() >= 0
                && landmark.visibility() <= 1;
    }

    public record ValidationResult(boolean valid, boolean positionRequired, String code, String message) {
        public static ValidationResult success() {
            return new ValidationResult(true, false, null, null);
        }

        public static ValidationResult invalid(String code, String message) {
            return new ValidationResult(false, false, code, message);
        }

        public static ValidationResult forPositionRequired() {
            return new ValidationResult(false, true, "POSITION_REQUIRED", "필수 관절이 충분히 보이지 않습니다.");
        }
    }
}
