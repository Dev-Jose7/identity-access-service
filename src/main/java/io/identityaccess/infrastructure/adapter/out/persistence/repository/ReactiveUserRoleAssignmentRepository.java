package io.identityaccess.infrastructure.adapter.out.persistence.repository;

import io.identityaccess.infrastructure.adapter.out.persistence.entity.UserRoleAssignmentRow;
import java.time.Instant;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveUserRoleAssignmentRepository extends ReactiveCrudRepository<UserRoleAssignmentRow, String> {

    @Query("""
            SELECT r.role_code
            FROM user_role_assignment ura
            JOIN role r ON r.role_id = ura.role_id
            WHERE ura.user_id = :userId
              AND ura.status = 'ACTIVE'
              AND r.status = 'ACTIVE'
            """)
    Flux<String> findActiveRoleCodesByUserId(@Param("userId") String userId);

    @Query("""
            SELECT DISTINCT rp.permission_code
            FROM user_role_assignment ura
            JOIN role r ON r.role_id = ura.role_id
            JOIN role_permission rp ON rp.role_id = ura.role_id
            JOIN permission p ON p.permission_code = rp.permission_code
            WHERE ura.user_id = :userId
              AND ura.status = 'ACTIVE'
              AND r.status = 'ACTIVE'
              AND p.status = 'ACTIVE'
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
                  AND actor_role.status = 'ACTIVE'
                  AND target_role.status = 'ACTIVE'
                  AND target_role.role_code = :targetRoleCode
            )
            """)
    Mono<Boolean> canActorAssignRole(
            @Param("actorUserId") String actorUserId,
            @Param("targetRoleCode") String targetRoleCode);

    @Query("""
            SELECT EXISTS (
                SELECT 1
                FROM user_role_assignment ura
                JOIN role r ON r.role_id = ura.role_id
                WHERE ura.user_id = :userId
                  AND r.role_code = :roleCode
                  AND ura.status = 'ACTIVE'
                  AND r.status = 'ACTIVE'
            )
            """)
    Mono<Boolean> existsActiveRoleAssignment(
            @Param("userId") String userId,
            @Param("roleCode") String roleCode);

    @Query("""
            SELECT EXISTS (
                SELECT 1
                FROM user_role_assignment ura
                JOIN role r ON r.role_id = ura.role_id
                WHERE r.role_code = :roleCode
                  AND ura.status = 'ACTIVE'
                  AND r.status = 'ACTIVE'
            )
            """)
    Mono<Boolean> existsActiveAssignmentForRoleCode(@Param("roleCode") String roleCode);

    @Query("""
            SELECT EXISTS (
                SELECT 1
                FROM user_role_assignment target_ura
                JOIN role exclusive_role ON exclusive_role.role_id = target_ura.role_id
                JOIN user_account target_user ON target_user.user_id = target_ura.user_id
                WHERE target_ura.user_id = :userId
                  AND target_ura.status = 'ACTIVE'
                  AND target_user.status = 'ACTIVE'
                  AND exclusive_role.status = 'ACTIVE'
                  AND exclusive_role.exclusive_assignment = TRUE
                  AND (
                    SELECT COUNT(DISTINCT active_user.user_id)
                    FROM user_role_assignment active_ura
                    JOIN user_account active_user ON active_user.user_id = active_ura.user_id
                    WHERE active_ura.role_id = exclusive_role.role_id
                      AND active_ura.status = 'ACTIVE'
                      AND active_user.status = 'ACTIVE'
                  ) <= 1
            )
            """)
    Mono<Boolean> wouldBlockLastActiveExclusiveRoleHolder(@Param("userId") String userId);

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
