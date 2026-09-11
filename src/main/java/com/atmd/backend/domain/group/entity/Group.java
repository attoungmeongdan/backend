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
    public static final int MIN_MEMBER_COUNT = 2;
    public static final int MAX_MEMBER_COUNT = 5;
    public static final int FREE_MEMBER_THRESHOLD = 2;
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
            int maxMemberCount,
            int price
    ) {
        this.owner = owner;
        this.name = name;
        this.membership = membership;
        this.inviteCode = inviteCode;
        this.maxMemberCount = maxMemberCount;
        this.price = price;
    }

    public static Group create(
            User owner,
            String name,
            String inviteCode,
            int maxMemberCount
    ) {
        GroupMembership membership = maxMemberCount <= FREE_MEMBER_THRESHOLD
                ? GroupMembership.GENERAL
                : GroupMembership.SUBSCRIBED;
        int price = calculatePrice(maxMemberCount);
        return Group.builder()
                .owner(owner)
                .name(name)
                .membership(membership)
                .inviteCode(inviteCode)
                .maxMemberCount(maxMemberCount)
                .price(price)
                .build();
    }

    public static int calculatePrice(int maxMemberCount) {
        return Math.max(0, maxMemberCount - FREE_MEMBER_THRESHOLD) * PRICE_PER_MEMBER;
    }

    public static boolean isValidMemberCount(int memberCount) {
        return memberCount >= MIN_MEMBER_COUNT && memberCount <= MAX_MEMBER_COUNT;
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
}
