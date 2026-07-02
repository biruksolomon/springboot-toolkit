package io.github.biruksolomon.auth.controller;

import io.github.biruksolomon.auth.domain.User;
import io.github.biruksolomon.auth.dto.ChangePasswordRequest;
import io.github.biruksolomon.auth.dto.UserResponse;
import io.github.biruksolomon.auth.dto.UserUpdateRequest;
import io.github.biruksolomon.auth.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserManagementService userManagementService;

    /**
     * Get current authenticated user's profile
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        User user = userManagementService.getUserByEmail(email);
        return ResponseEntity.ok(mapToResponse(user));
    }

    /**
     * Get user by ID (admin only)
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        User user = userManagementService.getUserById(userId);
        return ResponseEntity.ok(mapToResponse(user));
    }

    /**
     * Update current user's profile
     */
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(
            @RequestBody UserUpdateRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        User user = userManagementService.getUserByEmail(email);
        User updated = userManagementService.updateUserProfile(user.getId(), request);
        return ResponseEntity.ok(mapToResponse(updated));
    }

    /**
     * Change current user's password
     */
    @PostMapping("/me/change-password")
    public ResponseEntity<Void> changePassword(
            @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        User user = userManagementService.getUserByEmail(email);
        userManagementService.changePassword(user.getId(), request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all non-deleted users (admin only)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllNonDeletedUsers() {
        List<User> users = userManagementService.getAllNonDeletedUsers();
        return ResponseEntity.ok(users.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList()));
    }

    /**
     * Disable user account (admin only)
     */
    @PostMapping("/{userId}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> disableUser(@PathVariable Long userId) {
        User user = userManagementService.disableUser(userId);
        return ResponseEntity.ok(mapToResponse(user));
    }

    /**
     * Enable user account (admin only)
     */
    @PostMapping("/{userId}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> enableUser(@PathVariable Long userId) {
        User user = userManagementService.enableUser(userId);
        return ResponseEntity.ok(mapToResponse(user));
    }

    /**
     * Lock user account temporarily (admin only)
     */
    @PostMapping("/{userId}/lock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> lockUser(@PathVariable Long userId) {
        User user = userManagementService.lockUser(userId);
        return ResponseEntity.ok(mapToResponse(user));
    }

    /**
     * Unlock user account (admin only)
     */
    @PostMapping("/{userId}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> unlockUser(@PathVariable Long userId) {
        User user = userManagementService.unlockUser(userId);
        return ResponseEntity.ok(mapToResponse(user));
    }

    /**
     * Soft delete user account (admin only)
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userManagementService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Restore soft-deleted user account (admin only)
     */
    @PostMapping("/{userId}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> restoreUser(@PathVariable Long userId) {
        User user = userManagementService.restoreUser(userId);
        return ResponseEntity.ok(mapToResponse(user));
    }

    /**
     * Reset failed login attempts (admin only)
     */
    @PostMapping("/{userId}/reset-failed-attempts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> resetFailedAttempts(@PathVariable Long userId) {
        User user = userManagementService.resetFailedAttempts(userId);
        return ResponseEntity.ok(mapToResponse(user));
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .emailVerified(user.isEmailVerified())
                .status(user.getStatus())
                .accountLocked(user.isAccountLocked())
                .failedLoginAttempts(user.getFailedLoginAttempts())
                .lockedUntil(user.getLockedUntil())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .deletedAt(user.getDeletedAt())
                .build();
    }
}
