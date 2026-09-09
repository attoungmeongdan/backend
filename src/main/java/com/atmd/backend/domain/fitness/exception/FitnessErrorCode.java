package com.atmd.backend.domain.fitness.exception;

import com.atmd.backend.global.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FitnessErrorCode implements BaseErrorCode {
    UNSUPPORTED_EXERCISE(HttpStatus.BAD_REQUEST, "FITNESS_400_1", "아직 지원하지 않는 운동입니다."),
    ACTIVE_SESSION_EXISTS(HttpStatus.CONFLICT, "FITNESS_409_1", "이미 진행 중인 측정 세션이 있습니다."),
    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "FITNESS_404_1", "측정 세션을 찾을 수 없습니다."),
    SESSION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "FITNESS_403_1", "해당 측정 세션에 접근할 수 없습니다."),
    SESSION_NOT_ACTIVE(HttpStatus.CONFLICT, "FITNESS_409_2", "측정 가능한 상태의 세션이 아닙니다."),
    INVALID_SOCKET_TICKET(HttpStatus.UNAUTHORIZED, "FITNESS_401_1", "유효하지 않은 WebSocket 티켓입니다."),
    INVALID_POSE_FRAME(HttpStatus.BAD_REQUEST, "FITNESS_400_2", "유효하지 않은 관절 프레임입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
