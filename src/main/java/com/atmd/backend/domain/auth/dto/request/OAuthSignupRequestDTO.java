package com.atmd.backend.domain.auth.dto.request;

import com.atmd.backend.domain.user.entity.enums.Gender;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OAuthSignupRequestDTO {

    @NotBlank
    @Size(min = 1)
    private String nickname;

    @Min(1)
    private Integer age;
    private Gender gender;
    @Positive
    private Double height;
    @Positive
    private Double weight;
    private AddressCreateRequest address;
}
