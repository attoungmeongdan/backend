package com.atmd.backend.domain.user.entity;

import com.atmd.backend.domain.address.entity.Address;
import com.atmd.backend.domain.user.entity.enums.Gender;
import com.atmd.backend.domain.user.entity.enums.Provider;
import com.atmd.backend.domain.user.entity.enums.Role;
import com.atmd.backend.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_provider", columnNames = {"provider", "provider_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    // @Column
    // private String password;

    @Column(nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;

    @Column(name = "provider_id")
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @Column
    private Double height;

    @Column
    private Double weight;

    @Column
    private Double bmi;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "address_id")
    private Address address;

    @Column(name = "first_group_created", nullable = false)
    private boolean firstGroupCreated = false;

    @Builder
    private User(String email, String nickname, Provider provider, String providerId, Role role,
                 Integer age, Gender gender, Double height, Double weight, Address address) {
        this.email = email;
        // this.password = password;
        this.nickname = nickname;
        this.provider = provider;
        this.providerId = providerId;
        this.role = role;
        this.age = age;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
        this.address = address;
    }

    public static User ofOAuth(String email, String nickname, Provider provider, String providerId) {
        return User.builder()
                .email(email)
                .nickname(nickname)
                .provider(provider)
                .providerId(providerId)
                .role(Role.USER)
                .build();
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateProfile(Integer age, Gender gender, Double height, Double weight) {
        this.age = age;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
        this.bmi = calculateBmi(height, weight);
    }

    private Double calculateBmi(Double height, Double weight) {
        if (height == null || weight == null || height <= 0) return null;
        double heightM = height / 100.0;
        return Math.round(weight / (heightM * heightM) * 10.0) / 10.0;
    }

    public void updateAddress(Address address) {
        this.address = address;
    }

    public void markFirstGroupCreated() {
        this.firstGroupCreated = true;
    }
}
