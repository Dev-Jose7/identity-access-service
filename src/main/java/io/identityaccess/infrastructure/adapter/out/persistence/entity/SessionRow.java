package io.identityaccess.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("user_session")
public record SessionRow(
        @Id @Column("session_id") String sessionId,
        @Column("user_id") String userId,
        @Column("device_id") String deviceId,
        @Column("device_name") String deviceName,
        @Column("device_type") String deviceType,
        @Column("ip_address") String ipAddress,
        @Column("access_jti") String accessJti,
        @Column("refresh_jti") String refreshJti,
        @Column("issued_at") Instant issuedAt,
        @Column("access_token_expires_at") Instant accessTokenExpiresAt,
        @Column("refresh_token_expires_at") Instant refreshTokenExpiresAt,
        @Column("last_seen_at") Instant lastSeenAt,
        @Column("status") String status,
        @Column("revoked_at") Instant revokedAt,
        @Column("revocation_reason") String revocationReason,
        @Column("created_at") Instant createdAt,
        @Column("updated_at") Instant updatedAt) {}
