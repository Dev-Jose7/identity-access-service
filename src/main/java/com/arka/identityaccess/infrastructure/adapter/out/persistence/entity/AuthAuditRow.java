package com.arka.identityaccess.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("auth_audit")
public record AuthAuditRow(
        @Id @Column("audit_id") String auditId,
        @Column("event_type") String eventType,
        @Column("user_id") String userId,
        @Column("session_id") String sessionId,
        @Column("ip_address") String ipAddress,
        @Column("device_id") String deviceId,
        @Column("result") String result,
        @Column("payload") String payload,
        @Column("created_at") Instant occurredAt) {}
