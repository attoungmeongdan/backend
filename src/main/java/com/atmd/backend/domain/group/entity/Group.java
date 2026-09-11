package com.atmd.backend.domain.group.entity;

import com.atmd.backend.domain.group.entity.enums.GroupMembership;
import com.atmd.backend.domain.user.entity.User;
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

    public static final int MAX_GROUPS_PER_USER = 5;
    public static final int GENERAL_MAX_MEMBER_COUNT = 2;
    public static final int SUBSCRIBED_MIN_MEMBER_COUNT = 2;
    public static final int SUBSCRIBED_MAX_MEMBER_COUNT = 5;
    public static final int PRICE_PER_MEMBER = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 10)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GroupMembership membership;

    @Column(name = "invite_code", nullable = false, unique = true, length = 20)
    private String inviteCode;

    @Column(nullable = false)
    private String penalty;

    @Column(name = "max_member_count", nullable = false)
    private int maxMemberCount;

    @Column(nullable = false)
    private int price;

    @Builder
    private Group(
            User owner,
            String name,
            GroupMembership membership,
            String inviteCode,
            String penalty,
            int maxMemberCount,
            int price
    ) {
        this.owner = owner;
        this.name = name;
        this.membership = membership;
        this.inviteCode = inviteCode;
        this.penalty = penalty;
        this.maxMemberCount = maxMemberCount;
        this.price = price;
    }

    public static Group createFirst(
            User owner,
            String name,
            String inviteCode,
            String penalty
    ) {
        return Group.builder()
                .owner(owner)
                .name(name)
                .membership(GroupMembership.GENERAL)
                .inviteCode(inviteCode)
                .penalty(penalty)
                .maxMemberCount(GENERAL_MAX_MEMBER_COUNT)
                .price(0)
                .build();
    }

    public static Group createSubscribed(
            User owner,
            String name,
            String inviteCode,
            String penalty,
            int maxMemberCount
    ) {
        return Group.builder()
                .owner(owner)
                .name(name)
                .membership(GroupMembership.SUBSCRIBED)
                .inviteCode(inviteCode)
                .penalty(penalty)
                .maxMemberCount(maxMemberCount)
                .price(maxMemberCount * PRICE_PER_MEMBER)
                .build();
    }

    public static boolean isValidSubscribedCapacity(int memberCount) {
        return memberCount >= SUBSCRIBED_MIN_MEMBER_COUNT
                && memberCount <= SUBSCRIBED_MAX_MEMBER_COUNT;
    }

    public boolean isOwner(Long userId) {
        return this.owner.getId().equals(userId);
    }

    public boolean canAddMember(long currentMemberCount) {
        return currentMemberCount < maxMemberCount;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updatePenalty(String penalty) {
        this.penalty = penalty;
    }
}
