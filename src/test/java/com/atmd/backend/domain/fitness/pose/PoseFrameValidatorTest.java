package com.atmd.backend.domain.fitness.pose;

import com.atmd.backend.domain.fitness.dto.request.LandmarkDto;
import com.atmd.backend.domain.fitness.dto.request.PoseFrameMessage;
import com.atmd.backend.domain.fitness.enums.ExerciseType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PoseFrameValidatorTest {
    private final PoseFrameValidator validator = new PoseFrameValidator();

    @Test
    void rejectsCoordinateThatCanOverflowDownstreamArithmetic() {
        List<LandmarkDto> landmarks = visibleLandmarks();
        landmarks.set(0, new LandmarkDto(0, Double.MAX_VALUE, 0, 0, 1, null));
        PoseFrameMessage frame = new PoseFrameMessage(
                "POSE_FRAME", 1L, ExerciseType.PLANK, 1, 1, false, landmarks, null
        );

        PoseFrameValidator.ValidationResult result = validator.validate(
                frame, 1L, ExerciseType.PLANK, -1, -1
        );

        assertThat(result.valid()).isFalse();
        assertThat(result.code()).isEqualTo("INVALID_LANDMARK");
    }

    @Test
    void rejectsSmoothedLandmarksWhenRequiredSideIsNoLongerVisible() {
        List<LandmarkDto> landmarks = visibleLandmarks();
        for (int index : List.of(11, 12, 23, 24, 25, 26, 27, 28)) {
            LandmarkDto landmark = landmarks.get(index);
            landmarks.set(index, new LandmarkDto(index, landmark.x(), landmark.y(), 0, 0.59, null));
        }

        assertThat(validator.hasRequiredVisibility(landmarks, ExerciseType.PLANK)).isFalse();
    }

    private List<LandmarkDto> visibleLandmarks() {
        List<LandmarkDto> landmarks = new ArrayList<>();
        for (int index = 0; index < 33; index++) {
            landmarks.add(new LandmarkDto(index, index / 100.0, index / 100.0, 0, 1, null));
        }
        return landmarks;
    }
}
