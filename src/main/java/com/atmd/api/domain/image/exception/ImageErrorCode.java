package com.atmd.api.domain.image.exception;

import com.atmd.api.global.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ImageErrorCode implements BaseErrorCode {

    INVALID_DIRECTORY(HttpStatus.BAD_REQUEST, "IMAGE_400_INVALID_DIRECTORY", "허용되지 않는 업로드 경로입니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "IMAGE_400_INVALID_TYPE", "허용되지 않는 파일 형식입니다. (jpeg, png, gif, webp만 가능)"),
    UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "IMAGE_500_UPLOAD_FAILED", "파일 업로드 중 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
