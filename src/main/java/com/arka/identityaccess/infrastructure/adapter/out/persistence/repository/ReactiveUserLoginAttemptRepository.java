package com.arka.identityaccess.infrastructure.adapter.out.persistence.repository;

import com.arka.identityaccess.infrastructure.adapter.out.persistence.entity.UserLoginAttemptRow;
import java.time.Instant;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveUserLoginAttemptRepository extends ReactiveCrudRepository<UserLoginAttemptRow, String> {

    @Query("""
            SELECT attempt_id, user_id, ip_address, success, attempt_at, created_at
            FROM user_login_attempt
            WHERE user_id = :userId
            ORDER BY attempt_at DESC
            LIMIT 5
            """)
    Flux<UserLoginAttemptRow> findRecentByUserId(@Param("userId") String userId);

    @Modifying
    @Query("""
            INSERT INTO user_login_attempt (
                attempt_id,
                user_id,
                ip_address,
                success,
                attempt_at,
                created_at
            ) VALUES (
                :attemptId,
                :userId,
                :ipAddress,
                :success,
                :attemptAt,
                :createdAt
            )
            """)
    Mono<Integer> insert(
            @Param("attemptId") String attemptId,
            @Param("userId") String userId,
            @Param("ipAddress") String ipAddress,
            @Param("success") boolean success,
            @Param("attemptAt") Instant attemptAt,
            @Param("createdAt") Instant createdAt);
}
