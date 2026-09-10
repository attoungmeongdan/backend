package com.atmd.backend.domain.calendar.exception;

import com.atmd.backend.global.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CalendarErrorCode implements BaseErrorCode {

    INVALID_YEAR_MONTH(HttpStatus.BAD_REQUEST, "CALENDAR_400_INVALID_DATE", "유효하지 않은 연도 또는 월 입력입니다."),
    CALENDAR_NOT_FOUND(HttpStatus.NOT_FOUND, "CALENDAR_404_NOT_FOUND", "캘린더 기록을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}