package io.github.biruksolomon.auth.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "auth_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String firstName;
    private String lastName;

    @Column(nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column(nullable = false)
    @Builder.Default
    private boolean accountLocked = false;

    @Builder.Default
    private int failedLoginAttempts = 0;

    private LocalDateTime lockedUntil;

    private LocalDateTime deletedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "auth_user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .flatMap(role -> {
                    Set<GrantedAuthority> authorities = new HashSet<>();
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
                    authorities.addAll(
                            role.getPermissions().stream()
                                    .map(permission -> new SimpleGrantedAuthority(permission.getName()))
                                    .collect(Collectors.toSet())
                    );
                    return authorities.stream();
                })
                .collect(Collectors.toSet());
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked && status != UserStatus.LOCKED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status.canLogin();
    }

    public boolean isActiveAndNotDeleted() {
        return status == UserStatus.ACTIVE;
    }

    public void lockAccount() {
        this.status = UserStatus.LOCKED;
        this.accountLocked = true;
        this.lockedUntil = LocalDateTime.now().plusMinutes(30);
    }

    public void unlockAccount() {
        this.status = UserStatus.ACTIVE;
        this.accountLocked = false;
        this.lockedUntil = null;
        this.failedLoginAttempts = 0;
    }

    public void disableAccount() {
        this.status = UserStatus.DISABLED;
        this.accountLocked = false;
    }

    public void softDelete() {
        this.status = UserStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }

    public void restore() {
        this.status = UserStatus.ACTIVE;
        this.deletedAt = null;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
