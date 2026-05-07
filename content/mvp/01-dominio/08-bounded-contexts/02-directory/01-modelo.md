---
title: "Modelo"
linkTitle: "2. Modelo"
weight: 2
url: "/mvp/dominio/contextos-delimitados/directorio/modelo/"
---

## Marco del modelo
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Modelo conceptual del BC `directory`.

## Entidades principales
_Responde: que entidades estructuran el modelo local._
- Organizacion.
- Perfil legal de organizacion.
- Parametro operativo por pais.
- Direccion.
- Perfil de usuario organizacional.
- Contacto institucional.

## Value objects principales
_Responde: que objetos de valor expresan reglas relevantes sin identidad propia._
- `TenantRef`.
- `TaxIdRef`.
- `IamUserRef`.
- `AddressSnapshotCandidate`.
- `OrganizationContactValue`.
- `OrganizationOperationalPreferences`.
- `AddressValidationSnapshot`.

## Estados importantes
_Responde: que estados son relevantes para entender el ciclo local._
| Entidad | Estados permitidos | Estado inicial | Terminales |
|---|---|---|---|
| Organizacion | `ONBOARDING`, `ACTIVE`, `SUSPENDED`, `INACTIVE` | `ONBOARDING` | `INACTIVE` |
| PerfilLegal | `PENDING`, `VERIFIED`, `REJECTED` | `PENDING` | `VERIFIED`, `REJECTED` |
| ParametroOperativoPais | `ACTIVE`, `INACTIVE`, `SUPERSEDED` | `ACTIVE` | `INACTIVE`, `SUPERSEDED` |
| Direccion | `ACTIVE`, `INACTIVE`, `ARCHIVED` | `ACTIVE` | `ARCHIVED` |
| `Direccion.validationStatus` | `PENDING`, `VERIFIED`, `REJECTED` | `PENDING` | `VERIFIED`, `REJECTED` |
| PerfilUsuarioOrganizacional | `ACTIVE`, `INACTIVE` | `ACTIVE` | `INACTIVE` |
| ContactoInstitucional | `ACTIVE`, `INACTIVE` | `ACTIVE` | `INACTIVE` |

## Reglas de negocio nucleo
_Responde: que reglas de negocio sostienen el modelo del contexto._
- `taxId` es unico por `countryCode` entre organizaciones activas.
- Solo una direccion default por organizacion y tipo (`shipping`, `billing`).
- Solo un contacto institucional primario por organizacion y tipo de contacto.
- Solo un perfil de usuario organizacional por `organizationId + iamUserId`.
- Si IAM bloquea al usuario, el perfil de usuario organizacional no puede permanecer `ACTIVE`.
- Direccion de checkout debe ser activa y del mismo tenant del pedido.

## Identidad de agregados
_Responde: como se identifica cada agregado relevante del contexto._
- `OrganizationAggregate(organizationId, tenantId, status, legalProfile, embeddedOperationalPreferences, countryPolicies)`.
- `AddressAggregate(addressId, organizationId, type, status, isDefault)`.
- `OrganizationUserProfileAggregate(profileId, organizationId, iamUserId, status)`.
- `OrganizationContactAggregate(contactId, organizationId, type, status, isPrimary)`.
