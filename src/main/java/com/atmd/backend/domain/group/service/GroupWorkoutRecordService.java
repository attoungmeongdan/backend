package com.atmd.backend.domain.group.service;

import com.atmd.backend.domain.fitness.enums.ExerciseType;
import com.atmd.backend.domain.group.entity.GroupUser;
import com.atmd.backend.domain.group.repository.GroupDailyWorkoutRecordRepository;
import com.atmd.backend.domain.group.repository.GroupUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class GroupWorkoutRecordService {
    private static final ZoneId SERVER_ZONE = ZoneId.of("Asia/Seoul");

    private final GroupUserRepository groupUserRepository;
    private final GroupDailyWorkoutRecordRepository groupDailyWorkoutRecordRepository;

    public void accumulateCompletedWorkout(
            Long userId,
            ExerciseType exerciseType,
            int validCount,
            long validDurationMs,
            Instant completedAt
    ) {
        LocalDateTime completedDateTime = LocalDateTime.ofInstant(completedAt, SERVER_ZONE);
        LocalDate workoutDate = completedDateTime.toLocalDate();
        long chairStandCount = exerciseType == ExerciseType.CHAIR_STAND ? validCount : 0;
        long pushUpCount = exerciseType == ExerciseType.PUSH_UP ? validCount : 0;
        long sitUpCount = exerciseType == ExerciseType.SIT_UP ? validCount : 0;
        long plankDurationMs = exerciseType == ExerciseType.PLANK ? validDurationMs : 0;

        for (GroupUser membership : groupUserRepository.findAllByUserIdWithGroup(userId)) {
            if (membership.getCreatedAt().isAfter(completedDateTime)) {
                continue;
            }
            groupDailyWorkoutRecordRepository.accumulate(
                    membership.getGroup().getId(),
                    userId,
                    workoutDate,
                    chairStandCount,
                    pushUpCount,
                    sitUpCount,
                    plankDurationMs
            );
        }
    }
}
