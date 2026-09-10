package com.atmd.backend.domain.user.service;

import com.atmd.backend.domain.fitness.repository.ExerciseSessionRepository;
import com.atmd.backend.domain.user.dto.request.UserProfileUpdateRequest;
import com.atmd.backend.domain.user.dto.response.UserProfileResponseDTO;
import com.atmd.backend.domain.user.entity.User;
import com.atmd.backend.domain.user.exception.UserErrorCode;
import com.atmd.backend.domain.user.repository.UserRepository;
import com.atmd.backend.global.auth.cookie.CookieProvider;
import com.atmd.backend.global.auth.jwt.JwtService;
import com.atmd.backend.global.common.exception.GeneralException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ExerciseSessionRepository exerciseSessionRepository;
    private final JwtService jwtService;
    private final CookieProvider cookieProvider;

    @Transactional(readOnly = true)
    public UserProfileResponseDTO getProfile(Long userId) {
        User user = findById(userId);
        return UserProfileResponseDTO.from(user);
    }

    @Transactional
    public UserProfileResponseDTO updateProfile(Long userId, UserProfileUpdateRequest request) {
        User user = findById(userId);
        if (request.getNickname() != null) {
            user.updateNickname(request.getNickname());
        }
        user.updateProfile(request.getAge(), request.getGender(), request.getHeight(), request.getWeight());
        return UserProfileResponseDTO.from(user);
    }

    @Transactional
    public void withdraw(Long userId, HttpServletResponse response) {
        User user = findById(userId);
        exerciseSessionRepository.deleteAllByUserId(userId);
        userRepository.delete(user);
        jwtService.deleteRefreshToken(userId);
        response.addHeader("Set-Cookie", cookieProvider.expireRefreshTokenCookie().toString());
    }

    public User findById(Long userId) {
        return userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new GeneralException(UserErrorCode.USER_NOT_FOUND));
    }
}
