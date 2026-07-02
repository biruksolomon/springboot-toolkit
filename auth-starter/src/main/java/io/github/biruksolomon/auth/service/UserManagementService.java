package io.github.biruksolomon.auth.service;

import io.github.biruksolomon.auth.domain.User;
import io.github.biruksolomon.auth.domain.UserStatus;
import io.github.biruksolomon.auth.dto.UserUpdateRequest;
import io.github.biruksolomon.auth.exception.AuthException;
import io.github.biruksolomon.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing user accounts, including CRUD operations and status management.
 *
 * Handles:
 * - User retrieval (by ID, email, etc.)
 * - Account updates (profile info, password)
 * - Account status changes (disable, enable, delete, restore)
 * - Account locking/unlocking
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserManagementService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidationService passwordValidationService;

    /**
     * Get user by ID
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AuthException.UserNotFound("User not found with id: " + userId));
    }

    /**
     * Get user by email (including soft-deleted)
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException.UserNotFound(email));
    }

    /**
     * Get active user by email (excluding deleted/disabled)
     */
    public User getActiveUserByEmail(String email) {
        return userRepository.findActiveUserByEmail(email, UserStatus.ACTIVE)
                .orElseThrow(() -> new AuthException.UserNotFound(email));
    }

    /**
     * Get all non-deleted users
     */
    public List<User> getAllNonDeletedUsers() {
        return userRepository.findAllNonDeletedUsers();
    }

    /**
     * Get users by status
     */
    public List<User> getUsersByStatus(UserStatus status) {
        return userRepository.findByStatus(status);
    }

    /**
     * Count users by status
     */
    public long countUsersByStatus(UserStatus status) {
        return userRepository.countByStatus(status);
    }

    /**
     * Update user profile information
     */
    public User updateUserProfile(Long userId, UserUpdateRequest request) {
        User user = getUserById(userId);

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        user = userRepository.save(user);
        log.info("User profile updated: {} (id: {})", user.getEmail(), userId);

        return user;
    }

    /**
     * Change user password
     */
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = getUserById(userId);

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new AuthException.InvalidPassword("Current password is incorrect");
        }

        // Validate new password
        passwordValidationService.validate(newPassword);

        // Ensure new password is different
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new AuthException.InvalidPassword("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password changed for user: {} (id: {})", user.getEmail(), userId);
    }

    /**
     * Disable user account (by admin)
     */
    public User disableUser(Long userId) {
        User user = getUserById(userId);

        if (user.getStatus() == UserStatus.DELETED) {
            throw new AuthException.InvalidPassword("Cannot disable a deleted user account");
        }

        user.disableAccount();
        user = userRepository.save(user);

        log.warn("User account disabled by admin: {} (id: {})", user.getEmail(), userId);

        return user;
    }

    /**
     * Enable user account (by admin)
     */
    public User enableUser(Long userId) {
        User user = getUserById(userId);

        if (user.getStatus() == UserStatus.DELETED) {
            throw new AuthException.InvalidPassword("Cannot enable a deleted user account. Restore first.");
        }

        user.setStatus(UserStatus.ACTIVE);
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);
        user = userRepository.save(user);

        log.info("User account enabled by admin: {} (id: {})", user.getEmail(), userId);

        return user;
    }

    /**
     * Lock user account (temporary)
     */
    public User lockUser(Long userId) {
        User user = getUserById(userId);

        if (user.getStatus() == UserStatus.DELETED) {
            throw new AuthException.InvalidPassword("Cannot lock a deleted user account");
        }

        user.lockAccount();
        user = userRepository.save(user);

        log.warn("User account locked: {} (id: {})", user.getEmail(), userId);

        return user;
    }

    /**
     * Unlock user account
     */
    public User unlockUser(Long userId) {
        User user = getUserById(userId);

        if (user.getStatus() == UserStatus.DELETED) {
            throw new AuthException.InvalidPassword("Cannot unlock a deleted user account");
        }

        user.unlockAccount();
        user = userRepository.save(user);

        log.info("User account unlocked: {} (id: {})", user.getEmail(), userId);

        return user;
    }

    /**
     * Soft delete user account (by admin)
     * User data is retained but account is marked as deleted
     */
    public void deleteUser(Long userId) {
        User user = getUserById(userId);

        if (user.getStatus() == UserStatus.DELETED) {
            throw new AuthException.InvalidPassword("User account is already deleted");
        }

        user.softDelete();
        user = userRepository.save(user);

        log.warn("User account soft-deleted: {} (id: {})", user.getEmail(), userId);

    }

    /**
     * Restore a soft-deleted user account
     */
    public User restoreUser(Long userId) {
        User user = getUserById(userId);

        if (user.getStatus() != UserStatus.DELETED) {
            throw new AuthException.InvalidPassword("User account is not deleted");
        }

        user.restore();
        user = userRepository.save(user);

        log.info("User account restored: {} (id: {})", user.getEmail(), userId);

        return user;
    }

    /**
     * Verify user email (mark as verified)
     */
    public User verifyEmail(Long userId) {
        User user = getUserById(userId);

        user.setEmailVerified(true);
        user = userRepository.save(user);

        log.info("User email verified: {} (id: {})", user.getEmail(), userId);

        return user;
    }

    /**
     * Reset failed login attempts
     */
    public User resetFailedAttempts(Long userId) {
        User user = getUserById(userId);

        user.setFailedLoginAttempts(0);
        user = userRepository.save(user);

        log.info("Failed login attempts reset for user: {} (id: {})", user.getEmail(), userId);

        return user;
    }
}
