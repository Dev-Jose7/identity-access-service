package com.arka.identityaccess.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("user_role_assignment")
public record UserRoleRow(
        @Id @Column("assignment_id") String assignmentId,
        @Column("user_id") String userId,
        @Column("role_id") String roleId,
        @Column("status") String status,
        @Column("assigned_by") String assignedBy,
        @Column("assigned_at") Instant assignedAt,
        @Column("created_at") Instant createdAt,
        @Column("updated_at") Instant updatedAt) {}
