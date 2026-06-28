package io.github.biruksolomon.auth.exception;

public class AuthException extends RuntimeException {
    private final String code;

    public AuthException(String message, String code) {
        super(message);
        this.code = code;
    }

    public AuthException(String message, String code, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static class InvalidCredentials extends AuthException {
        public InvalidCredentials() {
            super("Invalid email or password", "INVALID_CREDENTIALS");
        }
    }

    public static class UserNotFound extends AuthException {
        public UserNotFound(String email) {
            super("User not found: " + email, "USER_NOT_FOUND");
        }

        public UserNotFound(Long id) {
            super("User not found: " + id, "USER_NOT_FOUND");
        }
    }

    public static class UserAlreadyExists extends AuthException {
        public UserAlreadyExists(String email) {
            super("User already exists: " + email, "USER_ALREADY_EXISTS");
        }
    }

    public static class InvalidToken extends AuthException {
        public InvalidToken(String message) {
            super(message, "INVALID_TOKEN");
        }
    }

    public static class TokenExpired extends AuthException {
        public TokenExpired() {
            super("Token has expired", "TOKEN_EXPIRED");
        }
    }

    public static class AccountLocked extends AuthException {
        public AccountLocked(String email) {
            super("Account is locked: " + email, "ACCOUNT_LOCKED");
        }
    }

    public static class EmailNotVerified extends AuthException {
        public EmailNotVerified() {
            super("Email not verified", "EMAIL_NOT_VERIFIED");
        }
    }

    public static class InvalidPassword extends AuthException {
        public InvalidPassword(String reason) {
            super("Password does not meet requirements: " + reason, "INVALID_PASSWORD");
        }
    }

    public static class RoleNotFound extends AuthException {
        public RoleNotFound(Long id) {
            super("Role not found: " + id, "ROLE_NOT_FOUND");
        }

        public RoleNotFound(String name) {
            super("Role not found: " + name, "ROLE_NOT_FOUND");
        }
    }

    public static class RoleAlreadyExists extends AuthException {
        public RoleAlreadyExists(String name) {
            super("Role already exists: " + name, "ROLE_ALREADY_EXISTS");
        }
    }

    public static class PermissionNotFound extends AuthException {
        public PermissionNotFound(Long id) {
            super("Permission not found: " + id, "PERMISSION_NOT_FOUND");
        }

        public PermissionNotFound(String name) {
            super("Permission not found: " + name, "PERMISSION_NOT_FOUND");
        }
    }

    public static class PermissionAlreadyExists extends AuthException {
        public PermissionAlreadyExists(String name) {
            super("Permission already exists: " + name, "PERMISSION_ALREADY_EXISTS");
        }
    }

    public static class AccessDenied extends AuthException {
        public AccessDenied() {
            super("Access denied", "ACCESS_DENIED");
        }

        public AccessDenied(String message) {
            super(message, "ACCESS_DENIED");
        }
    }
}
