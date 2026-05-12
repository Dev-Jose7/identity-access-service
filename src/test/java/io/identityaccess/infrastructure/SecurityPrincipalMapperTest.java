package io.identityaccess.infrastructure;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.identityaccess.infrastructure.adapter.in.security.SecurityPrincipalMapper;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

class SecurityPrincipalMapperTest {

    @Test
    void shouldBuildAuthoritiesCompatibleWithHasRoleAndHasAuthority() {
        SecurityPrincipalMapper mapper = new SecurityPrincipalMapper();

        Set<String> roleCodes = Set.of("ACCESS_ADMIN", "ROLE_SYSTEM_ADMIN");
        Set<String> permissionCodes = Set.of("iam.account.create", "iam.account.read");

        Set<String> authorities = mapper.toAuthorities(roleCodes, permissionCodes).stream()
                .map(GrantedAuthority::getAuthority)
                .collect(java.util.stream.Collectors.toSet());

        assertTrue(authorities.contains("ROLE_ACCESS_ADMIN"));
        assertTrue(authorities.contains("ROLE_SYSTEM_ADMIN"));
        assertTrue(authorities.contains("iam.account.create"));
        assertTrue(authorities.contains("iam.account.read"));
    }
}
