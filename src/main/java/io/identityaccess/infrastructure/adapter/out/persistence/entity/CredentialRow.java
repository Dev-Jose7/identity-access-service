package io.identityaccess.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("credential")
public record CredentialRow(
        @Id @Column("credential_id") String credentialId,
        @Column("user_id") String userId,
        @Column("credential_type") String credentialType,
        @Column("credential_purpose") String credentialPurpose,
        @Column("provider") String provider,
        @Column("status") String status,
        @Column("last_used_at") Instant lastUsedAt,
        @Column("created_at") Instant createdAt,
        @Column("updated_at") Instant updatedAt) {}
