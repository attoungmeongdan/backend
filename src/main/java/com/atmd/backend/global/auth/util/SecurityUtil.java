package com.atmd.backend.global.auth.util;

import com.atmd.backend.domain.auth.exception.AuthErrorCode;
import com.atmd.backend.global.common.exception.GeneralException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    private SecurityUtil() {}

    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new GeneralException(AuthErrorCode.INVALID_TOKEN);
        }
        return (Long) authentication.getPrincipal();
    }
}
