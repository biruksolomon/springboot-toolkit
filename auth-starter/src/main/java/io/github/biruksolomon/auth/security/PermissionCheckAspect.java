package io.github.biruksolomon.auth.security;

import io.github.biruksolomon.auth.annotation.RequiresPermission;
import io.github.biruksolomon.auth.exception.AuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionCheckAspect {

    @Before("@annotation(requiresPermission)")
    public void checkPermission(JoinPoint joinPoint, RequiresPermission requiresPermission) {
        String requiredPermission = requiresPermission.value();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Access denied: No authentication");
            throw new AuthException.AccessDenied();
        }

        boolean hasPermission = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals(requiredPermission) || auth.equals("ROLE_ADMIN"));

        if (!hasPermission) {
            log.warn("Access denied for user: {} to permission: {}",
                    authentication.getName(), requiredPermission);
            throw new AuthException.AccessDenied();
        }
    }
}