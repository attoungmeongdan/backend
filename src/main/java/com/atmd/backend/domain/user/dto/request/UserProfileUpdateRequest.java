package com.atmd.backend.domain.user.dto.request;

import com.atmd.backend.domain.user.entity.enums.Gender;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserProfileUpdateRequest {

    @Size(min = 1)
    private String nickname;

    @Min(1)
    private Integer age;
    private Gender gender;
    @DecimalMin("0.1")
    @DecimalMax("250.0")
    private Double height;
    @DecimalMin("0.1")
    @DecimalMax("300.0")
    private Double weight;
}
