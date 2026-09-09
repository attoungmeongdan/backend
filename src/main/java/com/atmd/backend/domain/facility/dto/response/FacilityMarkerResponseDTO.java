package com.atmd.backend.domain.facility.dto.response;

import com.atmd.backend.domain.facility.entity.Facility;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class FacilityMarkerResponseDTO {

    private Long id;
    private String name;
    private String category;
    private String roadNameAddress;
    private Double lat;
    private Double lng;
    private Double distanceKm;

    public static FacilityMarkerResponseDTO of(Facility facility, double distanceKm) {
        return FacilityMarkerResponseDTO.builder()
                .id(facility.getId())
                .name(facility.getName())
                .category(facility.getCategory())
                .roadNameAddress(facility.getRoadNameAddress())
                .lat(facility.getLat())
                .lng(facility.getLng())
                .distanceKm(distanceKm)
                .build();
    }
}
