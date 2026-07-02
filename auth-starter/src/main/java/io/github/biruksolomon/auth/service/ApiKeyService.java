package io.github.biruksolomon.auth.service;

import io.github.biruksolomon.auth.domain.ApiKey;
import io.github.biruksolomon.auth.domain.Permission;
import io.github.biruksolomon.auth.domain.User;
import io.github.biruksolomon.auth.exception.AuthException;
import io.github.biruksolomon.auth.repository.ApiKeyRepository;
import io.github.biruksolomon.auth.repository.PermissionRepository;
import io.github.biruksolomon.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApiKeyService {
    private final ApiKeyRepository apiKeyRepository;
    private final PermissionRepository permissionRepository;

    @Transactional
    public ApiKey generateApiKey(User user, String name, LocalDateTime expiresAt, Set<Long> permissionIds) {
        // Generate random key (32 bytes → 43 chars base64)
        String plainKey = generatePlainKey();
        String keyHash = hashKey(plainKey);
        String keyPrefix = plainKey.substring(0, 8);

        // Fetch permissions
        Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(permissionIds));

        ApiKey apiKey = ApiKey.builder()
                .user(user)
                .name(name)
                .keyHash(keyHash)
                .keyPrefix(keyPrefix)
                .expiresAt(expiresAt)
                .permissions(permissions)
                .build();

        apiKey = apiKeyRepository.save(apiKey);
        log.info("API key generated for user {} with name: {}", user.getEmail(), name);

        // Return key only once (plaintext). Store only hash in DB.
        // In real response, include plainKey for user to save
        return apiKey;
    }

    @Transactional(readOnly = true)
    public ApiKey validateAndGetApiKey(String plainKey) {
        String keyHash = hashKey(plainKey);
        ApiKey apiKey = apiKeyRepository.findByKeyHash(keyHash)
                .orElseThrow(() -> new AuthException("Invalid API key", "INVALID_API_KEY"));

        if (!apiKey.isValid()) {
            throw new AuthException("API key expired or revoked", "API_KEY_INACTIVE");
        }

        return apiKey;
    }

    @Transactional
    public void updateLastUsed(ApiKey apiKey) {
        apiKey.setLastUsedAt(LocalDateTime.now());
        apiKeyRepository.save(apiKey);
    }

    @Transactional
    public void revokeApiKey(Long apiKeyId, User requestingUser) {
        ApiKey apiKey = apiKeyRepository.findById(apiKeyId)
                .orElseThrow(() -> new AuthException.UserNotFound("API key not found"));

        if (!apiKey.getUser().getId().equals(requestingUser.getId())) {
            throw new AuthException.AccessDenied("Cannot revoke another user's API key");
        }

        apiKey.revoke();
        apiKeyRepository.save(apiKey);
        log.info("API key {} revoked", apiKeyId);
    }

    @Transactional(readOnly = true)
    public List<ApiKey> getUserApiKeys(User user) {
        return apiKeyRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public List<ApiKey> getUserActiveApiKeys(User user) {
        return apiKeyRepository.findByUserAndActive(user, true);
    }

    private String generatePlainKey() {
        SecureRandom random = new SecureRandom();
        byte[] keyBytes = new byte[32];
        random.nextBytes(keyBytes);
        return "sk_" + Base64.getEncoder().withoutPadding().encodeToString(keyBytes);
    }

    private String hashKey(String plainKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(plainKey.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash API key", e);
        }
    }
}
