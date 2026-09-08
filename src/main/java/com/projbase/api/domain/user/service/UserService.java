package com.projbase.api.domain.user.service;

import com.projbase.api.domain.user.dto.response.UserProfileResponseDTO;
import com.projbase.api.domain.user.entity.User;
import com.projbase.api.domain.user.exception.UserErrorCode;
import com.projbase.api.domain.user.repository.UserRepository;
import com.projbase.api.global.common.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserProfileResponseDTO getProfile(Long userId) {
        User user = findById(userId);
        return UserProfileResponseDTO.from(user);
    }

    @Transactional
    public UserProfileResponseDTO updateNickname(Long userId, String nickname) {
        User user = findById(userId);
        user.updateNickname(nickname);
        return UserProfileResponseDTO.from(user);
    }

    public User findById(Long userId) {
        return userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new GeneralException(UserErrorCode.USER_NOT_FOUND));
    }
}
