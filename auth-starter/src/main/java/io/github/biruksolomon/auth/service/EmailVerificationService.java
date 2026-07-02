package io.github.biruksolomon.auth.service;

import io.github.biruksolomon.auth.domain.EmailToken;
import io.github.biruksolomon.auth.domain.User;
import io.github.biruksolomon.auth.exception.AuthException;
import io.github.biruksolomon.auth.properties.AuthProperties;
import io.github.biruksolomon.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailVerificationService {
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final AuthProperties authProperties;

    @Transactional
    public void sendVerificationEmail(User user) {
        // If email verification is disabled in config, skip
        if (!authProperties.getEmail().isVerificationRequired()) {
            user.setEmailVerified(true);
            userRepository.save(user);
            log.info("Email verification disabled in config, auto-verified: {}", user.getEmail());
            return;
        }

        if (user.isEmailVerified()) {
            throw new AuthException("Email already verified", "EMAIL_ALREADY_VERIFIED");
        }

        EmailToken token = emailService.createVerificationToken(user);
        // TODO: Send email with token: verification-link?token=<token>
        log.info("Verification email sent to: {}", user.getEmail());
    }

    @Transactional
    public void verifyEmail(String tokenString) {
        // If email verification is disabled in config, allow verification without token
        if (!authProperties.getEmail().isVerificationRequired()) {
            log.info("Email verification disabled, skipping token validation");
            return;
        }

        EmailToken token = emailService.verifyToken(tokenString, EmailToken.EmailTokenType.VERIFICATION);
        User user = token.getUser();

        user.setEmailVerified(true);
        user.setUpdatedAt(java.time.LocalDateTime.now());
        userRepository.save(user);

        emailService.markTokenAsUsed(token);
        log.info("Email verified for user: {}", user.getEmail());
    }

    @Transactional
    public void resendVerificationEmail(Long userId) {
        // If email verification is disabled, skip
        if (!authProperties.getEmail().isVerificationRequired()) {
            log.info("Email verification disabled in config, skipping resend");
            return;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException.UserNotFound(userId));

        if (user.isEmailVerified()) {
            throw new AuthException("Email already verified", "EMAIL_ALREADY_VERIFIED");
        }

        sendVerificationEmail(user);
    }
}
