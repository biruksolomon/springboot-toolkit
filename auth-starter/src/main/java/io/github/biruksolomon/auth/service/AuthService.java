package io.github.biruksolomon.auth.service;

import io.github.biruksolomon.auth.domain.RefreshToken;
import io.github.biruksolomon.auth.domain.User;
import io.github.biruksolomon.auth.dto.AuthRequest;
import io.github.biruksolomon.auth.dto.AuthResponse;
import io.github.biruksolomon.auth.dto.RegisterRequest;
import io.github.biruksolomon.auth.exception.AuthException;
import io.github.biruksolomon.auth.properties.AuthProperties;
import io.github.biruksolomon.auth.repository.RefreshTokenRepository;
import io.github.biruksolomon.auth.repository.UserRepository;
import io.github.biruksolomon.auth.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuthProperties authProperties;
    private final PasswordValidationService passwordValidationService;

    public AuthResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthException.UserAlreadyExists(request.getEmail());
        }

        // Validate password
        passwordValidationService.validate(request.getPassword());

        // Create new user with ACTIVE status
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                // If email verification is not required, auto-verify
                .emailVerified(!authProperties.getEmail().isVerificationRequired())
                .build();

        user = userRepository.save(user);

        if (!authProperties.getEmail().isVerificationRequired()) {
            log.info("User registered with auto-verified email (verification disabled): {}", user.getEmail());
        } else {
            log.info("New user registered: {} with status: {}", user.getEmail(), user.getStatus());
        }

        return generateAuthResponse(user);
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(AuthException.InvalidCredentials::new);

        // Check if user is deleted
        if (user.getStatus().isDeleted()) {
            throw new AuthException.InvalidCredentials();
        }

        // Check if user is disabled by admin
        if (user.getStatus().isDisabled()) {
            log.warn("Login attempt for disabled user: {}", user.getEmail());
            throw new AuthException.AccountLocked(user.getEmail());
        }

        // Check if account is locked and unlock if lock duration expired
        if (user.getStatus().isLocked()) {
            if (user.getLockedUntil() != null && LocalDateTime.now().isAfter(user.getLockedUntil())) {
                user.unlockAccount();
            } else {
                log.warn("Login attempt for locked user: {}", user.getEmail());
                throw new AuthException.AccountLocked(user.getEmail());
            }
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

            if (user.getFailedLoginAttempts() >= authProperties.getAccount().getMaxFailedAttempts()) {
                user.lockAccount();
                userRepository.save(user);
                log.warn("Account locked due to failed attempts: {}", user.getEmail());
                throw new AuthException.AccountLocked(user.getEmail());
            }

            userRepository.save(user);
            throw new AuthException.InvalidCredentials();
        }

        // Reset failed attempts on successful login
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        log.info("User logged in: {} with status: {}", user.getEmail(), user.getStatus());
        return generateAuthResponse(user);
    }

    public AuthResponse refreshAccessToken(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new AuthException.InvalidToken("Refresh token not found"));

        if (!token.isValid()) {
            throw new AuthException.TokenExpired();
        }

        User user = token.getUser();

        // Revoke old refresh token (rotation)
        token.setRevoked(true);
        refreshTokenRepository.save(token);

        return generateAuthResponse(user);
    }

    public void logout(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new AuthException.InvalidToken("Refresh token not found"));

        token.setRevoked(true);
        token.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(token);
        log.info("User logged out, refresh token revoked");
    }

    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshTokenString = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenString)
                .issuedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plus(authProperties.getJwt().getRefreshTokenExpiry()))
                .build();

        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenString)
                .tokenType("Bearer")
                .expiresIn(authProperties.getJwt().getAccessTokenExpiry().toSeconds())
                .user(AuthResponse.UserDto.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .emailVerified(user.isEmailVerified())
                        .build())
                .build();
    }
}
