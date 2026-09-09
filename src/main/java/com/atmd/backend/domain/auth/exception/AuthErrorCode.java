package com.atmd.backend.domain.auth.exception;

import com.atmd.backend.global.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

    // 로컬 인증 차단
    LOCAL_SIGNUP_DISABLED(HttpStatus.FORBIDDEN, "AUTH_403_LOCAL_SIGNUP_DISABLED", "이메일 회원가입은 지원하지 않습니다. 소셜 로그인을 이용해 주세요."),
    LOCAL_LOGIN_DISABLED(HttpStatus.FORBIDDEN, "AUTH_403_LOCAL_LOGIN_DISABLED", "이메일 로그인은 지원하지 않습니다. 소셜 로그인을 이용해 주세요."),

    // 이메일 / 계정 중복
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "AUTH_409_DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다."),
    DUPLICATE_PROVIDER(HttpStatus.CONFLICT, "AUTH_409_DUPLICATE_PROVIDER", "이미 연동된 소셜 계정입니다."),

    // 인증 정보 오류
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_401_INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다."),

    // 토큰
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_INVALID_TOKEN", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_EXPIRED_TOKEN", "만료된 토큰입니다."),
    TOKEN_TYPE_MISMATCH(HttpStatus.UNAUTHORIZED, "AUTH_401_TOKEN_TYPE_MISMATCH", "리프레시 토큰이 아닙니다."),

    // OAuth 공통
    INVALID_PROVIDER(HttpStatus.BAD_REQUEST, "AUTH_400_INVALID_PROVIDER", "지원하지 않는 소셜 로그인 제공자입니다."),
    OAUTH_PROCESS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH_500_OAUTH_FAILED", "소셜 로그인 처리 중 오류가 발생했습니다."),
    OAUTH_INVALID_STATE(HttpStatus.BAD_REQUEST, "AUTH_400_INVALID_STATE", "유효하지 않거나 만료된 state 값입니다."),
    OAUTH_UNVERIFIED_EMAIL(HttpStatus.BAD_REQUEST, "AUTH_400_UNVERIFIED_EMAIL", "이메일 인증이 완료되지 않은 계정입니다."),

    // 소셜 회원가입 토큰
    INVALID_SIGNUP_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_INVALID_SIGNUP_TOKEN", "유효하지 않은 회원가입 토큰입니다."),
    EXPIRED_SIGNUP_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_401_EXPIRED_SIGNUP_TOKEN", "만료된 회원가입 토큰입니다."),
    MISSING_SIGNUP_TOKEN(HttpStatus.BAD_REQUEST, "AUTH_400_MISSING_SIGNUP_TOKEN", "회원가입 토큰이 필요합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
