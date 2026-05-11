package io.identityaccess.infrastructure.adapter.in.security;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class SecurityPrincipalMapper {

    public IamSecurityPrincipal toPrincipal(
            String userId,
            String sessionId,
            String email,
            Set<String> roleCodes) {
        return new IamSecurityPrincipal(userId, sessionId, email, roleCodes);
    }

    public Collection<GrantedAuthority> toAuthorities(Set<String> roleCodes, Set<String> permissionCodes) {
        LinkedHashSet<String> authorities = new LinkedHashSet<>();

        if (roleCodes != null) {
            roleCodes.stream()
                    .map(String::trim)
                    .filter(role -> !role.isBlank())
                    .map(String::toUpperCase)
                    .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                    .forEach(authorities::add);
        }

        if (permissionCodes != null) {
            permissionCodes.stream()
                    .map(String::trim)
                    .filter(permission -> !permission.isBlank())
                    .forEach(authorities::add);
        }

        return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableSet());
    }
}
