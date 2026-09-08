package com.projbase.api.global.auth.util;

import com.projbase.api.domain.auth.exception.AuthErrorCode;
import com.projbase.api.global.common.exception.GeneralException;
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
