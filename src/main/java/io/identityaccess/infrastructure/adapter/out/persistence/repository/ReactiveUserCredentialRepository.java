package io.identityaccess.infrastructure.adapter.out.persistence.repository;

import io.identityaccess.infrastructure.adapter.out.persistence.entity.UserCredentialRow;
import java.time.Instant;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ReactiveUserCredentialRepository extends ReactiveCrudRepository<UserCredentialRow, String> {

    @Query("""
            SELECT c.credential_id,
                   c.user_id,
                   cp.password_hash,
                   cp.hash_algorithm,
                   cp.password_changed_at,
                   c.status,
                   cp.updated_at
            FROM credential c
            JOIN credential_password cp ON cp.credential_id = c.credential_id
            WHERE c.user_id = :userId
              AND c.status = 'ACTIVE'
              AND c.credential_type = 'PASSWORD'
              AND c.credential_purpose = 'PRIMARY'
            LIMIT 1
            """)
    Mono<UserCredentialRow> findActiveByUserId(@Param("userId") String userId);

    @Modifying
    @Query("""
            INSERT INTO credential_password (
                credential_id,
                password_hash,
                hash_algorithm,
                password_changed_at,
                created_at,
                updated_at
            ) VALUES (
                :credentialId,
                :passwordHash,
                :hashAlgorithm,
                :passwordChangedAt,
                :createdAt,
                :updatedAt
            )
            """)
    Mono<Integer> insertPassword(
            @Param("credentialId") String credentialId,
            @Param("passwordHash") String passwordHash,
            @Param("hashAlgorithm") String hashAlgorithm,
            @Param("passwordChangedAt") Instant passwordChangedAt,
            @Param("createdAt") Instant createdAt,
            @Param("updatedAt") Instant updatedAt);
}
