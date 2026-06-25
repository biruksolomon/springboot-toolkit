package io.github.biruksolomon.auth.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A fine-grained permission authority (e.g. {@code users:read}, {@code reports:export}).
 * Permissions are owned by Roles; users inherit them transitively.
 *
 * Naming convention: {@code resource:action}, all lower-case with colon separator.
 */
@Entity
@Table(name = "auth_permissions",
        uniqueConstraints = @UniqueConstraint(name = "uq_auth_permissions_name", columnNames = "name"))
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false)
    private Long id;

    /** e.g. "users:read", "reports:export" */
    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    /** Back-reference — roles that hold this permission. */
    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Permission(String name) { this.name = name; }

    public Permission(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Permission p)) return false;
        return Objects.equals(name, p.name);
    }

    @Override
    public int hashCode() { return Objects.hashCode(name); }

    @Override
    public String toString() { return "Permission{id=" + id + ", name='" + name + "'}"; }
}
