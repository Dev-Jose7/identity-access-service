package com.arka.identityaccess.infrastructure.adapter.out.persistence.repository;

import com.arka.identityaccess.infrastructure.adapter.out.persistence.entity.RoleRow;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ReactiveRoleRepository extends ReactiveCrudRepository<RoleRow, String> {

    Mono<RoleRow> findByRoleCode(String roleCode);
}
