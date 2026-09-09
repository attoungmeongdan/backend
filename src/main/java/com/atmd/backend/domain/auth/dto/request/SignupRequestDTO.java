package com.atmd.backend.domain.auth.dto.request;

import com.atmd.backend.domain.user.entity.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequestDTO {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8, max = 16, message = "비밀번호는 8자 이상 16자 이하여야 합니다.")
    private String password;

    @NotBlank
    private String nickname;

    @Min(0)
    private Integer age;
    private Gender gender;
    @Positive
    private Double height;
    @Positive
    private Double weight;
    private AddressCreateRequest address;
}
