package io.identityaccess.infrastructure.adapter.out.persistence;

import io.identityaccess.application.port.out.persistence.RoleAssignmentPolicyPort;
import io.identityaccess.infrastructure.adapter.out.persistence.repository.ReactiveUserRoleAssignmentRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class RoleAssignmentPolicyR2dbcAdapter implements RoleAssignmentPolicyPort {

    private final ReactiveUserRoleAssignmentRepository userRoleAssignmentRepository;

    public RoleAssignmentPolicyR2dbcAdapter(ReactiveUserRoleAssignmentRepository userRoleAssignmentRepository) {
        this.userRoleAssignmentRepository = userRoleAssignmentRepository;
    }

    @Override
    public Mono<Boolean> canAssignRole(String actorUserId, String targetRoleCode) {
        return userRoleAssignmentRepository.canActorAssignRole(actorUserId, targetRoleCode);
    }
}
