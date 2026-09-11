package com.atmd.backend.domain.group.dto.response;

import com.atmd.backend.domain.fitness.enums.ExerciseType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GroupExerciseBarSectionDTO {

    private ExerciseType type;
    private String unit;
    private long topValue;
    private List<GroupExerciseMemberValueDTO> members;
}
