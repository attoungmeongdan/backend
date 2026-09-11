package com.atmd.backend.domain.group.service;

import com.atmd.backend.domain.fitness.enums.ExerciseType;
import com.atmd.backend.domain.group.dto.response.GroupDailyWorkoutBarResponseDTO;
import com.atmd.backend.domain.group.dto.response.GroupExerciseBarSectionDTO;
import com.atmd.backend.domain.group.dto.response.GroupExerciseMemberValueDTO;
import com.atmd.backend.domain.group.dto.response.GroupMemberDailyRecordDTO;
import com.atmd.backend.domain.group.dto.response.GroupMonthlyMemberSummaryDTO;
import com.atmd.backend.domain.group.dto.response.GroupMonthlyWorkoutBarResponseDTO;
import com.atmd.backend.domain.group.entity.Group;
import com.atmd.backend.domain.group.entity.GroupUser;
import com.atmd.backend.domain.group.exception.GroupErrorCode;
import com.atmd.backend.domain.group.repository.GroupDailyRecordItemProjection;
import com.atmd.backend.domain.group.repository.GroupDailyWorkoutRecordRepository;
import com.atmd.backend.domain.group.repository.GroupMemberWorkoutAggregateProjection;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToLongFunction;

@Service
@RequiredArgsConstructor
public class GroupWorkoutRecordService {
    private static final ZoneId SERVER_ZONE = ZoneId.of("Asia/Seoul");
    private static final String COUNT_UNIT = "COUNT";
    private static final String MS_UNIT = "MS";

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

        List<GroupMemberWorkoutAggregateProjection> rows =
                groupDailyWorkoutRecordRepository.aggregateByGroupAndDate(groupId, targetDate);

        List<GroupExerciseBarSectionDTO> exercises = List.of(
                buildExerciseSection(ExerciseType.CHAIR_STAND, COUNT_UNIT, rows, this::chairStand),
                buildExerciseSection(ExerciseType.PUSH_UP,     COUNT_UNIT, rows, this::pushUp),
                buildExerciseSection(ExerciseType.SIT_UP,      COUNT_UNIT, rows, this::sitUp),
                buildExerciseSection(ExerciseType.PLANK,       MS_UNIT,    rows, this::plankMs)
        );

        return GroupDailyWorkoutBarResponseDTO.builder()
                .groupId(group.getId())
                .groupName(group.getName())
                .date(targetDate)
                .exercises(exercises)
                .build();
    }

    @Transactional(readOnly = true)
    public GroupMonthlyWorkoutBarResponseDTO getMonthlyBar(Long groupId, Long viewerId, YearMonth yearMonth) {
        Group group = findGroupAndCheckMembership(groupId, viewerId);
        YearMonth targetMonth = yearMonth != null ? yearMonth : YearMonth.now(SERVER_ZONE);
        LocalDate startDate = targetMonth.atDay(1);
        LocalDate endDateExclusive = targetMonth.plusMonths(1).atDay(1);
        int daysInMonth = targetMonth.lengthOfMonth();
        Long ownerId = group.getOwner().getId();

        List<GroupUser> memberships = groupUserRepository.findAllByGroupIdWithUser(groupId);
        List<GroupDailyRecordItemProjection> items =
                groupDailyWorkoutRecordRepository.findItemsByGroupAndDateRange(groupId, startDate, endDateExclusive);

        Map<Long, List<GroupDailyRecordItemProjection>> itemsByUserId = new HashMap<>();
        for (GroupDailyRecordItemProjection item : items) {
            itemsByUserId.computeIfAbsent(item.getUserId(), k -> new ArrayList<>()).add(item);
        }

        List<GroupMonthlyMemberSummaryDTO> summaries = new ArrayList<>();
        for (GroupUser membership : memberships) {
            Long userId = membership.getUser().getId();
            List<GroupDailyRecordItemProjection> userItems =
                    itemsByUserId.getOrDefault(userId, List.of());
            summaries.add(buildMemberMonthlySummary(
                    membership, ownerId, userItems, daysInMonth));
        }

        summaries.sort(Comparator
                .comparingDouble(GroupMonthlyMemberSummaryDTO::getExecutionRate).reversed()
                .thenComparingInt(GroupMonthlyMemberSummaryDTO::getTotalExerciseTypes).reversed()
                .thenComparing(GroupMonthlyMemberSummaryDTO::getUserId));

        List<GroupMonthlyMemberSummaryDTO> ranked = new ArrayList<>(summaries.size());
        for (int i = 0; i < summaries.size(); i++) {
            GroupMonthlyMemberSummaryDTO s = summaries.get(i);
            ranked.add(GroupMonthlyMemberSummaryDTO.builder()
                    .rank(i + 1)
                    .userId(s.getUserId())
                    .nickname(s.getNickname())
                    .isOwner(s.isOwner())
                    .executionRate(s.getExecutionRate())
                    .executedDays(s.getExecutedDays())
                    .totalExerciseTypes(s.getTotalExerciseTypes())
                    .days(s.getDays())
                    .build());
        }

        return GroupMonthlyWorkoutBarResponseDTO.builder()
                .groupId(group.getId())
                .groupName(group.getName())
                .yearMonth(targetMonth)
                .daysInMonth(daysInMonth)
                .members(ranked)
                .build();
    }

    private GroupExerciseBarSectionDTO buildExerciseSection(
            ExerciseType type,
            String unit,
            List<GroupMemberWorkoutAggregateProjection> rows,
            ToLongFunction<GroupMemberWorkoutAggregateProjection> valueFn
    ) {
        List<GroupExerciseMemberValueDTO> values = new ArrayList<>(rows.size());
        long topValue = 0L;
        for (GroupMemberWorkoutAggregateProjection r : rows) {
            long value = valueFn.applyAsLong(r);
            if (value > topValue) topValue = value;
            values.add(GroupExerciseMemberValueDTO.builder()
                    .userId(r.getUserId())
                    .nickname(r.getNickname())
                    .isOwner(Boolean.TRUE.equals(r.getIsOwner()))
                    .value(value)
                    .build());
        }
        values.sort(Comparator
                .comparingLong(GroupExerciseMemberValueDTO::getValue).reversed()
                .thenComparing(GroupExerciseMemberValueDTO::getUserId));
        return GroupExerciseBarSectionDTO.builder()
                .type(type)
                .unit(unit)
                .topValue(topValue)
                .members(values)
                .build();
    }

    private GroupMonthlyMemberSummaryDTO buildMemberMonthlySummary(
            GroupUser membership,
            Long ownerId,
            List<GroupDailyRecordItemProjection> userItems,
            int daysInMonth
    ) {
        List<GroupMemberDailyRecordDTO> days = new ArrayList<>(userItems.size());
        int executedDays = 0;
        int totalExerciseTypes = 0;
        for (GroupDailyRecordItemProjection item : userItems) {
            long chair = zero(item.getChairStandCount());
            long push  = zero(item.getPushUpCount());
            long sit   = zero(item.getSitUpCount());
            long plank = zero(item.getPlankDurationMs());
            int typeCount = (chair > 0 ? 1 : 0) + (push > 0 ? 1 : 0)
                          + (sit   > 0 ? 1 : 0) + (plank > 0 ? 1 : 0);
            if (typeCount == 0) continue;
            executedDays++;
            totalExerciseTypes += typeCount;
            days.add(GroupMemberDailyRecordDTO.builder()
                    .date(item.getWorkoutDate())
                    .exerciseTypeCount(typeCount)
                    .chairStandCount(chair)
                    .pushUpCount(push)
                    .sitUpCount(sit)
                    .plankDurationMs(plank)
                    .build());
        }
        days.sort(Comparator.comparing(GroupMemberDailyRecordDTO::getDate));

        double executionRate = daysInMonth == 0
                ? 0.0
                : totalExerciseTypes * 100.0 / (daysInMonth * 4);

        Long userId = membership.getUser().getId();
        return GroupMonthlyMemberSummaryDTO.builder()
                .rank(0)
                .userId(userId)
                .nickname(membership.getUser().getNickname())
                .isOwner(userId.equals(ownerId))
                .executionRate(round1(executionRate))
                .executedDays(executedDays)
                .totalExerciseTypes(totalExerciseTypes)
                .days(days)
                .build();
    }

    private long chairStand(GroupMemberWorkoutAggregateProjection r) { return zero(r.getChairStandCount()); }
    private long pushUp(GroupMemberWorkoutAggregateProjection r)     { return zero(r.getPushUpCount()); }
    private long sitUp(GroupMemberWorkoutAggregateProjection r)      { return zero(r.getSitUpCount()); }
    private long plankMs(GroupMemberWorkoutAggregateProjection r)    { return zero(r.getPlankDurationMs()); }

    private long zero(Long v) { return v == null ? 0L : v; }

    private double round1(double v) { return Math.round(v * 10.0) / 10.0; }

    private Group findGroupAndCheckMembership(Long groupId, Long userId) {
        Group group = groupRepository.findByIdAndIsDeletedFalse(groupId)
                .orElseThrow(() -> new GeneralException(GroupErrorCode.GROUP_NOT_FOUND));
        if (!groupUserRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new GeneralException(GroupErrorCode.ACCESS_DENIED);
        }
        return group;
    }
}
