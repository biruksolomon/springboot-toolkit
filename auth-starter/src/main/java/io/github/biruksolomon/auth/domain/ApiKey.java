package io.github.biruksolomon.auth.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "auth_api_keys", indexes = {
        @Index(name = "idx_api_key_hash", columnList = "key_hash", unique = true),
        @Index(name = "idx_api_key_user", columnList = "user_id"),
        @Index(name = "idx_api_key_active", columnList = "active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiKey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name; // Human-readable name (e.g., "Mobile App", "Webhook Service")

    @Column(nullable = false, unique = true)
    private String keyHash; // SHA-256 hash of actual key (never store plain text)

    @Column(nullable = false)
    private String keyPrefix; // First 8 chars of key for identification (e.g., "sk_live_")

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "api_key_permissions",
            joinColumns = @JoinColumn(name = "api_key_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();

    private LocalDateTime expiresAt;
    private LocalDateTime lastUsedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime revokedAt;

    public boolean isValid() {
        return active && (expiresAt == null || LocalDateTime.now().isBefore(expiresAt));
    }

    public void revoke() {
        this.active = false;
        this.revokedAt = LocalDateTime.now();
    }
}
