package com.atmd.backend.domain.facility.entity;

import com.atmd.backend.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "facilities")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Facility extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(name = "road_name_address")
    private String roadNameAddress;

    @Column(nullable = false)
    private Double lat;

    @Column(nullable = false)
    private Double lng;

    @Builder
    private Facility(String name, String category, String roadNameAddress, Double lat, Double lng) {
        this.name = name;
        this.category = category;
        this.roadNameAddress = roadNameAddress;
        this.lat = lat;
        this.lng = lng;
    }
}
