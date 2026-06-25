package io.github.biruksolomon.auth.event;

import io.github.biruksolomon.auth.domain.User;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Thin wrapper around Spring's {@link ApplicationEventPublisher}.
 * Services call {@code eventPublisher.publish(...)} — never the raw publisher —
 * so the event contract stays in the auth domain.
 */
public class AuthEventPublisher {

    private final ApplicationEventPublisher publisher;

    public AuthEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publish(AuthEventType type, User user, String detail) {
        publisher.publishEvent(new AuthEvent(this, type, user, detail));
    }
}
