package com.atmd.backend.domain.group.exception;

import com.atmd.backend.global.common.exception.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GroupErrorCode implements BaseErrorCode {

    GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "GROUP_404_NOT_FOUND", "존재하지 않는 그룹입니다."),
    INVALID_INVITE_CODE(HttpStatus.BAD_REQUEST, "GROUP_400_INVALID_INVITE_CODE", "유효하지 않은 초대 코드입니다."),
    INVALID_MEMBER_COUNT(HttpStatus.BAD_REQUEST, "GROUP_400_INVALID_MEMBER_COUNT", "그룹 가용 인원은 2명 이상 5명 이하만 가능합니다."),
    ALREADY_MEMBER(HttpStatus.CONFLICT, "GROUP_409_ALREADY_MEMBER", "이미 참가된 그룹입니다."),
    NOT_MEMBER(HttpStatus.BAD_REQUEST, "GROUP_400_NOT_MEMBER", "그룹 멤버가 아닙니다."),
    GROUP_FULL(HttpStatus.CONFLICT, "GROUP_409_GROUP_FULL", "그룹 정원이 가득 찼습니다."),
    MAX_GROUPS_EXCEEDED(HttpStatus.CONFLICT, "GROUP_409_MAX_GROUPS_EXCEEDED", "참가 가능한 그룹 수를 초과했습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "GROUP_403_ACCESS_DENIED", "그룹에 대한 권한이 없습니다."),
    OWNER_MUST_DELETE_GROUP(HttpStatus.BAD_REQUEST, "GROUP_400_OWNER_MUST_DELETE_GROUP", "방장은 그룹을 탈퇴할 수 없습니다. 그룹을 삭제해 주세요."),
    INVITE_CODE_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "GROUP_500_INVITE_CODE_FAILED", "초대 코드 생성에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
