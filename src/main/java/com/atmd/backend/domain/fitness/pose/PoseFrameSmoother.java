package com.atmd.backend.domain.fitness.pose;

import com.atmd.backend.domain.fitness.dto.request.LandmarkDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@Component
public class PoseFrameSmoother {
    private static final int WINDOW_SIZE = 5;

    public List<LandmarkDto> addAndSmooth(Deque<List<LandmarkDto>> frames, List<LandmarkDto> current) {
        return addAndSmooth(frames, current, WINDOW_SIZE);
    }

    public List<LandmarkDto> addAndSmooth(
            Deque<List<LandmarkDto>> frames,
            List<LandmarkDto> current,
            int windowSize
    ) {
        frames.addLast(List.copyOf(current));
        while (frames.size() > windowSize) {
            frames.removeFirst();
        }

        List<LandmarkDto> smoothed = new ArrayList<>(current.size());
        for (int index = 0; index < current.size(); index++) {
            double x = 0;
            double y = 0;
            double z = 0;
            double visibility = 0;
            int sampleCount = 0;
            for (List<LandmarkDto> frame : frames) {
                LandmarkDto landmark = frame.get(index);
                sampleCount++;
                x += (landmark.x() - x) / sampleCount;
                y += (landmark.y() - y) / sampleCount;
                z += (landmark.z() - z) / sampleCount;
                visibility += (landmark.visibility() - visibility) / sampleCount;
            }
            smoothed.add(new LandmarkDto(index, x, y, z, visibility, null));
        }
        return smoothed;
    }
}
