package com.arka.identityaccess.infrastructure.adapter.out.persistence.repository;

import com.arka.identityaccess.infrastructure.adapter.out.persistence.entity.CredentialRow;
import java.time.Instant;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ReactiveCredentialRepository extends ReactiveCrudRepository<CredentialRow, String> {

    @Modifying
    @Query("""
            INSERT INTO credential (
                credential_id,
                user_id,
                credential_type,
                credential_purpose,
                provider,
                status,
                last_used_at,
                created_at,
                updated_at
            ) VALUES (
                :credentialId,
                :userId,
                :credentialType,
                :credentialPurpose,
                :provider,
                :status,
                :lastUsedAt,
                :createdAt,
                :updatedAt
            )
            """)
    Mono<Integer> insert(
            @Param("credentialId") String credentialId,
            @Param("userId") String userId,
            @Param("credentialType") String credentialType,
            @Param("credentialPurpose") String credentialPurpose,
            @Param("provider") String provider,
            @Param("status") String status,
            @Param("lastUsedAt") Instant lastUsedAt,
            @Param("createdAt") Instant createdAt,
            @Param("updatedAt") Instant updatedAt);
}
