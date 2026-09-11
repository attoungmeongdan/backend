package com.atmd.backend.domain.group.repository;

import com.atmd.backend.domain.group.entity.GroupUser;
import com.atmd.backend.domain.group.entity.enums.GroupMembership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupUserRepository extends JpaRepository<GroupUser, Long> {

    boolean existsByGroupIdAndUserId(Long groupId, Long userId);

    long countByGroupId(Long groupId);

    long countByUserId(Long userId);

    long countByUserIdAndGroupMembership(
            Long userId,
            GroupMembership membership
    );

    List<GroupUser> findAllByUserId(Long userId);

    List<GroupUser> findAllByGroupId(Long groupId);
}