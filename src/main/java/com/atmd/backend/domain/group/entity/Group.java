package com.atmd.backend.domain.group.entity;

import com.atmd.backend.domain.group.entity.enums.GroupMembership;
import com.atmd.backend.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "groups",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_groups_invite_code",
                        columnNames = "invite_code"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Group extends BaseEntity {

    public static final int GENERAL_MAX_MEMBER_COUNT = 2;
    public static final int SUBSCRIBED_MAX_MEMBER_COUNT = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GroupMembership membership;

    @Column(name = "invite_code", nullable = false, unique = true, length = 20)
    private String inviteCode;

    @Column(nullable = false)
    private String penalty;

    @Builder
    private Group(
            String name,
            GroupMembership membership,
            String inviteCode,
            String penalty
    ) {
        this.name = name;
        this.membership = membership;
        this.inviteCode = inviteCode;
        this.penalty = penalty;
    }

    public static Group of(
            String name,
            GroupMembership membership,
            String inviteCode,
            String penalty
    ) {
        return Group.builder()
                .name(name)
                .membership(membership)
                .inviteCode(inviteCode)
                .penalty(penalty)
                .build();
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateMembership(GroupMembership membership) {
        this.membership = membership;
    }

    public void updatePenalty(String penalty) {
        this.penalty = penalty;
    }

    public int getMaxMemberCount() {
        return membership == GroupMembership.GENERAL
                ? GENERAL_MAX_MEMBER_COUNT
                : SUBSCRIBED_MAX_MEMBER_COUNT;
    }

    public boolean canAddMember(long currentMemberCount) {
        return currentMemberCount < getMaxMemberCount();
    }
}