package io.identityaccess.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("credential_password")
public record UserCredentialRow(
        @Id @Column("credential_id") String credentialId,
        @Column("user_id") String userId,
        @Column("password_hash") String passwordHash,
        @Column("hash_algorithm") String hashAlgorithm,
        @Column("password_changed_at") Instant passwordChangedAt,
        @Column("status") String status,
        @Column("updated_at") Instant updatedAt) {}
