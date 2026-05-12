package io.identityaccess.infrastructure.adapter.out.persistence.repository;

import io.identityaccess.infrastructure.adapter.out.persistence.entity.AuthAuditRow;
import java.time.Instant;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ReactiveAuthAuditRepository extends ReactiveCrudRepository<AuthAuditRow, String> {

    @Modifying
    @Query("""
            INSERT INTO auth_audit (
                audit_id,
                event_type,
                user_id,
                session_id,
                ip_address,
                device_id,
                result,
                payload,
                created_at
            ) VALUES (
                :auditId,
                :eventType,
                :userId,
                :sessionId,
                :ipAddress,
                :deviceId,
                :result,
                CAST(:payload AS jsonb),
                :createdAt
            )
            """)
    Mono<Integer> insert(
            @Param("auditId") String auditId,
            @Param("eventType") String eventType,
            @Param("userId") String userId,
            @Param("sessionId") String sessionId,
            @Param("ipAddress") String ipAddress,
            @Param("deviceId") String deviceId,
            @Param("result") String result,
            @Param("payload") String payload,
            @Param("createdAt") Instant createdAt);
}
