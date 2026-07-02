package io.github.biruksolomon.auth.service;

import io.github.biruksolomon.auth.domain.EmailToken;
import io.github.biruksolomon.auth.domain.User;
import io.github.biruksolomon.auth.exception.AuthException;
import io.github.biruksolomon.auth.properties.AuthProperties;
import io.github.biruksolomon.auth.repository.EmailTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {
    private static final int TOKEN_LENGTH = 32;
    private static final SecureRandom random = new SecureRandom();

    private final EmailTokenRepository emailTokenRepository;
    private final AuthProperties authProperties;

    public String generateToken() {
        byte[] randomBytes = new byte[TOKEN_LENGTH];
        random.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    public EmailToken createVerificationToken(User user) {
        return createToken(user, EmailToken.EmailTokenType.VERIFICATION, authProperties.getEmail().getVerificationTokenExpiry());
    }

    public EmailToken createPasswordResetToken(User user) {
        return createToken(user, EmailToken.EmailTokenType.PASSWORD_RESET, authProperties.getEmail().getPasswordResetTokenExpiry());
    }

    private EmailToken createToken(User user, EmailToken.EmailTokenType type, String duration) {
        EmailToken token = EmailToken.builder()
                .token(generateToken())
                .type(type)
                .user(user)
                .expiresAt(parseExpiry(duration))
                .build();
        return emailTokenRepository.save(token);
    }

    public EmailToken verifyToken(String tokenString, EmailToken.EmailTokenType type) {
        EmailToken token = emailTokenRepository.findByToken(tokenString)
                .orElseThrow(() -> new AuthException("Invalid or expired token", "INVALID_TOKEN"));

        if (token.getType() != type) {
            throw new AuthException("Token type mismatch", "INVALID_TOKEN");
        }

        if (token.isExpired()) {
            throw new AuthException("Token expired", "TOKEN_EXPIRED");
        }

        if (token.isUsed()) {
            throw new AuthException("Token already used", "TOKEN_ALREADY_USED");
        }

        return token;
    }

    @Transactional
    public void markTokenAsUsed(EmailToken token) {
        token.markAsUsed();
        emailTokenRepository.save(token);
        log.info("Token marked as used: type={}, userId={}", token.getType(), token.getUser().getId());
    }

    @Transactional
    public void deleteExpiredTokens() {
        int deleted = (int) emailTokenRepository.findExpiredUnusedTokens(LocalDateTime.now())
                .stream()
                .peek(emailTokenRepository::delete)
                .count();
        log.info("Deleted {} expired tokens", deleted);
    }

    public long countValidTokens(Long userId, EmailToken.EmailTokenType type) {
        return emailTokenRepository.countValidTokens(userId, type, LocalDateTime.now());
    }

    private LocalDateTime parseExpiry(String duration) {
        // Parse "15m", "1h", "7d"
        if (duration.endsWith("m")) {
            int minutes = Integer.parseInt(duration.substring(0, duration.length() - 1));
            return LocalDateTime.now().plusMinutes(minutes);
        } else if (duration.endsWith("h")) {
            int hours = Integer.parseInt(duration.substring(0, duration.length() - 1));
            return LocalDateTime.now().plusHours(hours);
        } else if (duration.endsWith("d")) {
            int days = Integer.parseInt(duration.substring(0, duration.length() - 1));
            return LocalDateTime.now().plusDays(days);
        }
        throw new IllegalArgumentException("Invalid duration format: " + duration);
    }
}
