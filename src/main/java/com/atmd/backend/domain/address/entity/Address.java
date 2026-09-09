package com.atmd.backend.domain.address.entity;

import com.atmd.backend.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "addresses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "road_name_address")
    private String roadNameAddress;

    @Column(name = "lot_number_address")
    private String lotNumberAddress;

    @Column(name = "detail_address")
    private String detailAddress;

    @Column
    private Double lat;

    @Column
    private Double lng;

    @Builder
    private Address(String roadNameAddress, String lotNumberAddress, String detailAddress, Double lat, Double lng) {
        this.roadNameAddress = roadNameAddress;
        this.lotNumberAddress = lotNumberAddress;
        this.detailAddress = detailAddress;
        this.lat = lat;
        this.lng = lng;
    }

    public void updateCoordinates(Double lat, Double lng) {
        this.lat = lat;
        this.lng = lng;
    }
}
