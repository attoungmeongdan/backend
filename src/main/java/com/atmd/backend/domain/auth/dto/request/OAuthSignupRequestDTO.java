package com.atmd.backend.domain.auth.dto.request;

import com.atmd.backend.domain.user.entity.enums.Gender;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OAuthSignupRequestDTO {

    @NotBlank
    @Size(min = 1)
    private String nickname;

    @Min(1)
    @Max(100)
    private Integer age;
    private Gender gender;
    @DecimalMin("0.1")
    @DecimalMax("250.0")
    private Double height;
    @DecimalMin("0.1")
    @DecimalMax("300.0")
    private Double weight;
    @NotNull
    @Valid
    private AddressCreateRequest address;
}
