package com.atmd.backend.domain.address.exception;

import com.atmd.backend.global.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AddressErrorCode implements BaseErrorCode {

    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "ADDRESS_404", "주소를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
