package com.arka.identityaccess.infrastructure.adapter.out.persistence.mapper;

import com.arka.identityaccess.domain.session.aggregate.SessionAggregate;
import com.arka.identityaccess.domain.identity.aggregate.AccountAggregate;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.entity.AuthAuditRow;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class AuthAuditRowMapper {

    private final ObjectMapper objectMapper;

    public AuthAuditRowMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AuthAuditRow toLoginSuccessRow(AccountAggregate account, SessionAggregate session) {
        Instant now = Instant.now();
        String payload = toJson(Map.of(
                "sessionId", session.id().value(),
                "accessJti", session.accessJti().value()));
        return new AuthAuditRow(
                UUID.randomUUID().toString(),
                "LOGIN_SUCCESS",
                account.id().value(),
                session.id().value(),
                session.clientIp().value(),
                session.clientDevice().deviceId(),
                "SUCCESS",
                payload,
                now);
    }

    public AuthAuditRow toRegisterRow(AccountAggregate account) {
        Instant now = Instant.now();
        String payload = toJson(Map.of("email", account.email().value()));
        return new AuthAuditRow(
                UUID.randomUUID().toString(),
                "USER_REGISTERED",
                account.id().value(),
                null,
                null,
                null,
                "SUCCESS",
                payload,
                now);
    }

    public AuthAuditRow toSessionEventRow(String eventType, SessionAggregate session) {
        Instant now = Instant.now();
        String payload = toJson(Map.of("sessionId", session.id().value()));
        return new AuthAuditRow(
                UUID.randomUUID().toString(),
                eventType,
                session.userId().value(),
                session.id().value(),
                session.clientIp().value(),
                session.clientDevice().deviceId(),
                "SUCCESS",
                payload,
                now);
    }

    public AuthAuditRow toRoleAssignedRow(String actorUserId, String targetUserId, String roleCode, boolean assigned) {
        Instant now = Instant.now();
        Map<String, Object> payloadMap = new LinkedHashMap<>();
        payloadMap.put("actorUserId", normalize(actorUserId));
        payloadMap.put("targetUserId", normalize(targetUserId));
        payloadMap.put("roleCode", normalize(roleCode));
        payloadMap.put("assigned", assigned);
        String payload = toJson(payloadMap);
        return new AuthAuditRow(
                UUID.randomUUID().toString(),
                "ROLE_ASSIGNED",
                targetUserId,
                null,
                null,
                null,
                assigned ? "SUCCESS" : "NOOP",
                payload,
                now);
    }

    public AuthAuditRow toUserBlockedRow(String actorUserId, String targetUserId, String reason, boolean changed) {
        Instant now = Instant.now();
        Map<String, Object> payloadMap = new LinkedHashMap<>();
        payloadMap.put("actorUserId", normalize(actorUserId));
        payloadMap.put("targetUserId", normalize(targetUserId));
        payloadMap.put("reason", normalize(reason));
        payloadMap.put("changed", changed);
        String payload = toJson(payloadMap);
        return new AuthAuditRow(
                UUID.randomUUID().toString(),
                "USER_BLOCKED",
                targetUserId,
                null,
                null,
                null,
                changed ? "SUCCESS" : "NOOP",
                payload,
                now);
    }

    public AuthAuditRow toSessionsRevokedRow(String actorUserId, String targetUserId, String reason, long revokedSessions) {
        Instant now = Instant.now();
        Map<String, Object> payloadMap = new LinkedHashMap<>();
        payloadMap.put("actorUserId", normalize(actorUserId));
        payloadMap.put("targetUserId", normalize(targetUserId));
        payloadMap.put("reason", normalize(reason));
        payloadMap.put("revokedSessions", revokedSessions);
        String payload = toJson(payloadMap);
        return new AuthAuditRow(
                UUID.randomUUID().toString(),
                "USER_SESSIONS_REVOKED",
                targetUserId,
                null,
                null,
                null,
                "SUCCESS",
                payload,
                now);
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\"", "").trim();
    }

    private String toJson(Map<String, ?> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize auth audit payload", exception);
        }
    }
}
