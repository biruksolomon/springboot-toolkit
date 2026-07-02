package io.github.biruksolomon.auth.controller;

import io.github.biruksolomon.auth.domain.ApiKey;
import io.github.biruksolomon.auth.domain.User;
import io.github.biruksolomon.auth.dto.GenerateApiKeyRequest;
import io.github.biruksolomon.auth.dto.GenerateApiKeyResponse;
import io.github.biruksolomon.auth.dto.ApiKeyResponse;
import io.github.biruksolomon.auth.service.ApiKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {
    private final ApiKeyService apiKeyService;

    @PostMapping
    public ResponseEntity<GenerateApiKeyResponse> generateApiKey(
            @RequestBody GenerateApiKeyRequest request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ApiKey apiKey = apiKeyService.generateApiKey(user, request.getName(), request.getExpiresAt(), request.getPermissionIds());

        // Note: In real implementation, return the plain key here (shown only once)
        return ResponseEntity.ok(GenerateApiKeyResponse.builder()
                .id(apiKey.getId())
                .name(apiKey.getName())
                .keyPrefix(apiKey.getKeyPrefix())
                .expiresAt(apiKey.getExpiresAt() != null ? apiKey.getExpiresAt().toString() : null)
                .createdAt(apiKey.getCreatedAt().toString())
                .build());
    }

    @GetMapping
    public ResponseEntity<List<ApiKeyResponse>> listApiKeys(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<ApiKeyResponse> keys = apiKeyService.getUserApiKeys(user)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(keys);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiKeyResponse> getApiKey(
            @PathVariable Long id,
            Authentication authentication) {
        // Implementation: fetch and verify ownership
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revokeApiKey(
            @PathVariable Long id,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        apiKeyService.revokeApiKey(id, user);
        return ResponseEntity.noContent().build();
    }

    private ApiKeyResponse mapToResponse(ApiKey apiKey) {
        return ApiKeyResponse.builder()
                .id(apiKey.getId())
                .name(apiKey.getName())
                .keyPrefix(apiKey.getKeyPrefix())
                .active(apiKey.isActive())
                .permissions(apiKey.getPermissions().stream().map(p -> p.getName()).collect(Collectors.toList()))
                .expiresAt(apiKey.getExpiresAt() != null ? apiKey.getExpiresAt().toString() : null)
                .lastUsedAt(apiKey.getLastUsedAt() != null ? apiKey.getLastUsedAt().toString() : null)
                .createdAt(apiKey.getCreatedAt().toString())
                .revokedAt(apiKey.getRevokedAt() != null ? apiKey.getRevokedAt().toString() : null)
                .build();
    }
}