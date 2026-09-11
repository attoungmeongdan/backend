package com.atmd.backend.domain.group.entity;

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
        name = "group_users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_group_users_group_user",
                        columnNames = {"group_id", "user_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupUser extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    private GroupUser(Group group, User user) {
        this.group = group;
        this.user = user;
    }

    public static GroupUser of(Group group, User user) {
        return GroupUser.builder()
                .group(group)
                .user(user)
                .build();
    }
}