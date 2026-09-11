package com.atmd.backend.domain.group.repository;

import com.atmd.backend.domain.group.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {

    Optional<Group> findByIdAndIsDeletedFalse(Long id);

    Optional<Group> findByInviteCodeAndIsDeletedFalse(String inviteCode);

    boolean existsByInviteCode(String inviteCode);
}
