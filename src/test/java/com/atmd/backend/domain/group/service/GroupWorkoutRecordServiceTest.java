package com.atmd.backend.domain.group.service;

import com.atmd.backend.domain.fitness.enums.ExerciseType;
import com.atmd.backend.domain.group.entity.Group;
import com.atmd.backend.domain.group.entity.GroupUser;
import com.atmd.backend.domain.group.repository.GroupDailyWorkoutRecordRepository;
import com.atmd.backend.domain.group.repository.GroupRepository;
import com.atmd.backend.domain.group.repository.GroupUserRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GroupWorkoutRecordServiceTest {
    private final GroupRepository groupRepository = mock(GroupRepository.class);
    private final GroupUserRepository groupUserRepository = mock(GroupUserRepository.class);
    private final GroupDailyWorkoutRecordRepository recordRepository =
            mock(GroupDailyWorkoutRecordRepository.class);
    private final GroupWorkoutRecordService service =
            new GroupWorkoutRecordService(groupRepository, groupUserRepository, recordRepository);

    @Test
    void accumulatesSitUpCountForMembershipCreatedBeforeWorkout() {
        GroupUser membership = membership(10L, LocalDateTime.of(2026, 9, 12, 8, 0));
        when(groupUserRepository.findAllByUserIdWithGroup(1L)).thenReturn(List.of(membership));

        service.accumulateCompletedWorkout(
                1L, ExerciseType.SIT_UP, 6, 0,
                Instant.parse("2026-09-12T01:00:00Z")
        );

        verify(recordRepository).accumulate(
                10L, 1L, LocalDate.of(2026, 9, 12), 0, 0, 6, 0
        );
    }

    @Test
    void ignoresWorkoutCompletedBeforeMembership() {
        GroupUser membership = membership(10L, LocalDateTime.of(2026, 9, 12, 11, 0));
        when(groupUserRepository.findAllByUserIdWithGroup(1L)).thenReturn(List.of(membership));

        service.accumulateCompletedWorkout(
                1L, ExerciseType.SIT_UP, 6, 0,
                Instant.parse("2026-09-12T01:00:00Z")
        );

        verify(recordRepository, never()).accumulate(
                10L, 1L, LocalDate.of(2026, 9, 12), 0, 0, 6, 0
        );
    }

    private GroupUser membership(Long groupId, LocalDateTime joinedAt) {
        Group group = mock(Group.class);
        when(group.getId()).thenReturn(groupId);
        GroupUser membership = mock(GroupUser.class);
        when(membership.getGroup()).thenReturn(group);
        when(membership.getCreatedAt()).thenReturn(joinedAt);
        return membership;
    }
}
