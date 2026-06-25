package io.github.biruksolomon.auth.event;

import io.github.biruksolomon.auth.domain.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

/**
 * Application event fired for every significant auth action.
 * Consuming applications listen with {@code @EventListener(AuthEvent.class)}
 * and can build audit logs, send notifications, etc.
 */
@Getter
public class AuthEvent extends ApplicationEvent {

    private final AuthEventType type;
    private final User user;
    /** Optional extra context (e.g. IP address, device info). */
    private final String detail;
    private final Instant occurredAt;

    public AuthEvent(Object source, AuthEventType type, User user, String detail) {
        super(source);
        this.type = type;
        this.user = user;
        this.detail = detail;
        this.occurredAt = Instant.now();
    }

}
