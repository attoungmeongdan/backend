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
        frames.addLast(List.copyOf(current));
        while (frames.size() > WINDOW_SIZE) {
            frames.removeFirst();
        }

        List<LandmarkDto> smoothed = new ArrayList<>(current.size());
        for (int index = 0; index < current.size(); index++) {
            double x = 0;
            double y = 0;
            double z = 0;
            double visibility = 0;
            for (List<LandmarkDto> frame : frames) {
                LandmarkDto landmark = frame.get(index);
                x += landmark.x();
                y += landmark.y();
                z += landmark.z();
                visibility += landmark.visibility();
            }
            int size = frames.size();
            smoothed.add(new LandmarkDto(index, x / size, y / size, z / size, visibility / size, null));
        }
        return smoothed;
    }
}
