package com.arka.identityaccess.infrastructure.adapter.out.persistence.repository;

import com.arka.identityaccess.infrastructure.adapter.out.persistence.entity.UserLoginAttemptRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ReactiveUserLoginAttemptRepository extends ReactiveCrudRepository<UserLoginAttemptRow, String> {

    @Query("""
            SELECT attempt_id, user_id, ip_address, success, attempt_at, created_at
            FROM user_login_attempt
            WHERE user_id = :userId
            ORDER BY attempt_at DESC
            LIMIT 5
            """)
    Flux<UserLoginAttemptRow> findRecentByUserId(@Param("userId") String userId);
}
