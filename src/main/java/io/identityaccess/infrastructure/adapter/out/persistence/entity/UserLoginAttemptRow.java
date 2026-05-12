package io.identityaccess.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("user_login_attempt")
public record UserLoginAttemptRow(
        @Id @Column("attempt_id") String attemptId,
        @Column("user_id") String userId,
        @Column("ip_address") String ipAddress,
        @Column("success") boolean success,
        @Column("attempt_at") Instant attemptAt,
        @Column("created_at") Instant createdAt) {}
