package com.atmd.backend.domain.group.repository;

import com.atmd.backend.domain.group.entity.GroupUser;
import com.atmd.backend.domain.group.entity.enums.GroupMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupUserRepository extends JpaRepository<GroupUser, Long> {

    boolean existsByGroupIdAndUserId(Long groupId, Long userId);

    Optional<GroupUser> findByGroupIdAndUserId(Long groupId, Long userId);

    long countByGroupId(Long groupId);

    long countByUserId(Long userId);

    long countByUserIdAndGroupMembership(Long userId, GroupMembership membership);

    @Query("select gu from GroupUser gu join fetch gu.group where gu.user.id = :userId")
    List<GroupUser> findAllByUserIdWithGroup(@Param("userId") Long userId);

    @Query("select gu from GroupUser gu join fetch gu.user where gu.group.id = :groupId")
    List<GroupUser> findAllByGroupIdWithUser(@Param("groupId") Long groupId);

    @Modifying
    @Query("delete from GroupUser gu where gu.group.id = :groupId")
    void deleteAllByGroupId(@Param("groupId") Long groupId);
}
