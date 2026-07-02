package io.github.biruksolomon.auth.service;

import io.github.biruksolomon.auth.domain.OAuth2User;
import io.github.biruksolomon.auth.domain.User;
import io.github.biruksolomon.auth.dto.AuthResponse;
import io.github.biruksolomon.auth.exception.AuthException;
import io.github.biruksolomon.auth.repository.OAuth2UserRepository;
import io.github.biruksolomon.auth.repository.UserRepository;
import io.github.biruksolomon.auth.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class OAuth2Service {
    private final OAuth2UserRepository oauth2UserRepository;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    @Transactional
    public AuthResponse handleGoogleLogin(String googleId, String email, String displayName, String pictureUrl) {
        // Check if OAuth2 account exists
        OAuth2User existingOAuth2 = oauth2UserRepository.findByProviderAndProviderId("GOOGLE", googleId)
                .orElse(null);

        if (existingOAuth2 != null) {
            // Update last used
            existingOAuth2.setLastUsedAt(LocalDateTime.now());
            oauth2UserRepository.save(existingOAuth2);

            User user = existingOAuth2.getUser();
            if (user.getStatus().canLogin()) {
                log.info("User logged in via Google: {}", email);
                return generateAuthResponse(user);
            } else {
                throw new AuthException.AccountLocked(user.getEmail());
            }
        }

        // Check if user with email exists
        User existingUser = userRepository.findByEmail(email).orElse(null);

        if (existingUser != null) {
            // Link OAuth2 account to existing user
            OAuth2User oauth2User = OAuth2User.builder()
                    .user(existingUser)
                    .provider("GOOGLE")
                    .providerId(googleId)
                    .email(email)
                    .displayName(displayName)
                    .profilePictureUrl(pictureUrl)
                    .build();

            oauth2UserRepository.save(oauth2User);
            log.info("OAuth2 account linked to existing user: {}", email);
            return generateAuthResponse(existingUser);
        }

        // Create new user
        User newUser = User.builder()
                .email(email)
                .firstName(displayName)
                .password(UUID.randomUUID().toString()) // Random password (OAuth2 users don't use password)
                .emailVerified(true) // Google emails are verified
                .build();

        newUser = userRepository.save(newUser);

        // Link OAuth2 account
        OAuth2User oauth2User = OAuth2User.builder()
                .user(newUser)
                .provider("GOOGLE")
                .providerId(googleId)
                .email(email)
                .displayName(displayName)
                .profilePictureUrl(pictureUrl)
                .build();

        oauth2UserRepository.save(oauth2User);
        log.info("New user created and logged in via Google: {}", email);

        return generateAuthResponse(newUser);
    }

    @Transactional
    public void linkGoogleAccount(User user, String googleId, String email, String displayName, String pictureUrl) {
        // Check if already linked
        if (oauth2UserRepository.existsByProviderAndProviderId("GOOGLE", googleId)) {
            throw new AuthException("Google account already linked to another user", "OAUTH2_ALREADY_LINKED");
        }

        OAuth2User oauth2User = OAuth2User.builder()
                .user(user)
                .provider("GOOGLE")
                .providerId(googleId)
                .email(email)
                .displayName(displayName)
                .profilePictureUrl(pictureUrl)
                .build();

        oauth2UserRepository.save(oauth2User);
        log.info("Google account linked to user: {}", user.getEmail());
    }

    @Transactional
    public void unlinkGoogleAccount(User user) {
        oauth2UserRepository.findByProviderAndProviderId("GOOGLE", user.getId().toString())
                .ifPresentOrElse(
                        oauth2UserRepository::delete,
                        () -> {
                            throw new AuthException("Google account not linked", "OAUTH2_NOT_LINKED");
                        }
                );
        log.info("Google account unlinked from user: {}", user.getEmail());
    }

    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshToken = jwtProvider.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtProvider.getExpiresIn(accessToken))
                .tokenType("Bearer")
                .build();
    }
}
