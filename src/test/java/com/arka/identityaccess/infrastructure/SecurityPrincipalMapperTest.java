package com.arka.identityaccess.infrastructure;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.arka.identityaccess.infrastructure.adapter.in.security.SecurityPrincipalMapper;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

class SecurityPrincipalMapperTest {

    @Test
    void shouldBuildAuthoritiesCompatibleWithHasRoleAndHasAuthority() {
        SecurityPrincipalMapper mapper = new SecurityPrincipalMapper();

        Set<String> roleCodes = Set.of("ORG_ADMIN", "ROLE_ORG_OWNER");
        Set<String> permissionCodes = Set.of("iam.user.create", "iam.user.read");

        Set<String> authorities = mapper.toAuthorities(roleCodes, permissionCodes).stream()
                .map(GrantedAuthority::getAuthority)
                .collect(java.util.stream.Collectors.toSet());

        assertTrue(authorities.contains("ROLE_ORG_ADMIN"));
        assertTrue(authorities.contains("ROLE_ORG_OWNER"));
        assertTrue(authorities.contains("iam.user.create"));
        assertTrue(authorities.contains("iam.user.read"));
    }
}
