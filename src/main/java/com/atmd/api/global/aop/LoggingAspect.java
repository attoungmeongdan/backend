package com.atmd.api.global.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object logRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        String method = "";
        String uri = "";
        if (attrs != null) {
            method = attrs.getRequest().getMethod();
            uri = attrs.getRequest().getRequestURI();
        }

        String userId = resolveUserId();
        String handler = joinPoint.getSignature().toShortString();

        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;
            log.info("[{}] {} | user={} | {}ms | handler={}", method, uri, userId, elapsed, handler);
            return result;
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            log.warn("[{}] {} | user={} | {}ms | handler={} | error={}", method, uri, userId, elapsed, handler, e.getMessage());
            throw e;
        }
    }

    private String resolveUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Long id) {
                return String.valueOf(id);
            }
        } catch (Exception ignored) {
        }
        return "anonymous";
    }
}
