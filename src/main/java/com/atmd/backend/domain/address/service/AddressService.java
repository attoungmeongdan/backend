package com.atmd.backend.domain.address.service;

import com.atmd.backend.domain.address.entity.Address;
import com.atmd.backend.domain.address.exception.AddressErrorCode;
import com.atmd.backend.domain.address.repository.AddressRepository;
import com.atmd.backend.global.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;

    @Transactional
    public Address create(String roadNameAddress, String lotNumberAddress, String detailAddress) {
        return addressRepository.save(
                Address.builder()
                        .roadNameAddress(roadNameAddress)
                        .lotNumberAddress(lotNumberAddress)
                        .detailAddress(detailAddress)
                        .build()
        );
    }

    @Transactional(readOnly = true)
    public Address findById(Long id) {
        return addressRepository.findById(id)
                .orElseThrow(() -> new GeneralException(AddressErrorCode.ADDRESS_NOT_FOUND));
    }
}
