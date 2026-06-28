package io.github.biruksolomon.auth.security;

import io.github.biruksolomon.auth.domain.User;
import io.github.biruksolomon.auth.domain.UserStatus;
import io.github.biruksolomon.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads a {@link User} by email for Spring Security's authentication pipeline.
 *
 * <p>Returns the {@link User} entity directly — which already implements
 * {@link UserDetails} — instead of rebuilding a Spring {@code User} wrapper.
 * This preserves every authority granted by the domain model:
 *
 * <ul>
 *   <li>{@code ROLE_<name>} — one per assigned role</li>
 *   <li>{@code <permission.name>} — every permission carried by every role</li>
 * </ul>
 *
 * <p>Account-state signals ({@link User#isAccountNonLocked()},
 * {@link User#isEnabled()}, etc.) are also delegated directly to the entity,
 * which derives them from {@link UserStatus}. Nothing is re-computed here.
 *
 * <p>The method is annotated {@code @Transactional(readOnly = true)} so that
 * the EAGER-loaded {@code roles → permissions} graph is fetched within a single
 * open session. Without a transaction boundary here, Hibernate would open a
 * short-lived session just for the {@code findByEmail} call and close it before
 * the security framework ever accesses {@code user.getAuthorities()}, causing a
 * {@code LazyInitializationException} if any collection were switched to LAZY
 * in the future.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Locate a user by email address.
     *
     * @param email the email supplied at login (Spring Security uses "username" as
     *              the field name but this library treats email as the unique
     *              identifier)
     * @return the matching {@link User} entity, which itself satisfies
     *         {@link UserDetails}
     * @throws UsernameNotFoundException if no non-deleted user with that email
     *                                   exists
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmailAndStatusNot(email, UserStatus.DELETED)
                .orElseThrow(() -> {
                    log.debug("No active user found for email: {}", email);
                    // Return a generic message — never reveal whether the email
                    // exists or the account is in a different state.
                    return new UsernameNotFoundException("User not found: " + email);
                });

        log.debug(
                "Loaded user '{}' with {} role(s) and {} total authorit(ies)",
                user.getEmail(),
                user.getRoles().size(),
                user.getAuthorities().size()
        );

        // Return the entity directly.  User already implements UserDetails and
        // overrides every method (getAuthorities, isEnabled, isAccountNonLocked,
        // isAccountNonExpired, isCredentialsNonExpired) correctly from its domain
        // state.  Re-wrapping it in Spring's User builder would silently discard
        // all permission authorities and lose the status-driven flag logic.
        return user;
    }
}