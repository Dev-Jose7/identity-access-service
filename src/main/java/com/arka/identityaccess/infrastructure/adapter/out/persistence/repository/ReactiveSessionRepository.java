package com.arka.identityaccess.infrastructure.adapter.out.persistence.repository;

import com.arka.identityaccess.infrastructure.adapter.out.persistence.entity.SessionRow;
import java.time.Instant;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveSessionRepository extends ReactiveCrudRepository<SessionRow, String> {

    @Modifying
    @Query("""
            INSERT INTO user_session (
                session_id,
                user_id,
                device_id,
                device_name,
                device_type,
                ip_address,
                access_jti,
                refresh_jti,
                issued_at,
                access_token_expires_at,
                refresh_token_expires_at,
                last_seen_at,
                status,
                revoked_at,
                revocation_reason,
                created_at,
                updated_at
            ) VALUES (
                :sessionId,
                :userId,
                :deviceId,
                :deviceName,
                :deviceType,
                :ipAddress,
                :accessJti,
                :refreshJti,
                :issuedAt,
                :accessTokenExpiresAt,
                :refreshTokenExpiresAt,
                :lastSeenAt,
                :status,
                :revokedAt,
                :revocationReason,
                :createdAt,
                :updatedAt
            )
            """)
    Mono<Integer> insert(
            @Param("sessionId") String sessionId,
            @Param("userId") String userId,
            @Param("deviceId") String deviceId,
            @Param("deviceName") String deviceName,
            @Param("deviceType") String deviceType,
            @Param("ipAddress") String ipAddress,
            @Param("accessJti") String accessJti,
            @Param("refreshJti") String refreshJti,
            @Param("issuedAt") Instant issuedAt,
            @Param("accessTokenExpiresAt") Instant accessTokenExpiresAt,
            @Param("refreshTokenExpiresAt") Instant refreshTokenExpiresAt,
            @Param("lastSeenAt") Instant lastSeenAt,
            @Param("status") String status,
            @Param("revokedAt") Instant revokedAt,
            @Param("revocationReason") String revocationReason,
            @Param("createdAt") Instant createdAt,
            @Param("updatedAt") Instant updatedAt);

    @Modifying
    @Query("""
            UPDATE user_session
            SET access_jti = :accessJti,
                refresh_jti = :refreshJti,
                issued_at = :issuedAt,
                access_token_expires_at = :accessTokenExpiresAt,
                refresh_token_expires_at = :refreshTokenExpiresAt,
                last_seen_at = :lastSeenAt,
                status = :status,
                revoked_at = :revokedAt,
                revocation_reason = :revocationReason,
                updated_at = :updatedAt
            WHERE session_id = :sessionId
            """)
    Mono<Integer> update(
            @Param("sessionId") String sessionId,
            @Param("accessJti") String accessJti,
            @Param("refreshJti") String refreshJti,
            @Param("issuedAt") Instant issuedAt,
            @Param("accessTokenExpiresAt") Instant accessTokenExpiresAt,
            @Param("refreshTokenExpiresAt") Instant refreshTokenExpiresAt,
            @Param("lastSeenAt") Instant lastSeenAt,
            @Param("status") String status,
            @Param("revokedAt") Instant revokedAt,
            @Param("revocationReason") String revocationReason,
            @Param("updatedAt") Instant updatedAt);

    @Query("""
            SELECT *
            FROM user_session
            WHERE refresh_jti = :refreshJti
              AND status = 'ACTIVE'
            LIMIT 1
            """)
    Mono<SessionRow> findActiveByRefreshJti(@Param("refreshJti") String refreshJti);

    @Query("""
            UPDATE user_session
            SET status = 'REVOKED',
                revoked_at = :revokedAt,
                revocation_reason = :revocationReason,
                last_seen_at = :revokedAt,
                updated_at = :updatedAt
            WHERE user_id = :userId
              AND status = 'ACTIVE'
            RETURNING *
            """)
    Flux<SessionRow> revokeActiveByUserId(
            @Param("userId") String userId,
            @Param("revocationReason") String revocationReason,
            @Param("revokedAt") Instant revokedAt,
            @Param("updatedAt") Instant updatedAt);
}
