package com.projbase.api.domain.user.controller;

import com.projbase.api.domain.user.dto.response.UserProfileResponseDTO;
import com.projbase.api.domain.user.service.UserService;
import com.projbase.api.global.auth.util.SecurityUtil;
import com.projbase.api.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponseDTO>> getMyProfile() {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(userService.getProfile(userId)));
    }
}
