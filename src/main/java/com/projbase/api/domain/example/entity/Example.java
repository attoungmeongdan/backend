package com.projbase.api.domain.example.entity;

import com.projbase.api.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "example")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Example extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Builder
    private Example(String name) {
        this.name = name;
    }
}
