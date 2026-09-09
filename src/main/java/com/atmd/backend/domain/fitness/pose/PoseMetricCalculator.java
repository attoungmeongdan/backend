package com.atmd.backend.domain.fitness.pose;

import com.atmd.backend.domain.fitness.dto.request.LandmarkDto;
import org.springframework.stereotype.Component;

@Component
public class PoseMetricCalculator {

    public double calculateAngle(LandmarkDto first, LandmarkDto vertex, LandmarkDto third) {
        double firstX = first.x() - vertex.x();
        double firstY = first.y() - vertex.y();
        double secondX = third.x() - vertex.x();
        double secondY = third.y() - vertex.y();

        double firstLength = Math.hypot(firstX, firstY);
        double secondLength = Math.hypot(secondX, secondY);
        if (firstLength == 0 || secondLength == 0) {
            throw new IllegalArgumentException("Cannot calculate an angle from overlapping landmarks");
        }

        double cosine = (firstX / firstLength) * (secondX / secondLength)
                + (firstY / firstLength) * (secondY / secondLength);
        double boundedCosine = Math.max(-1.0, Math.min(1.0, cosine));
        return Math.toDegrees(Math.acos(boundedCosine));
    }
}
