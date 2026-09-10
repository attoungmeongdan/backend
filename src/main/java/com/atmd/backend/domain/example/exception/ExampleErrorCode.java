package com.atmd.backend.domain.example.exception;

import com.atmd.backend.global.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ExampleErrorCode implements BaseErrorCode {

    EXAMPLE_NOT_FOUND(HttpStatus.NOT_FOUND, "EXAMPLE_404_NOT_FOUND", "예시 데이터를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}