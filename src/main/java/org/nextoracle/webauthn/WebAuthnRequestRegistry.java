package org.nextoracle.webauthn;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Short-lived, in-memory store for pending WebAuthn ceremonies (challenges).
 * <p>
 * A {@code requestId} is handed to the browser when a ceremony starts and must be
 * presented again on finish, so the server can match the response to the original
 * challenge. Entries expire after {@code app.webauthn.challenge-ttl-seconds}.
 * <p>
 * NOTE: this is per-instance. For a multi-node deployment behind a load balancer,
 * back this with a shared store (e.g. Redis) or enable sticky sessions.
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class WebAuthnRequestRegistry {

    private final WebAuthnProperties properties;

    private record Entry(Object value, Instant expiresAt) {
    }

    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    public String store(Object ceremony) {
        purgeExpired();
        String requestId = UUID.randomUUID().toString();
        store.put(requestId, new Entry(ceremony,
                Instant.now().plusSeconds(properties.getChallengeTtlSeconds())));
        return requestId;
    }

    public <T> Optional<T> consume(String requestId, Class<T> type) {
        Entry entry = store.remove(requestId);
        if (entry == null || entry.expiresAt().isBefore(Instant.now())) {
            return Optional.empty();
        }
        return Optional.of(type.cast(entry.value()));
    }

    private void purgeExpired() {
        Instant now = Instant.now();
        store.entrySet().removeIf(e -> e.getValue().expiresAt().isBefore(now));
    }
}

