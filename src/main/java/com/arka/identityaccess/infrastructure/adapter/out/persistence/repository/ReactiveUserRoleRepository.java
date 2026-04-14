package com.arka.identityaccess.infrastructure.adapter.out.persistence.repository;

import com.arka.identityaccess.infrastructure.adapter.out.persistence.entity.UserRoleRow;
import java.time.Instant;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveUserRoleRepository extends ReactiveCrudRepository<UserRoleRow, String> {

    @Query("""
            SELECT ura.role_id
            FROM user_role_assignment ura
            WHERE ura.user_id = :userId
              AND ura.status = 'ACTIVE'
            """)
    Flux<String> findActiveRoleIdsByUserId(@Param("userId") String userId);

    @Query("""
            SELECT assignment_id, user_id, role_id, status, assigned_by, assigned_at, created_at, updated_at
            FROM user_role_assignment
            WHERE user_id = :userId
              AND status = 'ACTIVE'
            """)
    Flux<UserRoleRow> findActiveAssignmentsByUserId(@Param("userId") String userId);

    @Query("""
            SELECT r.role_code
            FROM user_role_assignment ura
            JOIN role r ON r.role_id = ura.role_id
            WHERE ura.user_id = :userId
              AND ura.status = 'ACTIVE'
            """)
    Flux<String> findActiveRoleCodesByUserId(@Param("userId") String userId);

    @Query("""
            SELECT DISTINCT rp.permission_code
            FROM user_role_assignment ura
            JOIN role_permission rp ON rp.role_id = ura.role_id
            WHERE ura.user_id = :userId
              AND ura.status = 'ACTIVE'
            """)
    Flux<String> findActivePermissionCodesByUserId(@Param("userId") String userId);

    @Query("""
            SELECT EXISTS (
                SELECT 1
                FROM user_role_assignment ura
                JOIN role actor_role ON actor_role.role_id = ura.role_id
                JOIN role_assignment_policy rap ON rap.assigner_role_id = actor_role.role_id
                JOIN role target_role ON target_role.role_id = rap.assignable_role_id
                WHERE ura.user_id = :actorUserId
                  AND ura.status = 'ACTIVE'
                  AND target_role.role_code = :targetRoleCode
            )
            """)
    Mono<Boolean> canAssignRoleByPolicy(
            @Param("actorUserId") String actorUserId,
            @Param("targetRoleCode") String targetRoleCode);

    @Query("""
            SELECT EXISTS (
                SELECT 1
                FROM user_role_assignment ura
                JOIN role r ON r.role_id = ura.role_id
                WHERE ura.user_id = :userId
                  AND ura.role_id = :roleId
                  AND ura.status = 'ACTIVE'
            )
            """)
    Mono<Boolean> existsActiveRoleAssignment(
            @Param("userId") String userId,
            @Param("roleId") String roleId);

    @Modifying
    @Query("""
            INSERT INTO user_role_assignment (
                assignment_id,
                user_id,
                role_id,
                status,
                assigned_by,
                assigned_at,
                created_at,
                updated_at
            ) VALUES (
                :assignmentId,
                :userId,
                :roleId,
                :status,
                :assignedBy,
                :assignedAt,
                :createdAt,
                :updatedAt
            )
            """)
    Mono<Integer> insert(
            @Param("assignmentId") String assignmentId,
            @Param("userId") String userId,
            @Param("roleId") String roleId,
            @Param("status") String status,
            @Param("assignedBy") String assignedBy,
            @Param("assignedAt") Instant assignedAt,
            @Param("createdAt") Instant createdAt,
            @Param("updatedAt") Instant updatedAt);

    @Modifying
    @Query("""
            INSERT INTO user_role_assignment (
                assignment_id,
                user_id,
                role_id,
                status,
                assigned_by,
                assigned_at,
                created_at,
                updated_at
            ) VALUES (
                :assignmentId,
                :userId,
                :roleId,
                :status,
                :assignedBy,
                :assignedAt,
                :createdAt,
                :updatedAt
            )
            ON CONFLICT (user_id, role_id) DO NOTHING
            """)
    Mono<Integer> insertIgnoreIfExists(
            @Param("assignmentId") String assignmentId,
            @Param("userId") String userId,
            @Param("roleId") String roleId,
            @Param("status") String status,
            @Param("assignedBy") String assignedBy,
            @Param("assignedAt") Instant assignedAt,
            @Param("createdAt") Instant createdAt,
            @Param("updatedAt") Instant updatedAt);
}
