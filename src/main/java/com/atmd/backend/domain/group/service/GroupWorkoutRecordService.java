package com.atmd.backend.domain.group.service;

import com.atmd.backend.domain.fitness.enums.ExerciseType;
import com.atmd.backend.domain.group.dto.response.GroupDailyWorkoutBarResponseDTO;
import com.atmd.backend.domain.group.dto.response.GroupMemberWorkoutBarResponseDTO;
import com.atmd.backend.domain.group.dto.response.GroupMonthlyWorkoutBarResponseDTO;
import com.atmd.backend.domain.group.entity.Group;
import com.atmd.backend.domain.group.entity.GroupUser;
import com.atmd.backend.domain.group.exception.GroupErrorCode;
import com.atmd.backend.domain.group.repository.GroupDailyWorkoutRecordRepository;
import com.atmd.backend.domain.group.repository.GroupRepository;
import com.atmd.backend.domain.group.repository.GroupUserRepository;
import com.atmd.backend.global.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupWorkoutRecordService {
    private static final ZoneId SERVER_ZONE = ZoneId.of("Asia/Seoul");

    private final GroupRepository groupRepository;
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

    @Transactional(readOnly = true)
    public GroupDailyWorkoutBarResponseDTO getDailyBar(Long groupId, Long viewerId, LocalDate date) {
        Group group = findGroupAndCheckMembership(groupId, viewerId);
        LocalDate targetDate = date != null ? date : LocalDate.now(SERVER_ZONE);
        List<GroupMemberWorkoutBarResponseDTO> members = groupDailyWorkoutRecordRepository
                .aggregateByGroupAndDate(groupId, targetDate)
                .stream()
                .map(GroupMemberWorkoutBarResponseDTO::from)
                .toList();
        return GroupDailyWorkoutBarResponseDTO.builder()
                .groupId(group.getId())
                .groupName(group.getName())
                .date(targetDate)
                .members(members)
                .build();
    }

    @Transactional(readOnly = true)
    public GroupMonthlyWorkoutBarResponseDTO getMonthlyBar(Long groupId, Long viewerId, YearMonth yearMonth) {
        Group group = findGroupAndCheckMembership(groupId, viewerId);
        YearMonth targetMonth = yearMonth != null ? yearMonth : YearMonth.now(SERVER_ZONE);
        LocalDate startDate = targetMonth.atDay(1);
        LocalDate endDateExclusive = targetMonth.plusMonths(1).atDay(1);
        List<GroupMemberWorkoutBarResponseDTO> members = groupDailyWorkoutRecordRepository
                .aggregateByGroupAndDateRange(groupId, startDate, endDateExclusive)
                .stream()
                .map(GroupMemberWorkoutBarResponseDTO::from)
                .toList();
        return GroupMonthlyWorkoutBarResponseDTO.builder()
                .groupId(group.getId())
                .groupName(group.getName())
                .yearMonth(targetMonth)
                .members(members)
                .build();
    }

    private Group findGroupAndCheckMembership(Long groupId, Long userId) {
        Group group = groupRepository.findByIdAndIsDeletedFalse(groupId)
                .orElseThrow(() -> new GeneralException(GroupErrorCode.GROUP_NOT_FOUND));
        if (!groupUserRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new GeneralException(GroupErrorCode.ACCESS_DENIED);
        }
        return group;
    }
}
