package com.atmd.backend.domain.auth.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AddressCreateRequest {
    private String roadNameAddress;
    private String lotNumberAddress;
    private String detailAddress;
}
