package io.identityaccess.infrastructure.adapter.out.persistence.repository;

import io.identityaccess.infrastructure.adapter.out.persistence.entity.RoleRow;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ReactiveRoleRepository extends ReactiveCrudRepository<RoleRow, String> {

    Mono<RoleRow> findByRoleCode(String roleCode);

    @Query("""
            SELECT role_id, role_code, description, created_at, updated_at
            FROM role
            WHERE role_code = :roleCode
              AND status = 'ACTIVE'
            """)
    Mono<RoleRow> findActiveByRoleCode(@Param("roleCode") String roleCode);
}
