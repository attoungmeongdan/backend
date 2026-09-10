package com.atmd.backend.domain.facility.exception;

import com.atmd.backend.global.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FacilityErrorCode implements BaseErrorCode {

    USER_ADDRESS_NOT_SET(HttpStatus.BAD_REQUEST, "FACILITY_400_USER_ADDRESS_NOT_SET", "주소 정보가 등록되지 않아 주변 시설을 조회할 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
