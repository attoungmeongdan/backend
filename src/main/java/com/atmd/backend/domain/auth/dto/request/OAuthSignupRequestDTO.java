package com.atmd.backend.domain.auth.dto.request;

import com.atmd.backend.domain.user.entity.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OAuthSignupRequestDTO {

    @NotBlank
    private String nickname;

    private Integer age;
    private Gender gender;
    private Double height;
    private Double weight;
    private AddressCreateRequest address;
}
