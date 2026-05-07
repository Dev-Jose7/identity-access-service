package com.arka.identityaccess.infrastructure.adapter.out.persistence.mapper;

import com.arka.identityaccess.domain.model.session.SessionAggregate;
import com.arka.identityaccess.domain.model.user.UserAggregate;
import com.arka.identityaccess.infrastructure.adapter.out.persistence.entity.AuthAuditRow;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class AuthAuditRowMapper {

    public AuthAuditRow toLoginSuccessRow(UserAggregate user, SessionAggregate session) {
        Instant now = Instant.now();
        String payload = "{\"sessionId\":\"" + session.id().value() + "\",\"accessJti\":\"" + session.accessJti().value() + "\"}";
        return new AuthAuditRow(
                UUID.randomUUID().toString(),
                "LOGIN_SUCCESS",
                user.id().value(),
                session.id().value(),
                session.clientIp().value(),
                session.clientDevice().deviceId(),
                "SUCCESS",
                payload,
                now);
    }

    public AuthAuditRow toRegisterRow(UserAggregate user) {
        Instant now = Instant.now();
        String payload = "{\"email\":\"" + user.email().value() + "\"}";
        return new AuthAuditRow(
                UUID.randomUUID().toString(),
                "USER_REGISTERED",
                user.id().value(),
                null,
                null,
                null,
                "SUCCESS",
                payload,
                now);
    }

    public AuthAuditRow toSessionEventRow(String eventType, SessionAggregate session) {
        Instant now = Instant.now();
        String payload = "{\"sessionId\":\"" + session.id().value() + "\"}";
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
        String payload = "{\"actorUserId\":\"" + normalize(actorUserId) + "\",\"targetUserId\":\"" + normalize(targetUserId)
                + "\",\"roleCode\":\"" + normalize(roleCode) + "\",\"assigned\":" + assigned + "}";
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
        String payload = "{\"actorUserId\":\"" + normalize(actorUserId) + "\",\"targetUserId\":\"" + normalize(targetUserId)
                + "\",\"reason\":\"" + normalize(reason) + "\",\"changed\":" + changed + "}";
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

    public AuthAuditRow toSessionsRevokedByUserRow(String actorUserId, String targetUserId, String reason, long revokedSessions) {
        Instant now = Instant.now();
        String payload = "{\"actorUserId\":\"" + normalize(actorUserId) + "\",\"targetUserId\":\"" + normalize(targetUserId)
                + "\",\"reason\":\"" + normalize(reason) + "\",\"revokedSessions\":" + revokedSessions + "}";
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
}
