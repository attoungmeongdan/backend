package com.atmd.backend.domain.user.controller;

import com.atmd.backend.domain.user.dto.request.UserProfileUpdateRequest;
import com.atmd.backend.domain.user.dto.response.UserProfileResponseDTO;
import com.atmd.backend.domain.user.service.UserService;
import com.atmd.backend.global.auth.util.SecurityUtil;
import com.atmd.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "유저 API (프로필 조회 · 수정 · 탈퇴)")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "내 프로필 조회",
            description = "로그인한 유저의 프로필 정보를 반환합니다. 닉네임, 신체정보, 주소(위경도 포함)가 포함됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "프로필 조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 요청")
    })
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponseDTO>> getMyProfile() {
        return ResponseEntity.ok(ApiResponse.success(userService.getProfile(SecurityUtil.getCurrentUserId())));
    }

    @Operation(
            summary = "내 프로필 수정",
            description = "닉네임, 나이, 성별, 키, 체중을 수정합니다. 수정하지 않을 필드는 null로 보내면 됩니다. 단, 나이·키·체중은 null 전송 시 null로 저장됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "프로필 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음")
    })
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponseDTO>> updateProfile(
            @Valid @RequestBody UserProfileUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateProfile(SecurityUtil.getCurrentUserId(), request)));
    }

    @Operation(
            summary = "회원탈퇴",
            description = "계정과 운동 기록을 모두 삭제하고 리프레시 토큰을 무효화합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원탈퇴 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음")
    })
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> withdraw(HttpServletResponse response) {
        userService.withdraw(SecurityUtil.getCurrentUserId(), response);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
