package com.atmd.backend.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AddressCreateRequest {
    @NotBlank
    private String roadNameAddress;
    @NotBlank
    private String lotNumberAddress;
    private String detailAddress;
}
