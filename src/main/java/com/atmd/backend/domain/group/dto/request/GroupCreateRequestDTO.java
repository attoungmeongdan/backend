package com.atmd.backend.domain.group.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GroupCreateRequestDTO {

    @NotBlank
    @Size(max = 10)
    private String name;

    @NotBlank
    @Size(max = 200)
    private String penalty;

    /**
     * 그룹 가용 최대 인원 (방장 포함, 2~5).
     * 2인은 무료(GENERAL), 3~5인은 유료(SUBSCRIBED, (maxMemberCount - 2) × 500원).
     */
    @NotNull
    @Min(2)
    @Max(5)
    private Integer maxMemberCount;
}
