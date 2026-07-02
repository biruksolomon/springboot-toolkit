package io.github.biruksolomon.auth.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "auth_audit_logs", indexes = {
        @Index(name = "idx_audit_user_id", columnList = "user_id"),
        @Index(name = "idx_audit_action", columnList = "action"),
        @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
        @Index(name = "idx_audit_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // Null if system action

    @Column(nullable = false, length = 100)
    private String action; // e.g., "LOGIN", "USER_CREATED", "PERMISSION_ASSIGNED", "API_KEY_GENERATED"

    @Column(length = 50)
    private String resourceType; // e.g., "USER", "ROLE", "PERMISSION", "API_KEY"

    @Column(length = 500)
    private String resourceId; // e.g., "user_id:42", "role_id:5", "api_key_id:1"

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Status status; // SUCCESS, FAILURE

    @Column(length = 500)
    private String details; // Additional info (e.g., reason for failure)

    @Column(length = 50)
    private String clientIp;

    @Column(length = 500)
    private String userAgent;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    public enum Status {
        SUCCESS, FAILURE
    }
}