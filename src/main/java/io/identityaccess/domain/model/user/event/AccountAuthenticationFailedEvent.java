package io.identityaccess.domain.model.user.event;

import io.identityaccess.domain.event.DomainEvent;
import io.identityaccess.domain.model.session.valueobject.ClientIp;
import io.identityaccess.domain.model.user.valueobject.EmailAddress;
import io.identityaccess.domain.model.user.valueobject.UserId;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public record AccountAuthenticationFailedEvent(
        String eventId,
        Instant occurredAt,
        EmailAddress email,
        UserId userId,
        ClientIp clientIp,
        String failureReason) implements DomainEvent {

    public static AccountAuthenticationFailedEvent create(
            EmailAddress email,
            UserId userId,
            ClientIp clientIp,
            String failureReason,
            Instant occurredAt) {
        return new AccountAuthenticationFailedEvent(
                UUID.randomUUID().toString(),
                occurredAt,
                email,
                userId,
                clientIp,
                failureReason);
    }

    public AccountAuthenticationFailedEvent {
        if (eventId == null || eventId.isBlank()) eventId = UUID.randomUUID().toString();
        if (occurredAt == null) throw new IllegalArgumentException("occurredAt is required");
        if (email == null) throw new IllegalArgumentException("email is required");
        if (clientIp == null) throw new IllegalArgumentException("clientIp is required");
        failureReason = normalizeReason(failureReason);
    }

    @Override
    public String eventType() {
        return "AccountAuthenticationFailed";
    }

    @Override
    public String aggregateId() {
        return userId == null ? email.normalized() : userId.value();
    }

    @Override
    public Map<String, Object> payload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("email", email.value());
        if (userId != null) {
            payload.put("userId", userId.value());
        }
        payload.put("clientIp", clientIp.value());
        payload.put("failureReason", failureReason);
        payload.put("occurredAt", occurredAt.toString());
        return Map.copyOf(payload);
    }

    private static String normalizeReason(String value) {
        if (value == null || value.isBlank()) {
            return "AUTHENTICATION_FAILED";
        }
        return value.trim().toUpperCase();
    }
}
