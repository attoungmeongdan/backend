package com.atmd.backend.domain.address.service;

import com.atmd.backend.domain.address.entity.Address;
import com.atmd.backend.domain.address.exception.AddressErrorCode;
import com.atmd.backend.domain.address.repository.AddressRepository;
import com.atmd.backend.global.common.exception.GeneralException;
import com.atmd.backend.global.external.kakao.client.KakaoLocalClient;
import com.atmd.backend.global.external.kakao.dto.response.KakaoLocalAddressResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final KakaoLocalClient kakaoLocalClient;

    @Transactional
    public Address create(String roadNameAddress, String lotNumberAddress, String detailAddress) {
        Double lat = null;
        Double lng = null;

        if (roadNameAddress != null && !roadNameAddress.isBlank()) {
            try {
                KakaoLocalAddressResponseDTO response = kakaoLocalClient.searchAddress(roadNameAddress);
                lat = response.getLat();
                lng = response.getLng();
            } catch (Exception e) {
                log.warn("카카오 주소 좌표 변환 실패: address={}, error={}", roadNameAddress, e.getMessage());
            }
        }

        return addressRepository.save(
                Address.builder()
                        .roadNameAddress(roadNameAddress)
                        .lotNumberAddress(lotNumberAddress)
                        .detailAddress(detailAddress)
                        .lat(lat)
                        .lng(lng)
                        .build()
        );
    }

    @Transactional(readOnly = true)
    public Address findById(Long id) {
        return addressRepository.findById(id)
                .orElseThrow(() -> new GeneralException(AddressErrorCode.ADDRESS_NOT_FOUND));
    }
}
