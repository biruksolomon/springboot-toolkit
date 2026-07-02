package io.github.biruksolomon.auth.security;

import io.github.biruksolomon.auth.domain.ApiKey;
import io.github.biruksolomon.auth.service.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {
    private final ApiKeyService apiKeyService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("X-API-Key");

        if (authHeader != null && !authHeader.isEmpty()) {
            try {
                ApiKey apiKey = apiKeyService.validateAndGetApiKey(authHeader);
                apiKeyService.updateLastUsed(apiKey);

                // Create authentication token
                ApiKeyAuthenticationToken token = new ApiKeyAuthenticationToken(apiKey);
                SecurityContextHolder.getContext().setAuthentication(token);
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API key");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
