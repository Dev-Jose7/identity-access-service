package io.identityaccess.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("role")
public record RoleRow(
        @Id @Column("role_id") String roleId,
        @Column("role_code") String roleCode,
        @Column("description") String description,
        @Column("created_at") Instant createdAt,
        @Column("updated_at") Instant updatedAt) {}
