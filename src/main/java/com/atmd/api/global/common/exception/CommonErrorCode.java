package com.atmd.api.global.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements BaseErrorCode {

    EXTERNAL_API_ERROR(HttpStatus.BAD_GATEWAY, "EXTERNAL_502_API_ERROR", "외부 API 호출 중 오류가 발생했습니다."),
    EXTERNAL_API_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "EXTERNAL_503_UNAVAILABLE", "외부 서비스를 일시적으로 사용할 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
