package com.arka.identityaccess.domain.access.repository;

import com.arka.identityaccess.domain.access.aggregate.RoleAggregate;
import com.arka.identityaccess.domain.access.valueobject.RoleCode;
import com.arka.identityaccess.domain.access.valueobject.RoleId;
import java.util.Optional;

public interface RoleRepository {

    RoleAggregate save(RoleAggregate role);

    Optional<RoleAggregate> findById(RoleId roleId);

    Optional<RoleAggregate> findByCode(RoleCode roleCode);

    boolean existsByCode(RoleCode roleCode);
}
