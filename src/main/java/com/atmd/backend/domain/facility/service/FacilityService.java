package com.atmd.backend.domain.facility.service;

import com.atmd.backend.domain.address.entity.Address;
import com.atmd.backend.domain.facility.dto.response.FacilityMarkerResponseDTO;
import com.atmd.backend.domain.facility.exception.FacilityErrorCode;
import com.atmd.backend.domain.facility.repository.FacilityRepository;
import com.atmd.backend.domain.user.entity.User;
import com.atmd.backend.domain.user.exception.UserErrorCode;
import com.atmd.backend.domain.user.repository.UserRepository;
import com.atmd.backend.global.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FacilityService {

    private final FacilityRepository facilityRepository;
    private final UserRepository userRepository;
    private final FacilityCacheService facilityCacheService;

    @Transactional(readOnly = true)
    public List<FacilityMarkerResponseDTO> getMarkers(Long userId) {
        Optional<List<FacilityMarkerResponseDTO>> cached = facilityCacheService.get(userId);
        if (cached.isPresent()) return cached.get();

        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new GeneralException(UserErrorCode.USER_NOT_FOUND));

        Address address = user.getAddress();
        if (address == null || address.getLat() == null || address.getLng() == null) {
            throw new GeneralException(FacilityErrorCode.USER_ADDRESS_NOT_SET);
        }

        double userLat = address.getLat();
        double userLng = address.getLng();

        List<FacilityMarkerResponseDTO> markers = facilityRepository.findWithinRadius(userLat, userLng)
                .stream()
                .map(f -> FacilityMarkerResponseDTO.of(f, calculateDistance(userLat, userLng, f.getLat(), f.getLng())))
                .sorted(java.util.Comparator.comparingDouble(FacilityMarkerResponseDTO::getDistanceKm))
                .toList();

        facilityCacheService.save(userId, markers);

        return markers;
    }

    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round(R * c * 10.0) / 10.0;
    }
}
