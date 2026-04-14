package com.arka.identityaccess.domain.access.repository;

import com.arka.identityaccess.domain.access.aggregate.PermissionAggregate;
import com.arka.identityaccess.domain.access.valueobject.PermissionCode;
import com.arka.identityaccess.domain.access.valueobject.PermissionId;
import java.util.Optional;

public interface PermissionRepository {

    PermissionAggregate save(PermissionAggregate permission);

    Optional<PermissionAggregate> findById(PermissionId permissionId);

    Optional<PermissionAggregate> findByCode(PermissionCode permissionCode);

    boolean existsByCode(PermissionCode permissionCode);
}
