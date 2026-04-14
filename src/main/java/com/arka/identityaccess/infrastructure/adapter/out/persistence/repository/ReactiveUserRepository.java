package com.arka.identityaccess.infrastructure.adapter.out.persistence.repository;

import com.arka.identityaccess.infrastructure.adapter.out.persistence.entity.UserRow;
import java.time.Instant;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ReactiveUserRepository extends ReactiveCrudRepository<UserRow, String> {

    Mono<UserRow> findByEmail(String email);

    Mono<Boolean> existsByEmail(String email);

    @Modifying
    @Query("""
            INSERT INTO user_account (user_id, email, status, failed_login_count, created_at, updated_at)
            VALUES (:userId, :email, :status, :failedLoginCount, :createdAt, :updatedAt)
            """)
    Mono<Integer> insert(
            @Param("userId") String userId,
            @Param("email") String email,
            @Param("status") String status,
            @Param("failedLoginCount") Integer failedLoginCount,
            @Param("createdAt") Instant createdAt,
            @Param("updatedAt") Instant updatedAt);

    @Modifying
    @Query("""
            UPDATE user_account
            SET status = :status,
                updated_at = :updatedAt
            WHERE user_id = :userId
            """)
    Mono<Integer> updateStatus(
            @Param("userId") String userId,
            @Param("status") String status,
            @Param("updatedAt") Instant updatedAt);

    @Modifying
    @Query("""
            UPDATE user_account
            SET failed_login_count = CASE
                    WHEN :success THEN 0
                    ELSE failed_login_count + 1
                END,
                updated_at = :updatedAt
            WHERE user_id = :userId
            """)
    Mono<Integer> updateFailedLoginCount(
            @Param("userId") String userId,
            @Param("success") boolean success,
            @Param("updatedAt") Instant updatedAt);
}
