---
title: "Vista C4 Nivel 3"
linkTitle: "1. Vista C4 L3"
weight: 1
url: "/mvp/arquitectura/servicios/servicio-directorio/arquitectura-interna/vista-c4-nivel-3/"
---

## Proposito
Definir la vista C4 de componente para `directory-service`, detallando limites internos, dependencias y responsabilidades tecnicas para implementacion reactiva con Spring WebFlux.

## Alcance y fronteras
- Incluye componentes internos de Directory en la vista componente: `Adapter-in`, `Application service`, `Domain`, `Adapter-out`.
- Incluye unicamente clases de implementacion representativas de cada componente para identificacion rapida.
- Excluye de la vista componente: DTOs, mappers, interfaces de puertos, utilidades y clases de configuracion.
- Incluye dependencias con `api-gateway-service`, `identity-access-service`, `config-server`, `eureka-server`, `kafka-cluster`, `redis-cache` y `Directory DB`.
- Excluye decisiones de codigo de otros servicios core.

## Rol del servicio en el sistema
`directory-service` es la autoridad semantica de datos organizacionales B2B:
- administra perfil de organizacion y datos legales minimos (taxId/NIT por pais),
- administra direcciones de facturacion/despacho y contactos institucionales,
- mantiene perfiles de usuario organizacionales locales sincronizados desde IAM para contexto operativo dentro de la organizacion,
- administra parametros operativos por pais versionados por organizacion,
- aplica aislamiento estricto por `organizationId`/`tenantId`,
- expone validacion de direccion para checkout de `order-service`,
- publica eventos de cambios de directorio para sincronizacion en consumidores.

## C4 componente del servicio
La vista de componentes se divide por caso de uso para reducir cruces visuales sin perder detalle estructural del servicio. Cada diagrama conserva los mismos grupos (`Adapter-in`, `Application service`, `Domain`, `Adapter-out`) y muestra solo las relaciones relevantes para ese flujo.

{{< tabs groupid="directory-c4-l3" >}}
{{< tab title="RegisterOrganization" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    ORG_CTRL["OrganizationHttpController"]
  end

  subgraph APP["Application service"]
    REG_ORG_UC["RegisterOrganizationUseCase"]
  end

  subgraph DOM["Domain"]
    ORG_AGG["OrganizationAggregate"]
    LEGAL_SVC["LegalValidationService"]
    DIR_AUTHZ["DirectoryAuthorizationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    ORG_REPO["OrganizationR2dbcRepositoryAdapter"]
    ORG_LEGAL_REPO["OrganizationLegalR2dbcRepositoryAdapter"]
    AUDIT_REPO["DirectoryAuditR2dbcRepositoryAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
    CLAIMS_ADP["PrincipalContextAdapter"]
    PERM_ADP["RbacPermissionEvaluatorAdapter"]
    TAXID_CLIENT["TaxIdValidationHttpClientAdapter"]
    CLOCK_ADP["SystemClockAdapter"]
  end

  ORG_CTRL --> REG_ORG_UC
  REG_ORG_UC --> CLAIMS_ADP
  REG_ORG_UC --> PERM_ADP
  REG_ORG_UC --> TAXID_CLIENT
  REG_ORG_UC --> CLOCK_ADP
  REG_ORG_UC --> DIR_AUTHZ
  REG_ORG_UC --> LEGAL_SVC
  REG_ORG_UC --> ORG_AGG
  REG_ORG_UC --> ORG_REPO
  REG_ORG_UC --> ORG_LEGAL_REPO
  REG_ORG_UC --> AUDIT_REPO
  REG_ORG_UC --> OUTBOX_ADP
  OUTBOX_ADP --> EVENT_PUB

  ORG_REPO --> DIRDB["Directory DB (PostgreSQL)"]
  ORG_LEGAL_REPO --> DIRDB
  AUDIT_REPO --> DIRDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="UpdateOrganizationProfile" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    ORG_CTRL["OrganizationHttpController"]
  end

  subgraph APP["Application service"]
    UPDATE_ORG_UC["UpdateOrganizationProfileUseCase"]
  end

  subgraph DOM["Domain"]
    ORG_AGG["OrganizationAggregate"]
    TENANT_POLICY["TenantIsolationPolicy"]
    DIR_AUTHZ["DirectoryAuthorizationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    ORG_REPO["OrganizationR2dbcRepositoryAdapter"]
    AUDIT_REPO["DirectoryAuditR2dbcRepositoryAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
    CLAIMS_ADP["PrincipalContextAdapter"]
    PERM_ADP["RbacPermissionEvaluatorAdapter"]
    CLOCK_ADP["SystemClockAdapter"]
  end

  ORG_CTRL --> UPDATE_ORG_UC
  UPDATE_ORG_UC --> CLAIMS_ADP
  UPDATE_ORG_UC --> PERM_ADP
  UPDATE_ORG_UC --> CLOCK_ADP
  UPDATE_ORG_UC --> TENANT_POLICY
  UPDATE_ORG_UC --> DIR_AUTHZ
  UPDATE_ORG_UC --> ORG_AGG
  UPDATE_ORG_UC --> ORG_REPO
  UPDATE_ORG_UC --> AUDIT_REPO
  UPDATE_ORG_UC --> OUTBOX_ADP
  OUTBOX_ADP --> EVENT_PUB

  ORG_REPO --> DIRDB["Directory DB (PostgreSQL)"]
  AUDIT_REPO --> DIRDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="UpdateOrganizationLegalData" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    ORG_CTRL["OrganizationHttpController"]
  end

  subgraph APP["Application service"]
    UPDATE_LEGAL_UC["UpdateOrganizationLegalDataUseCase"]
  end

  subgraph DOM["Domain"]
    ORG_AGG["OrganizationAggregate"]
    TENANT_POLICY["TenantIsolationPolicy"]
    DIR_AUTHZ["DirectoryAuthorizationPolicy"]
    LEGAL_SVC["LegalValidationService"]
  end

  subgraph OUT["Adapter-out"]
    ORG_LEGAL_REPO["OrganizationLegalR2dbcRepositoryAdapter"]
    AUDIT_REPO["DirectoryAuditR2dbcRepositoryAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
    CLAIMS_ADP["PrincipalContextAdapter"]
    PERM_ADP["RbacPermissionEvaluatorAdapter"]
    TAXID_CLIENT["TaxIdValidationHttpClientAdapter"]
    CLOCK_ADP["SystemClockAdapter"]
  end

  ORG_CTRL --> UPDATE_LEGAL_UC
  UPDATE_LEGAL_UC --> CLAIMS_ADP
  UPDATE_LEGAL_UC --> PERM_ADP
  UPDATE_LEGAL_UC --> TAXID_CLIENT
  UPDATE_LEGAL_UC --> CLOCK_ADP
  UPDATE_LEGAL_UC --> TENANT_POLICY
  UPDATE_LEGAL_UC --> DIR_AUTHZ
  UPDATE_LEGAL_UC --> LEGAL_SVC
  UPDATE_LEGAL_UC --> ORG_AGG
  UPDATE_LEGAL_UC --> ORG_LEGAL_REPO
  UPDATE_LEGAL_UC --> AUDIT_REPO
  UPDATE_LEGAL_UC --> OUTBOX_ADP
  OUTBOX_ADP --> EVENT_PUB

  ORG_LEGAL_REPO --> DIRDB["Directory DB (PostgreSQL)"]
  AUDIT_REPO --> DIRDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="ActivateOrganization" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    ORG_ADMIN_CTRL["OrganizationAdminHttpController"]
  end

  subgraph APP["Application service"]
    ACTIVATE_ORG_UC["ActivateOrganizationUseCase"]
  end

  subgraph DOM["Domain"]
    ORG_AGG["OrganizationAggregate"]
    ADDR_AGG["AddressAggregate"]
    CONTACT_AGG["OrganizationContactAggregate"]
    TENANT_POLICY["TenantIsolationPolicy"]
    DIR_AUTHZ["DirectoryAuthorizationPolicy"]
    ORG_POLICY["OrganizationActivationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    ORG_REPO["OrganizationR2dbcRepositoryAdapter"]
    ADDR_REPO["AddressR2dbcRepositoryAdapter"]
    CONTACT_REPO["ContactR2dbcRepositoryAdapter"]
    AUDIT_REPO["DirectoryAuditR2dbcRepositoryAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
    CLAIMS_ADP["PrincipalContextAdapter"]
    PERM_ADP["RbacPermissionEvaluatorAdapter"]
    CLOCK_ADP["SystemClockAdapter"]
  end

  ORG_ADMIN_CTRL --> ACTIVATE_ORG_UC
  ACTIVATE_ORG_UC --> CLAIMS_ADP
  ACTIVATE_ORG_UC --> PERM_ADP
  ACTIVATE_ORG_UC --> CLOCK_ADP
  ACTIVATE_ORG_UC --> ORG_REPO
  ACTIVATE_ORG_UC --> ADDR_REPO
  ACTIVATE_ORG_UC --> CONTACT_REPO
  ACTIVATE_ORG_UC --> TENANT_POLICY
  ACTIVATE_ORG_UC --> DIR_AUTHZ
  ACTIVATE_ORG_UC --> ORG_POLICY
  ACTIVATE_ORG_UC --> ORG_AGG
  ACTIVATE_ORG_UC --> ADDR_AGG
  ACTIVATE_ORG_UC --> CONTACT_AGG
  ACTIVATE_ORG_UC --> AUDIT_REPO
  ACTIVATE_ORG_UC --> OUTBOX_ADP
  OUTBOX_ADP --> EVENT_PUB

  ORG_REPO --> DIRDB["Directory DB (PostgreSQL)"]
  ADDR_REPO --> DIRDB
  CONTACT_REPO --> DIRDB
  AUDIT_REPO --> DIRDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="SuspendOrganization" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    ORG_ADMIN_CTRL["OrganizationAdminHttpController"]
  end

  subgraph APP["Application service"]
    SUSPEND_ORG_UC["SuspendOrganizationUseCase"]
  end

  subgraph DOM["Domain"]
    ORG_AGG["OrganizationAggregate"]
    TENANT_POLICY["TenantIsolationPolicy"]
    DIR_AUTHZ["DirectoryAuthorizationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    ORG_REPO["OrganizationR2dbcRepositoryAdapter"]
    AUDIT_REPO["DirectoryAuditR2dbcRepositoryAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
    CLAIMS_ADP["PrincipalContextAdapter"]
    PERM_ADP["RbacPermissionEvaluatorAdapter"]
    CLOCK_ADP["SystemClockAdapter"]
  end

  ORG_ADMIN_CTRL --> SUSPEND_ORG_UC
  SUSPEND_ORG_UC --> CLAIMS_ADP
  SUSPEND_ORG_UC --> PERM_ADP
  SUSPEND_ORG_UC --> CLOCK_ADP
  SUSPEND_ORG_UC --> ORG_REPO
  SUSPEND_ORG_UC --> TENANT_POLICY
  SUSPEND_ORG_UC --> DIR_AUTHZ
  SUSPEND_ORG_UC --> ORG_AGG
  SUSPEND_ORG_UC --> AUDIT_REPO
  SUSPEND_ORG_UC --> OUTBOX_ADP
  OUTBOX_ADP --> EVENT_PUB

  ORG_REPO --> DIRDB["Directory DB (PostgreSQL)"]
  AUDIT_REPO --> DIRDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="RegisterAddress" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    ADDR_CTRL["AddressHttpController"]
  end

  subgraph APP["Application service"]
    REG_ADDR_UC["RegisterAddressUseCase"]
  end

  subgraph DOM["Domain"]
    ADDR_AGG["AddressAggregate"]
    TENANT_POLICY["TenantIsolationPolicy"]
    DIR_AUTHZ["DirectoryAuthorizationPolicy"]
    ADDRESS_POLICY["AddressPolicy"]
  end

  subgraph OUT["Adapter-out"]
    ORG_REPO["OrganizationR2dbcRepositoryAdapter"]
    ADDR_REPO["AddressR2dbcRepositoryAdapter"]
    GEO_CLIENT["GeoValidationHttpClientAdapter"]
    AUDIT_REPO["DirectoryAuditR2dbcRepositoryAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
    CLAIMS_ADP["PrincipalContextAdapter"]
    PERM_ADP["RbacPermissionEvaluatorAdapter"]
    CLOCK_ADP["SystemClockAdapter"]
  end

  ADDR_CTRL --> REG_ADDR_UC
  REG_ADDR_UC --> CLAIMS_ADP
  REG_ADDR_UC --> PERM_ADP
  REG_ADDR_UC --> ORG_REPO
  REG_ADDR_UC --> GEO_CLIENT
  REG_ADDR_UC --> CLOCK_ADP
  REG_ADDR_UC --> TENANT_POLICY
  REG_ADDR_UC --> DIR_AUTHZ
  REG_ADDR_UC --> ADDRESS_POLICY
  REG_ADDR_UC --> ADDR_AGG
  REG_ADDR_UC --> ADDR_REPO
  REG_ADDR_UC --> AUDIT_REPO
  REG_ADDR_UC --> OUTBOX_ADP
  OUTBOX_ADP --> EVENT_PUB

  ORG_REPO --> DIRDB["Directory DB (PostgreSQL)"]
  ADDR_REPO --> DIRDB
  AUDIT_REPO --> DIRDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="UpdateAddress" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    ADDR_CTRL["AddressHttpController"]
  end

  subgraph APP["Application service"]
    UPDATE_ADDR_UC["UpdateAddressUseCase"]
  end

  subgraph DOM["Domain"]
    ADDR_AGG["AddressAggregate"]
    TENANT_POLICY["TenantIsolationPolicy"]
    DIR_AUTHZ["DirectoryAuthorizationPolicy"]
    ADDRESS_POLICY["AddressPolicy"]
  end

  subgraph OUT["Adapter-out"]
    ADDR_REPO["AddressR2dbcRepositoryAdapter"]
    GEO_CLIENT["GeoValidationHttpClientAdapter"]
    AUDIT_REPO["DirectoryAuditR2dbcRepositoryAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
    CLAIMS_ADP["PrincipalContextAdapter"]
    PERM_ADP["RbacPermissionEvaluatorAdapter"]
    CLOCK_ADP["SystemClockAdapter"]
  end

  ADDR_CTRL --> UPDATE_ADDR_UC
  UPDATE_ADDR_UC --> CLAIMS_ADP
  UPDATE_ADDR_UC --> PERM_ADP
  UPDATE_ADDR_UC --> GEO_CLIENT
  UPDATE_ADDR_UC --> CLOCK_ADP
  UPDATE_ADDR_UC --> TENANT_POLICY
  UPDATE_ADDR_UC --> DIR_AUTHZ
  UPDATE_ADDR_UC --> ADDRESS_POLICY
  UPDATE_ADDR_UC --> ADDR_AGG
  UPDATE_ADDR_UC --> ADDR_REPO
  UPDATE_ADDR_UC --> AUDIT_REPO
  UPDATE_ADDR_UC --> OUTBOX_ADP
  OUTBOX_ADP --> EVENT_PUB

  ADDR_REPO --> DIRDB["Directory DB (PostgreSQL)"]
  AUDIT_REPO --> DIRDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="SetDefaultAddress" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    ADDR_CTRL["AddressHttpController"]
  end

  subgraph APP["Application service"]
    SET_DEFAULT_ADDR_UC["SetDefaultAddressUseCase"]
  end

  subgraph DOM["Domain"]
    ADDR_AGG["AddressAggregate"]
    TENANT_POLICY["TenantIsolationPolicy"]
    DIR_AUTHZ["DirectoryAuthorizationPolicy"]
    ADDRESS_POLICY["AddressPolicy"]
  end

  subgraph OUT["Adapter-out"]
    ADDR_REPO["AddressR2dbcRepositoryAdapter"]
    AUDIT_REPO["DirectoryAuditR2dbcRepositoryAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
    CLAIMS_ADP["PrincipalContextAdapter"]
    PERM_ADP["RbacPermissionEvaluatorAdapter"]
    CLOCK_ADP["SystemClockAdapter"]
  end

  ADDR_CTRL --> SET_DEFAULT_ADDR_UC
  SET_DEFAULT_ADDR_UC --> CLAIMS_ADP
  SET_DEFAULT_ADDR_UC --> PERM_ADP
  SET_DEFAULT_ADDR_UC --> CLOCK_ADP
  SET_DEFAULT_ADDR_UC --> TENANT_POLICY
  SET_DEFAULT_ADDR_UC --> DIR_AUTHZ
  SET_DEFAULT_ADDR_UC --> ADDRESS_POLICY
  SET_DEFAULT_ADDR_UC --> ADDR_AGG
  SET_DEFAULT_ADDR_UC --> ADDR_REPO
  SET_DEFAULT_ADDR_UC --> AUDIT_REPO
  SET_DEFAULT_ADDR_UC --> OUTBOX_ADP
  OUTBOX_ADP --> EVENT_PUB

  ADDR_REPO --> DIRDB["Directory DB (PostgreSQL)"]
  AUDIT_REPO --> DIRDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="DeactivateAddress" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    ADDR_CTRL["AddressHttpController"]
  end

  subgraph APP["Application service"]
    DEACTIVATE_ADDR_UC["DeactivateAddressUseCase"]
  end

  subgraph DOM["Domain"]
    ADDR_AGG["AddressAggregate"]
    TENANT_POLICY["TenantIsolationPolicy"]
    DIR_AUTHZ["DirectoryAuthorizationPolicy"]
    ADDRESS_POLICY["AddressPolicy"]
  end

  subgraph OUT["Adapter-out"]
    ADDR_REPO["AddressR2dbcRepositoryAdapter"]
    AUDIT_REPO["DirectoryAuditR2dbcRepositoryAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
    CLAIMS_ADP["PrincipalContextAdapter"]
    PERM_ADP["RbacPermissionEvaluatorAdapter"]
    CLOCK_ADP["SystemClockAdapter"]
  end

  ADDR_CTRL --> DEACTIVATE_ADDR_UC
  DEACTIVATE_ADDR_UC --> CLAIMS_ADP
  DEACTIVATE_ADDR_UC --> PERM_ADP
  DEACTIVATE_ADDR_UC --> CLOCK_ADP
  DEACTIVATE_ADDR_UC --> TENANT_POLICY
  DEACTIVATE_ADDR_UC --> DIR_AUTHZ
  DEACTIVATE_ADDR_UC --> ADDRESS_POLICY
  DEACTIVATE_ADDR_UC --> ADDR_AGG
  DEACTIVATE_ADDR_UC --> ADDR_REPO
  DEACTIVATE_ADDR_UC --> AUDIT_REPO
  DEACTIVATE_ADDR_UC --> OUTBOX_ADP
  OUTBOX_ADP --> EVENT_PUB

  ADDR_REPO --> DIRDB["Directory DB (PostgreSQL)"]
  AUDIT_REPO --> DIRDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="RegisterContact" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    CONTACT_CTRL["ContactHttpController"]
  end

  subgraph APP["Application service"]
    REG_CONTACT_UC["RegisterContactUseCase"]
  end

  subgraph DOM["Domain"]
    CONTACT_AGG["OrganizationContactAggregate"]
    TENANT_POLICY["TenantIsolationPolicy"]
    DIR_AUTHZ["DirectoryAuthorizationPolicy"]
    CONTACT_POLICY["ContactPolicy"]
  end

  subgraph OUT["Adapter-out"]
    CONTACT_REPO["ContactR2dbcRepositoryAdapter"]
    AUDIT_REPO["DirectoryAuditR2dbcRepositoryAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
    CLAIMS_ADP["PrincipalContextAdapter"]
    PERM_ADP["RbacPermissionEvaluatorAdapter"]
    CLOCK_ADP["SystemClockAdapter"]
  end

  CONTACT_CTRL --> REG_CONTACT_UC
  REG_CONTACT_UC --> CLAIMS_ADP
  REG_CONTACT_UC --> PERM_ADP
  REG_CONTACT_UC --> CLOCK_ADP
  REG_CONTACT_UC --> TENANT_POLICY
  REG_CONTACT_UC --> DIR_AUTHZ
  REG_CONTACT_UC --> CONTACT_POLICY
  REG_CONTACT_UC --> CONTACT_AGG
  REG_CONTACT_UC --> CONTACT_REPO
  REG_CONTACT_UC --> AUDIT_REPO
  REG_CONTACT_UC --> OUTBOX_ADP
  OUTBOX_ADP --> EVENT_PUB

  CONTACT_REPO --> DIRDB["Directory DB (PostgreSQL)"]
  AUDIT_REPO --> DIRDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="UpdateContact" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    CONTACT_CTRL["ContactHttpController"]
  end

  subgraph APP["Application service"]
    UPDATE_CONTACT_UC["UpdateContactUseCase"]
  end

  subgraph DOM["Domain"]
    CONTACT_AGG["OrganizationContactAggregate"]
    TENANT_POLICY["TenantIsolationPolicy"]
    DIR_AUTHZ["DirectoryAuthorizationPolicy"]
    CONTACT_POLICY["ContactPolicy"]
  end

  subgraph OUT["Adapter-out"]
    CONTACT_REPO["ContactR2dbcRepositoryAdapter"]
    AUDIT_REPO["DirectoryAuditR2dbcRepositoryAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
    CLAIMS_ADP["PrincipalContextAdapter"]
    PERM_ADP["RbacPermissionEvaluatorAdapter"]
    CLOCK_ADP["SystemClockAdapter"]
  end

  CONTACT_CTRL --> UPDATE_CONTACT_UC
  UPDATE_CONTACT_UC --> CLAIMS_ADP
  UPDATE_CONTACT_UC --> PERM_ADP
  UPDATE_CONTACT_UC --> CLOCK_ADP
  UPDATE_CONTACT_UC --> TENANT_POLICY
  UPDATE_CONTACT_UC --> DIR_AUTHZ
  UPDATE_CONTACT_UC --> CONTACT_POLICY
  UPDATE_CONTACT_UC --> CONTACT_AGG
  UPDATE_CONTACT_UC --> CONTACT_REPO
  UPDATE_CONTACT_UC --> AUDIT_REPO
  UPDATE_CONTACT_UC --> OUTBOX_ADP
  OUTBOX_ADP --> EVENT_PUB

  CONTACT_REPO --> DIRDB["Directory DB (PostgreSQL)"]
  AUDIT_REPO --> DIRDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="SetPrimaryContact" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    CONTACT_CTRL["ContactHttpController"]
  end

  subgraph APP["Application service"]
    SET_PRIMARY_CONTACT_UC["SetPrimaryContactUseCase"]
  end

  subgraph DOM["Domain"]
    CONTACT_AGG["OrganizationContactAggregate"]
    TENANT_POLICY["TenantIsolationPolicy"]
    DIR_AUTHZ["DirectoryAuthorizationPolicy"]
    CONTACT_POLICY["ContactPolicy"]
  end

  subgraph OUT["Adapter-out"]
    CONTACT_REPO["ContactR2dbcRepositoryAdapter"]
    AUDIT_REPO["DirectoryAuditR2dbcRepositoryAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
    CLAIMS_ADP["PrincipalContextAdapter"]
    PERM_ADP["RbacPermissionEvaluatorAdapter"]
    CLOCK_ADP["SystemClockAdapter"]
  end

  CONTACT_CTRL --> SET_PRIMARY_CONTACT_UC
  SET_PRIMARY_CONTACT_UC --> CLAIMS_ADP
  SET_PRIMARY_CONTACT_UC --> PERM_ADP
  SET_PRIMARY_CONTACT_UC --> CLOCK_ADP
  SET_PRIMARY_CONTACT_UC --> TENANT_POLICY
  SET_PRIMARY_CONTACT_UC --> DIR_AUTHZ
  SET_PRIMARY_CONTACT_UC --> CONTACT_POLICY
  SET_PRIMARY_CONTACT_UC --> CONTACT_AGG
  SET_PRIMARY_CONTACT_UC --> CONTACT_REPO
  SET_PRIMARY_CONTACT_UC --> AUDIT_REPO
  SET_PRIMARY_CONTACT_UC --> OUTBOX_ADP
  OUTBOX_ADP --> EVENT_PUB

  CONTACT_REPO --> DIRDB["Directory DB (PostgreSQL)"]
  AUDIT_REPO --> DIRDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="DeactivateContact" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    CONTACT_CTRL["ContactHttpController"]
  end

  subgraph APP["Application service"]
    DEACTIVATE_CONTACT_UC["DeactivateContactUseCase"]
  end

  subgraph DOM["Domain"]
    CONTACT_AGG["OrganizationContactAggregate"]
    TENANT_POLICY["TenantIsolationPolicy"]
    DIR_AUTHZ["DirectoryAuthorizationPolicy"]
    CONTACT_POLICY["ContactPolicy"]
  end

  subgraph OUT["Adapter-out"]
    CONTACT_REPO["ContactR2dbcRepositoryAdapter"]
    AUDIT_REPO["DirectoryAuditR2dbcRepositoryAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
    CLAIMS_ADP["PrincipalContextAdapter"]
    PERM_ADP["RbacPermissionEvaluatorAdapter"]
    CLOCK_ADP["SystemClockAdapter"]
  end

  CONTACT_CTRL --> DEACTIVATE_CONTACT_UC
  DEACTIVATE_CONTACT_UC --> CLAIMS_ADP
  DEACTIVATE_CONTACT_UC --> PERM_ADP
  DEACTIVATE_CONTACT_UC --> CLOCK_ADP
  DEACTIVATE_CONTACT_UC --> TENANT_POLICY
  DEACTIVATE_CONTACT_UC --> DIR_AUTHZ
  DEACTIVATE_CONTACT_UC --> CONTACT_POLICY
  DEACTIVATE_CONTACT_UC --> CONTACT_AGG
  DEACTIVATE_CONTACT_UC --> CONTACT_REPO
  DEACTIVATE_CONTACT_UC --> AUDIT_REPO
  DEACTIVATE_CONTACT_UC --> OUTBOX_ADP
  OUTBOX_ADP --> EVENT_PUB

  CONTACT_REPO --> DIRDB["Directory DB (PostgreSQL)"]
  AUDIT_REPO --> DIRDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="SyncOrganizationUserProfile" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    IAM_ROLE_LISTENER["IamRoleChangedEventListener"]
    IAM_BLOCK_LISTENER["IamUserBlockedEventListener"]
    TRIGGER_CTX["TriggerContextResolver"]
  end

  subgraph APP["Application service"]
    UPSERT_PROFILE_UC["UpsertOrganizationUserProfileUseCase"]
    DEACTIVATE_PROFILE_UC["DeactivateOrganizationUserProfileUseCase"]
  end

  subgraph DOM["Domain"]
    TENANT_POLICY["TenantIsolationPolicy"]
    DIR_AUTHZ["DirectoryAuthorizationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    PROFILE_REPO["OrganizationUserProfilePersistenceAdapter"]
    PROCESSED_REPO["ProcessedEventR2dbcRepositoryAdapter"]
  end

  IAM_ROLE_LISTENER --> TRIGGER_CTX
  IAM_BLOCK_LISTENER --> TRIGGER_CTX
  TRIGGER_CTX --> UPSERT_PROFILE_UC
  TRIGGER_CTX --> DEACTIVATE_PROFILE_UC
  UPSERT_PROFILE_UC --> TENANT_POLICY
  UPSERT_PROFILE_UC --> DIR_AUTHZ
  UPSERT_PROFILE_UC --> PROFILE_REPO
  UPSERT_PROFILE_UC --> PROCESSED_REPO
  DEACTIVATE_PROFILE_UC --> TENANT_POLICY
  DEACTIVATE_PROFILE_UC --> DIR_AUTHZ
  DEACTIVATE_PROFILE_UC --> PROFILE_REPO
  DEACTIVATE_PROFILE_UC --> PROCESSED_REPO

  PROFILE_REPO --> DIRDB["Directory DB (PostgreSQL)"]
  PROCESSED_REPO --> DIRDB
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="ValidateCheckoutAddress" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    VALIDATION_CTRL["DirectoryValidationHttpController"]
  end

  subgraph APP["Application service"]
    VALIDATE_ADDR_UC["ValidateCheckoutAddressUseCase"]
  end

  subgraph DOM["Domain"]
    ORG_AGG["OrganizationAggregate"]
    ADDR_AGG["AddressAggregate"]
    CHECKOUT_POLICY["CheckoutAddressValidationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    ORG_REPO["OrganizationR2dbcRepositoryAdapter"]
    ADDR_REPO["AddressR2dbcRepositoryAdapter"]
    AUDIT_REPO["DirectoryAuditR2dbcRepositoryAdapter"]
    CACHE_ADP["DirectoryCacheRedisAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
  end

  VALIDATION_CTRL --> VALIDATE_ADDR_UC
  VALIDATE_ADDR_UC --> ORG_REPO
  VALIDATE_ADDR_UC --> ADDR_REPO
  VALIDATE_ADDR_UC --> ORG_AGG
  VALIDATE_ADDR_UC --> ADDR_AGG
  VALIDATE_ADDR_UC --> CHECKOUT_POLICY
  VALIDATE_ADDR_UC --> AUDIT_REPO
  VALIDATE_ADDR_UC --> CACHE_ADP
  VALIDATE_ADDR_UC --> OUTBOX_ADP
  OUTBOX_ADP --> EVENT_PUB

  ORG_REPO --> DIRDB["Directory DB (PostgreSQL)"]
  ADDR_REPO --> DIRDB
  AUDIT_REPO --> DIRDB
  CACHE_ADP --> REDIS["redis-cache"]
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="GetOrganizationProfile" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    QUERY_CTRL["DirectoryQueryHttpController"]
  end

  subgraph APP["Application service"]
    GET_PROFILE_UC["GetOrganizationProfileUseCase"]
  end

  subgraph DOM["Domain"]
    ORG_AGG["OrganizationAggregate"]
    TENANT_POLICY["TenantIsolationPolicy"]
    DIR_AUTHZ["DirectoryAuthorizationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    ORG_REPO["OrganizationR2dbcRepositoryAdapter"]
    CLAIMS_ADP["PrincipalContextAdapter"]
    PERM_ADP["RbacPermissionEvaluatorAdapter"]
  end

  QUERY_CTRL --> GET_PROFILE_UC
  GET_PROFILE_UC --> CLAIMS_ADP
  GET_PROFILE_UC --> PERM_ADP
  GET_PROFILE_UC --> TENANT_POLICY
  GET_PROFILE_UC --> DIR_AUTHZ
  GET_PROFILE_UC --> ORG_AGG
  GET_PROFILE_UC --> ORG_REPO

  ORG_REPO --> DIRDB["Directory DB (PostgreSQL)"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="ListOrganizationAddresses" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    QUERY_CTRL["DirectoryQueryHttpController"]
  end

  subgraph APP["Application service"]
    LIST_ADDR_UC["ListOrganizationAddressesUseCase"]
  end

  subgraph DOM["Domain"]
    ADDR_AGG["AddressAggregate"]
    TENANT_POLICY["TenantIsolationPolicy"]
    DIR_AUTHZ["DirectoryAuthorizationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    ADDR_REPO["AddressR2dbcRepositoryAdapter"]
    CLAIMS_ADP["PrincipalContextAdapter"]
    PERM_ADP["RbacPermissionEvaluatorAdapter"]
  end

  QUERY_CTRL --> LIST_ADDR_UC
  LIST_ADDR_UC --> CLAIMS_ADP
  LIST_ADDR_UC --> PERM_ADP
  LIST_ADDR_UC --> TENANT_POLICY
  LIST_ADDR_UC --> DIR_AUTHZ
  LIST_ADDR_UC --> ADDR_AGG
  LIST_ADDR_UC --> ADDR_REPO

  ADDR_REPO --> DIRDB["Directory DB (PostgreSQL)"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="GetOrganizationAddressById" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    QUERY_CTRL["DirectoryQueryHttpController"]
  end

  subgraph APP["Application service"]
    GET_ADDR_UC["GetOrganizationAddressByIdUseCase"]
  end

  subgraph DOM["Domain"]
    ADDR_AGG["AddressAggregate"]
    TENANT_POLICY["TenantIsolationPolicy"]
    DIR_AUTHZ["DirectoryAuthorizationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    ADDR_REPO["AddressR2dbcRepositoryAdapter"]
    CLAIMS_ADP["PrincipalContextAdapter"]
    PERM_ADP["RbacPermissionEvaluatorAdapter"]
  end

  QUERY_CTRL --> GET_ADDR_UC
  GET_ADDR_UC --> CLAIMS_ADP
  GET_ADDR_UC --> PERM_ADP
  GET_ADDR_UC --> TENANT_POLICY
  GET_ADDR_UC --> DIR_AUTHZ
  GET_ADDR_UC --> ADDR_AGG
  GET_ADDR_UC --> ADDR_REPO

  ADDR_REPO --> DIRDB["Directory DB (PostgreSQL)"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="ListOrganizationContacts" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    QUERY_CTRL["DirectoryQueryHttpController"]
  end

  subgraph APP["Application service"]
    LIST_CONTACT_UC["ListOrganizationContactsUseCase"]
  end

  subgraph DOM["Domain"]
    CONTACT_AGG["OrganizationContactAggregate"]
    TENANT_POLICY["TenantIsolationPolicy"]
    DIR_AUTHZ["DirectoryAuthorizationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    CONTACT_REPO["ContactR2dbcRepositoryAdapter"]
    PII_ADP["PiiMaskingAdapter"]
    CLAIMS_ADP["PrincipalContextAdapter"]
    PERM_ADP["RbacPermissionEvaluatorAdapter"]
  end

  QUERY_CTRL --> LIST_CONTACT_UC
  LIST_CONTACT_UC --> CLAIMS_ADP
  LIST_CONTACT_UC --> PERM_ADP
  LIST_CONTACT_UC --> TENANT_POLICY
  LIST_CONTACT_UC --> DIR_AUTHZ
  LIST_CONTACT_UC --> CONTACT_AGG
  LIST_CONTACT_UC --> CONTACT_REPO
  LIST_CONTACT_UC --> PII_ADP

  CONTACT_REPO --> DIRDB["Directory DB (PostgreSQL)"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="GetOrganizationContactById" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    QUERY_CTRL["DirectoryQueryHttpController"]
  end

  subgraph APP["Application service"]
    GET_CONTACT_UC["GetOrganizationContactByIdUseCase"]
  end

  subgraph DOM["Domain"]
    CONTACT_AGG["OrganizationContactAggregate"]
    TENANT_POLICY["TenantIsolationPolicy"]
    DIR_AUTHZ["DirectoryAuthorizationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    CONTACT_REPO["ContactR2dbcRepositoryAdapter"]
    PII_ADP["PiiMaskingAdapter"]
    CLAIMS_ADP["PrincipalContextAdapter"]
    PERM_ADP["RbacPermissionEvaluatorAdapter"]
  end

  QUERY_CTRL --> GET_CONTACT_UC
  GET_CONTACT_UC --> CLAIMS_ADP
  GET_CONTACT_UC --> PERM_ADP
  GET_CONTACT_UC --> TENANT_POLICY
  GET_CONTACT_UC --> DIR_AUTHZ
  GET_CONTACT_UC --> CONTACT_AGG
  GET_CONTACT_UC --> CONTACT_REPO
  GET_CONTACT_UC --> PII_ADP

  CONTACT_REPO --> DIRDB["Directory DB (PostgreSQL)"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="GetDirectorySummary" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    QUERY_CTRL["DirectoryQueryHttpController"]
  end

  subgraph APP["Application service"]
    SUMMARY_UC["GetDirectorySummaryUseCase"]
  end

  subgraph DOM["Domain"]
    ORG_AGG["OrganizationAggregate"]
    ADDR_AGG["AddressAggregate"]
    CONTACT_AGG["OrganizationContactAggregate"]
    TENANT_POLICY["TenantIsolationPolicy"]
    DIR_AUTHZ["DirectoryAuthorizationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    ORG_REPO["OrganizationR2dbcRepositoryAdapter"]
    ADDR_REPO["AddressR2dbcRepositoryAdapter"]
    CONTACT_REPO["ContactR2dbcRepositoryAdapter"]
    CACHE_ADP["DirectoryCacheRedisAdapter"]
    PII_ADP["PiiMaskingAdapter"]
    CLAIMS_ADP["PrincipalContextAdapter"]
    PERM_ADP["RbacPermissionEvaluatorAdapter"]
  end

  QUERY_CTRL --> SUMMARY_UC
  SUMMARY_UC --> CLAIMS_ADP
  SUMMARY_UC --> PERM_ADP
  SUMMARY_UC --> TENANT_POLICY
  SUMMARY_UC --> DIR_AUTHZ
  SUMMARY_UC --> ORG_AGG
  SUMMARY_UC --> ADDR_AGG
  SUMMARY_UC --> CONTACT_AGG
  SUMMARY_UC --> ORG_REPO
  SUMMARY_UC --> ADDR_REPO
  SUMMARY_UC --> CONTACT_REPO
  SUMMARY_UC --> PII_ADP
  SUMMARY_UC --> CACHE_ADP

  ORG_REPO --> DIRDB["Directory DB (PostgreSQL)"]
  ADDR_REPO --> DIRDB
  CONTACT_REPO --> DIRDB
  CACHE_ADP --> REDIS["redis-cache"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="ConfigureCountryOperationalPolicy" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    COUNTRY_POLICY_CTRL["DirectoryOperationalPolicyHttpController"]
  end

  subgraph APP["Application service"]
    CONFIGURE_COUNTRY_POLICY_UC["ConfigureCountryOperationalPolicyUseCase"]
  end

  subgraph DOM["Domain"]
    COUNTRY_POLICY_AGG["CountryOperationalPolicyAggregate"]
    COUNTRY_POLICY_RESOLUTION["CountryOperationalPolicyResolutionPolicy"]
    TENANT_POLICY["TenantIsolationPolicy"]
    DIR_AUTHZ["DirectoryAuthorizationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    ORG_REPO["OrganizationR2dbcRepositoryAdapter"]
    COUNTRY_POLICY_REPO["CountryOperationalPolicyR2dbcRepositoryAdapter"]
    AUDIT_REPO["DirectoryAuditR2dbcRepositoryAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
    CLAIMS_ADP["PrincipalContextAdapter"]
    PERM_ADP["RbacPermissionEvaluatorAdapter"]
    CLOCK_ADP["SystemClockAdapter"]
  end

  COUNTRY_POLICY_CTRL --> CONFIGURE_COUNTRY_POLICY_UC
  CONFIGURE_COUNTRY_POLICY_UC --> CLAIMS_ADP
  CONFIGURE_COUNTRY_POLICY_UC --> PERM_ADP
  CONFIGURE_COUNTRY_POLICY_UC --> ORG_REPO
  CONFIGURE_COUNTRY_POLICY_UC --> CLOCK_ADP
  CONFIGURE_COUNTRY_POLICY_UC --> TENANT_POLICY
  CONFIGURE_COUNTRY_POLICY_UC --> DIR_AUTHZ
  CONFIGURE_COUNTRY_POLICY_UC --> COUNTRY_POLICY_RESOLUTION
  CONFIGURE_COUNTRY_POLICY_UC --> COUNTRY_POLICY_AGG
  CONFIGURE_COUNTRY_POLICY_UC --> COUNTRY_POLICY_REPO
  CONFIGURE_COUNTRY_POLICY_UC --> AUDIT_REPO
  CONFIGURE_COUNTRY_POLICY_UC --> OUTBOX_ADP
  OUTBOX_ADP --> EVENT_PUB

  ORG_REPO --> DIRDB["Directory DB (PostgreSQL)"]
  COUNTRY_POLICY_REPO --> DIRDB
  AUDIT_REPO --> DIRDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="ResolveCountryOperationalPolicy" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    COUNTRY_POLICY_CTRL["DirectoryOperationalPolicyHttpController"]
  end

  subgraph APP["Application service"]
    RESOLVE_COUNTRY_POLICY_UC["ResolveCountryOperationalPolicyUseCase"]
  end

  subgraph DOM["Domain"]
    COUNTRY_POLICY_AGG["CountryOperationalPolicyAggregate"]
    COUNTRY_POLICY_RESOLUTION["CountryOperationalPolicyResolutionPolicy"]
    TENANT_POLICY["TenantIsolationPolicy"]
    DIR_AUTHZ["DirectoryAuthorizationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    COUNTRY_POLICY_REPO["CountryOperationalPolicyR2dbcRepositoryAdapter"]
    CACHE_ADP["DirectoryCacheRedisAdapter"]
    CLAIMS_ADP["PrincipalContextAdapter"]
    PERM_ADP["RbacPermissionEvaluatorAdapter"]
    CLOCK_ADP["SystemClockAdapter"]
  end

  COUNTRY_POLICY_CTRL --> RESOLVE_COUNTRY_POLICY_UC
  RESOLVE_COUNTRY_POLICY_UC --> CLAIMS_ADP
  RESOLVE_COUNTRY_POLICY_UC --> PERM_ADP
  RESOLVE_COUNTRY_POLICY_UC --> CLOCK_ADP
  RESOLVE_COUNTRY_POLICY_UC --> TENANT_POLICY
  RESOLVE_COUNTRY_POLICY_UC --> DIR_AUTHZ
  RESOLVE_COUNTRY_POLICY_UC --> COUNTRY_POLICY_RESOLUTION
  RESOLVE_COUNTRY_POLICY_UC --> COUNTRY_POLICY_AGG
  RESOLVE_COUNTRY_POLICY_UC --> COUNTRY_POLICY_REPO
  RESOLVE_COUNTRY_POLICY_UC --> CACHE_ADP

  COUNTRY_POLICY_REPO --> DIRDB["Directory DB (PostgreSQL)"]
  CACHE_ADP --> REDIS["redis-cache"]
{{< /mermaid >}}
{{< /tab >}}
{{< /tabs >}}

## Componentes base por capa (vista componente)
| Capa | Clases base | Responsabilidad tecnica |
|---|---|---|
| `Adapter-in` | `OrganizationHttpController`, `OrganizationAdminHttpController`, `AddressHttpController`, `ContactHttpController`, `DirectoryQueryHttpController`, `DirectoryValidationHttpController`, `DirectoryOperationalPolicyHttpController`, `IamRoleChangedEventListener`, `IamUserBlockedEventListener`, `TriggerContextResolver` | Recibir HTTP/eventos, validar entrada y traducir a casos de uso |
| `Application service` | `RegisterOrganizationUseCase`, `UpdateOrganizationProfileUseCase`, `UpdateOrganizationLegalDataUseCase`, `ActivateOrganizationUseCase`, `SuspendOrganizationUseCase`, `RegisterAddressUseCase`, `UpdateAddressUseCase`, `SetDefaultAddressUseCase`, `DeactivateAddressUseCase`, `RegisterContactUseCase`, `UpdateContactUseCase`, `SetPrimaryContactUseCase`, `DeactivateContactUseCase`, `UpsertOrganizationUserProfileUseCase`, `DeactivateOrganizationUserProfileUseCase`, `ValidateCheckoutAddressUseCase`, `GetOrganizationProfileUseCase`, `ListOrganizationAddressesUseCase`, `GetOrganizationAddressByIdUseCase`, `ListOrganizationContactsUseCase`, `GetOrganizationContactByIdUseCase`, `GetDirectorySummaryUseCase`, `ConfigureCountryOperationalPolicyUseCase`, `ResolveCountryOperationalPolicyUseCase` | Orquestar casos de uso, idempotencia, errores y consistencia semantica de directorio |
| `Domain` | `OrganizationAggregate`, `AddressAggregate`, `OrganizationContactAggregate`, `CountryOperationalPolicyAggregate`, `OrganizationActivationPolicy`, `AddressPolicy`, `ContactPolicy`, `TenantIsolationPolicy`, `CheckoutAddressValidationPolicy`, `CountryOperationalPolicyResolutionPolicy`, `LegalValidationService` | Mantener invariantes del directorio organizacional, reglas de checkout y vigencia regional por pais |
| `Adapter-out` | `OrganizationR2dbcRepositoryAdapter`, `OrganizationLegalR2dbcRepositoryAdapter`, `AddressR2dbcRepositoryAdapter`, `ContactR2dbcRepositoryAdapter`, `OrganizationUserProfilePersistenceAdapter`, `CountryOperationalPolicyR2dbcRepositoryAdapter`, `ProcessedEventR2dbcRepositoryAdapter`, `DirectoryAuditR2dbcRepositoryAdapter`, `OutboxPersistenceAdapter`, `KafkaDomainEventPublisherAdapter`, `DirectoryCacheRedisAdapter`, `PrincipalContextAdapter`, `RbacPermissionEvaluatorAdapter`, `PiiMaskingAdapter`, `TaxIdValidationHttpClientAdapter`, `GeoValidationHttpClientAdapter`, `SystemClockAdapter` | Conectar con DB, cache, broker, autorizacion, masking y validadores externos |

## Nota de modelado
- Esta vista componente no detalla estructura de carpetas.
- Esta vista componente lista implementaciones y owners representativos de `Adapter-in`, `Application service`, `Domain` y `Adapter-out`.
- DTOs/mappers/interfaces/config se detallan en la vista de codigo.
- `contact` representa contacto institucional del tenant, no una persona operativa.
- `RoleAssigned` y `UserBlocked` no entran por el slice HTTP de contacto; alimentan un slice interno de sincronizacion de `organization_user_profile` no expuesto por REST en `MVP`.
- El detalle de paquetes/codigo se mantiene en:
  - [02-Vista-de-Codigo.md](/Users/jose/Development/Documentation/arkab2b-docs/content/mvp/02-arquitectura/services/directory-service/architecture/02-Vista-de-Codigo.md)

## Dependencias externas permitidas
| Dependencia | Tipo | Uso en Directory | Criticidad |
|---|---|---|---|
| `api-gateway-service` | plataforma | Entrada principal de trafico de backoffice/web B2B | alta |
| `identity-access-service` | core | Autenticacion y claims de tenant/rol | critica |
| `Directory DB (PostgreSQL)` | datos | Fuente de verdad de organizacion, direccion, contacto institucional y perfil de usuario organizacional local | critica |
| `redis-cache` | soporte | Cache de validaciones y lecturas frecuentes | media |
| `kafka-cluster` | soporte | Eventos de cambios de directorio para consumidores | media |
| `tax-id-validation provider` | externo | Validacion tributaria de taxId/NIT por pais | media |
| `geo-validation provider` | externo | Normalizacion de direccion/geo basica | baja-media |
| `config-server` | plataforma | Configuracion centralizada y feature toggles | alta |
| `eureka-server` | plataforma | Service discovery | media |

## Modelo de autenticacion y autorizacion runtime
| Flujo | Autenticacion | Autorizacion y legitimidad |
|---|---|---|
| HTTP command/query | `api-gateway-service` autentica el JWT y entrega un request confiable al servicio. | `directory-service` materializa el `PrincipalContext` canonico con su slice reactivo de claims y tenant, valida permiso base con `PermissionEvaluatorPort` y cierra tenant/accion con `TenantIsolationPolicy` y `DirectoryAuthorizationPolicy`. |
| listeners de directorio | No depende de JWT de usuario. | `IamRoleChangedEventListener` e `IamUserBlockedEventListener` materializan `TriggerContext` mediante `TriggerContextResolver`, validan `tenant`, dedupe y la politica aplicable antes de sincronizar perfiles de usuario organizacionales locales. |


## Modelo de errores y excepciones runtime
| Responsabilidad | Componentes | Aplicacion |
|---|---|---|
| Decision semantica | `Application service`, `Domain service`, `TenantIsolationPolicy`, `DirectoryAuthorizationPolicy`, `OrganizationActivationPolicy` | Los casos de Directory expresan rechazo temprano y rechazo de decision mediante familias canonicas de acceso/contexto (`ApplicationException`, `AuthorizationDeniedException`, `TenantIsolationException`, `ResourceNotFoundException`) y de decision (`DomainException`, `DomainRuleViolationException`, `ConflictException`) sin filtrar errores tecnicos al cliente o trigger. |
| Cierre HTTP | `OrganizationHttpController`, `OrganizationAdminHttpController`, `AddressHttpController`, `ContactHttpController`, `DirectoryQueryHttpController`, `DirectoryValidationHttpController`, `WebFluxRouterConfig`, `WebExceptionHandlerConfig` | El adapter-in HTTP traduce la familia semantica o tecnica a un envelope canonico con `errorCode`, `category`, `traceId`, `correlationId` y `timestamp`. |
| Cierre async | `IamRoleChangedEventListener`, `IamUserBlockedEventListener`, `ProcessedEventR2dbcRepositoryAdapter` | Los flujos event-driven tratan duplicados como `noop idempotente`, distinguen fallos retryable/no-retryable y cierran la incidencia por reintento, DLQ o auditoria operativa. |

## Soporte de observabilidad
| Elemento | Componentes principales | Funcion arquitectonica |
|---|---|---|
| Configuracion de metricas y trazas | `ObservabilityConfig` | Expone la configuracion base para `meterRegistry` y `tracingBridge`, dejando preparada la instrumentacion transversal del servicio. |
| Propagacion de correlacion | `CorrelationIdProviderAdapter` | Propaga el `correlationId` entre llamadas salientes, validaciones externas, mutaciones de directorio y publicacion asincrona desde outbox. |
| Auditoria de cambios de directorio | `DirectoryAuditPort`, `DirectoryAuditR2dbcRepositoryAdapter`, `ReactiveDirectoryAuditRepository` | Registran evidencia tecnica y operativa de organizaciones, direcciones, contactos y politicas regionales. |
| Emision de eventos observables | `OutboxPersistenceAdapter`, `OutboxEventRelayPublisher`, `DomainEventKafkaMapper`, `KafkaDomainEventPublisherAdapter` | Materializan y publican eventos de directorio hacia Kafka para integracion y trazabilidad near-real-time. |

Nota:
- Esta vista solo documenta los componentes que habilitan observabilidad dentro de la arquitectura.
- La definicion detallada de metricas, logs, trazas, alertas y dashboards corresponde al pilar de calidad u operacion.

## Canales de eventos (naming canonico)
Convencion aplicada: `<bc>.<event-name>.v<major>`.

| Tipo | Evento | Topic canonico |
|---|---|---|
| Emitido | `OrganizationRegistered` | `directory.organization-registered.v1` |
| Emitido | `OrganizationProfileUpdated` | `directory.organization-profile-updated.v1` |
| Emitido | `OrganizationLegalDataUpdated` | `directory.organization-legal-updated.v1` |
| Emitido | `OrganizationActivated` | `directory.organization-activated.v1` |
| Emitido | `OrganizationSuspended` | `directory.organization-suspended.v1` |
| Emitido | `AddressRegistered` | `directory.address-registered.v1` |
| Emitido | `AddressUpdated` | `directory.address-updated.v1` |
| Emitido | `AddressDeactivated` | `directory.address-deactivated.v1` |
| Emitido | `AddressDefaultChanged` | `directory.address-default-changed.v1` |
| Emitido | `ContactRegistered` | `directory.contact-registered.v1` |
| Emitido | `ContactUpdated` | `directory.contact-updated.v1` |
| Emitido | `ContactDeactivated` | `directory.contact-deactivated.v1` |
| Emitido | `PrimaryContactChanged` | `directory.primary-contact-changed.v1` |
| Emitido | `CheckoutAddressValidated` | `directory.checkout-address-validated.v1` |
| Emitido | `CountryOperationalPolicyConfigured` | `directory.country-operational-policy-configured.v1` |
| Consumido | `RoleAssigned` | `iam.user-role-assigned.v1` |
| Consumido | `UserBlocked` | `iam.user-blocked.v1` |

## Restricciones de diseno
- `MUST`: toda operacion de lectura/escritura valida `organizationId` del token y del recurso.
- `MUST`: `directory-service` no autentica ni emite sesiones; depende de `identity-access-service`.
- `MUST`: taxId/NIT se valida por reglas de pais y unicidad por organizacion activa.
- `MUST`: direccion desactivada o de otro tenant nunca es valida para checkout.
- `MUST`: `RoleAssigned` y `UserBlocked` solo sincronizan `organization_user_profile` local; nunca reescriben credenciales, sesiones ni permisos efectivos de IAM.
- `MUST`: resolucion de parametros operativos por pais es obligatoria para operaciones mutantes/reportes sin fallback global implicito.
- `MUST`: cambios mutantes (`register`, `update`, `deactivate`, `set-default`, `set-primary`) usan `Idempotency-Key`.
- `SHOULD`: consultas de directorio para checkout usan cache con invalidacion por evento.

## Riesgos y trade-offs
- Riesgo: over-validation en registro de direccion aumenta latencia de alta.
  - Mitigacion: validacion minima sincrona + enriquecimiento asincrono posterior.
- Riesgo: dependencia externa de validacion tributaria degrada disponibilidad.
  - Mitigacion: estrategia fail-safe configurable por pais/segmento con flags.
- Riesgo: politica regional ausente para un `countryCode` bloquea checkout y reportes.
  - Mitigacion: endpoint dedicado de configuracion/resolucion con versionado y alerta operacional.
- Trade-off: mantener direccion historica en `order-service` como snapshot evita mutaciones retroactivas, a costo de duplicacion controlada.
