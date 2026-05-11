package io.identityaccess.infrastructure.adapter.out.persistence.mapper;

import io.identityaccess.domain.model.session.SessionAggregate;
import io.identityaccess.domain.model.user.UserAggregate;
import io.identityaccess.infrastructure.adapter.out.persistence.entity.AuthAuditRow;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class AuthAuditRowMapper {

    private final ObjectMapper objectMapper;

    public AuthAuditRowMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AuthAuditRow toLoginSuccessRow(UserAggregate user, SessionAggregate session) {
        return row(
                "LOGIN_SUCCESS",
                user.id().value(),
                session.id().value(),
                session.clientIp().value(),
                session.clientDevice().deviceId(),
                "SUCCESS",
                Map.of("sessionId", session.id().value(), "accessJti", session.accessJti().value()));
    }

    public AuthAuditRow toRegisterRow(UserAggregate user) {
        return row(
                "ACCOUNT_REGISTERED",
                user.id().value(),
                null,
                null,
                null,
                "SUCCESS",
                Map.of("email", user.email().value()));
    }

    public AuthAuditRow toSessionEventRow(String eventType, SessionAggregate session) {
        return row(
                eventType,
                session.userId().value(),
                session.id().value(),
                session.clientIp().value(),
                session.clientDevice().deviceId(),
                "SUCCESS",
                Map.of("sessionId", session.id().value()));
    }

    public AuthAuditRow toRoleAssignedToAccountRow(String actorUserId, String targetUserId, String roleCode, boolean assigned) {
        return row(
                "ROLE_ASSIGNED_TO_ACCOUNT",
                targetUserId,
                null,
                null,
                null,
                assigned ? "SUCCESS" : "NOOP",
                Map.of(
                        "actorUserId", normalize(actorUserId),
                        "targetUserId", normalize(targetUserId),
                        "roleCode", normalize(roleCode),
                        "assigned", assigned));
    }

    public AuthAuditRow toAccountBlockedRow(String actorUserId, String targetUserId, String reason, boolean changed) {
        return row(
                "ACCOUNT_BLOCKED",
                targetUserId,
                null,
                null,
                null,
                changed ? "SUCCESS" : "NOOP",
                Map.of(
                        "actorUserId", normalize(actorUserId),
                        "targetUserId", normalize(targetUserId),
                        "reason", normalize(reason),
                        "changed", changed));
    }

    public AuthAuditRow toAccountUnblockedRow(String actorUserId, String targetUserId, String reason, boolean changed) {
        return row(
                "ACCOUNT_UNBLOCKED",
                targetUserId,
                null,
                null,
                null,
                changed ? "SUCCESS" : "NOOP",
                Map.of(
                        "actorUserId", normalize(actorUserId),
                        "targetUserId", normalize(targetUserId),
                        "reason", normalize(reason),
                        "changed", changed));
    }

    public AuthAuditRow toSessionsRevokedRow(String actorUserId, String targetUserId, String reason, long revokedSessions) {
        return row(
                "ACCOUNT_SESSIONS_REVOKED",
                targetUserId,
                null,
                null,
                null,
                "SUCCESS",
                Map.of(
                        "actorUserId", normalize(actorUserId),
                        "targetUserId", normalize(targetUserId),
                        "reason", normalize(reason),
                        "revokedSessions", revokedSessions));
    }

    public AuthAuditRow toAccessCatalogChangedRow(
            String actorUserId,
            String eventType,
            String targetType,
            String targetId,
            String targetCode,
            boolean changed) {
        return row(
                normalize(eventType).isBlank() ? "ACCESS_CATALOG_CHANGED" : normalize(eventType),
                normalize(actorUserId).isBlank() ? null : normalize(actorUserId),
                null,
                null,
                null,
                changed ? "SUCCESS" : "NOOP",
                Map.of(
                        "actorUserId", normalize(actorUserId),
                        "targetType", normalize(targetType),
                        "targetId", normalize(targetId),
                        "targetCode", normalize(targetCode),
                        "changed", changed));
    }

    private AuthAuditRow row(
            String eventType,
            String userId,
            String sessionId,
            String ipAddress,
            String deviceId,
            String result,
            Map<String, Object> payload) {
        return new AuthAuditRow(
                UUID.randomUUID().toString(),
                eventType,
                userId,
                sessionId,
                ipAddress,
                deviceId,
                result,
                toJson(payload),
                Instant.now());
    }

    private String toJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload == null ? Map.of() : payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize audit payload", exception);
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
