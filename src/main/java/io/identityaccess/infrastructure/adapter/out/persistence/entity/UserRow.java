package io.identityaccess.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("user_account")
public record UserRow(
        @Id @Column("user_id") String userId,
        @Column("email") String email,
        @Column("status") String status,
        @Column("failed_login_count") Integer failedLoginCount,
        @Column("created_at") Instant createdAt,
        @Column("updated_at") Instant updatedAt) {}
