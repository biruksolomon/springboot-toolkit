package io.github.biruksolomon.auth.security;

import io.github.biruksolomon.auth.annotation.RateLimit;
import io.github.biruksolomon.auth.exception.AuthException;
import io.github.biruksolomon.auth.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class RateLimitingAspect {
    private final RateLimitService rateLimitService;

    @Around("@annotation(rateLimit)")
    public Object checkRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        HttpServletRequest request = getRequest();
        String clientIp = getClientIp(request);
        String key = rateLimit.keyPrefix().isEmpty() ? clientIp : rateLimit.keyPrefix() + ":" + clientIp;

        if (!rateLimitService.isAllowed(key, rateLimit.limit(), rateLimit.windowSeconds())) {
            long resetTime = rateLimitService.getResetTime(key, rateLimit.windowSeconds());
            throw new AuthException("Rate limit exceeded. Reset at: " + resetTime, "RATE_LIMIT_EXCEEDED");
        }

        return joinPoint.proceed();
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new RuntimeException("Request context not available");
        }
        return attributes.getRequest();
    }

    private String getClientIp(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        } else {
            clientIp = clientIp.split(",")[0];
        }
        return clientIp;
    }
}
