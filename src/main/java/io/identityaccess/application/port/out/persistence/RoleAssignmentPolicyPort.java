package io.identityaccess.application.port.out.persistence;

import reactor.core.publisher.Mono;

public interface RoleAssignmentPolicyPort {

    Mono<Boolean> canAssignRole(String actorUserId, String targetRoleCode);
}
