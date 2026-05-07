package com.arka.identityaccess.domain.model.user.entity;

import com.arka.identityaccess.domain.model.session.valueobject.ClientIp;
import com.arka.identityaccess.domain.model.user.valueobject.UserId;
import java.time.Instant;
import java.util.UUID;

public record UserLoginAttempt(
        String attemptId,
        UserId userId,
        Instant occurredAt,
        ClientIp clientIp,
        boolean success) {

    public UserLoginAttempt {
        if (attemptId == null || attemptId.isBlank()) {
            throw new IllegalArgumentException("attemptId is required");
        }
        if (userId == null || occurredAt == null || clientIp == null) {
            throw new IllegalArgumentException("login attempt is incomplete");
        }
    }

    public static UserLoginAttempt success(UserId userId, ClientIp clientIp, Instant occurredAt) {
        return new UserLoginAttempt(UUID.randomUUID().toString(), userId, occurredAt, clientIp, true);
    }

    public static UserLoginAttempt failed(UserId userId, ClientIp clientIp, Instant occurredAt) {
        return new UserLoginAttempt(UUID.randomUUID().toString(), userId, occurredAt, clientIp, false);
    }
}
