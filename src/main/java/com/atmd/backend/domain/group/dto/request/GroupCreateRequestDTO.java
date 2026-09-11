package com.atmd.backend.domain.group.dto.request;

import jakarta.validation.constraints.NotBlank;
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
     * 첫 그룹이 아닌 경우(SUBSCRIBED)에만 사용. 2~5 사이 값.
     * 첫 그룹은 서버에서 2로 고정하므로 null 허용.
     */
    private Integer maxMemberCount;
}
