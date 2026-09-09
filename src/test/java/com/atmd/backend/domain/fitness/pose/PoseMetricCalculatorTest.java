package com.atmd.backend.domain.fitness.pose;

import com.atmd.backend.domain.fitness.dto.request.LandmarkDto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PoseMetricCalculatorTest {
    private final PoseMetricCalculator calculator = new PoseMetricCalculator();

    @Test
    void calculatesRightAngle() {
        LandmarkDto first = landmark(0, 1, 0);
        LandmarkDto vertex = landmark(1, 0, 0);
        LandmarkDto third = landmark(2, 0, 1);

        assertThat(calculator.calculateAngle(first, vertex, third)).isCloseTo(90.0, within(0.001));
    }

    @Test
    void rejectsOverlappingLandmarks() {
        LandmarkDto overlapping = landmark(0, 0, 0);

        assertThatThrownBy(() -> calculator.calculateAngle(
                overlapping,
                overlapping,
                landmark(1, 1, 0)
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void calculatesAngleWithoutOverflowForLargeFiniteCoordinates() {
        double large = Math.sqrt(Double.MAX_VALUE / 8.0);
        LandmarkDto vertex = landmark(0, 0, 0);

        double angle = calculator.calculateAngle(
                landmark(1, large, 0),
                vertex,
                landmark(2, 0, large)
        );

        assertThat(angle).isCloseTo(90.0, within(0.001));
    }

    private LandmarkDto landmark(int index, double x, double y) {
        return new LandmarkDto(index, x, y, 0, 1, 1.0);
    }

    private org.assertj.core.data.Offset<Double> within(double value) {
        return org.assertj.core.data.Offset.offset(value);
    }
}
