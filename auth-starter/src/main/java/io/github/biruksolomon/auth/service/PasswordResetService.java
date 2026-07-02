package io.github.biruksolomon.auth.service;

import io.github.biruksolomon.auth.domain.EmailToken;
import io.github.biruksolomon.auth.domain.User;
import io.github.biruksolomon.auth.exception.AuthException;
import io.github.biruksolomon.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PasswordResetService {
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PasswordValidationService passwordValidationService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void sendPasswordResetEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(AuthException.InvalidCredentials::new);

        if (user.getStatus().isDeleted()) {
            throw new AuthException("User account deleted", "USER_DELETED");
        }

        EmailToken token = emailService.createPasswordResetToken(user);
        // TODO: Send email with reset link: reset-password?token=<token>
        log.info("Password reset email sent to: {}", email);
    }

    @Transactional
    public void resetPassword(String tokenString, String newPassword) {
        EmailToken token = emailService.verifyToken(tokenString, EmailToken.EmailTokenType.PASSWORD_RESET);
        User user = token.getUser();

        // Validate new password
        passwordValidationService.validate(newPassword);

        // Check if new password is same as old
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new AuthException("New password cannot be the same as current password", "INVALID_PASSWORD");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setUpdatedAt(java.time.LocalDateTime.now());
        userRepository.save(user);

        emailService.markTokenAsUsed(token);
        log.info("Password reset for user: {}", user.getEmail());
    }
}
