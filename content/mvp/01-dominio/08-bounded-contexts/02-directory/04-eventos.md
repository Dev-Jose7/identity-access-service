---
title: "Eventos"
linkTitle: "5. Eventos"
weight: 5
url: "/mvp/dominio/contextos-delimitados/directorio/eventos/"
---

## Marco de eventos
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Eventos emitidos/consumidos por `directory`.

## Eventos emitidos
_Responde: que hechos publica este contexto para otros consumidores._

### OrganizationRegistered
_Esta subseccion detalla organizationregistered dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `traceId`, `tenantId`, `organizationId`, `organizationCode`, `status`.
- Semantica:
  - alta de organizacion B2B.
- Consumidores:
  - `order`, `reporting`.

### OrganizationProfileUpdated
_Esta subseccion detalla organizationprofileupdated dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `organizationId`, `status`, `changedFields`.
- Semantica:
  - cambio valido de perfil organizacional no legal.
- Consumidores:
  - `reporting`, `notification`.

### CountryOperationalPolicyConfigured
_Esta subseccion detalla countryoperationalpolicyconfigured dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `organizationId`, `countryCode`, `policyVersion`, `currencyCode`, `weeklyClosingDay`, `status`, `effectiveFrom`, `effectiveTo`.
- Semantica:
  - configuracion versionada de politica operativa por pais vigente para la organizacion.
- Consumidores:
  - `reporting`.

### OrganizationLegalDataUpdated
_Esta subseccion detalla organizationlegaldataupdated dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `organizationId`, `taxIdMasked`, `verificationStatus`.
- Consumidores:
  - `reporting`.

### OrganizationActivated
_Esta subseccion detalla organizationactivated dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `organizationId`, `status`.
- Consumidores:
  - `identity-access`, `order`.

### OrganizationSuspended
_Esta subseccion detalla organizationsuspended dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `organizationId`, `status`, `reason`.
- Consumidores:
  - `identity-access`, `order`, `notification`.

### AddressRegistered
_Esta subseccion detalla addressregistered dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `organizationId`, `addressId`, `addressType`, `status`.
- Semantica:
  - alta de direccion operativa.
- Consumidores:
  - `order`, `reporting`.

### AddressUpdated
_Esta subseccion detalla addressupdated dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `organizationId`, `addressId`, `addressType`, `status`, `changedFields`.
- Consumidores:
  - `order`, `reporting`.

### AddressDeactivated
_Esta subseccion detalla addressdeactivated dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `organizationId`, `addressId`, `reason`.
- Consumidores:
  - `order`, `notification`.

### AddressDefaultChanged
_Esta subseccion detalla addressdefaultchanged dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `organizationId`, `addressType`, `defaultAddressId`, `previousDefaultAddressId`.
- Consumidores:
  - `order`.

### ContactRegistered
_Esta subseccion detalla contactregistered dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `organizationId`, `contactId`, `contactType`, `label`, `status`.
- Semantica:
  - alta de contacto institucional.
- Consumidores:
  - `notification`, `reporting`.

### ContactUpdated
_Esta subseccion detalla contactupdated dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `organizationId`, `contactId`, `status`, `changedFields`.
- Semantica:
  - cambio de contacto institucional existente.
- Consumidores:
  - `notification`, `reporting`.

### ContactDeactivated
_Esta subseccion detalla contactdeactivated dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `organizationId`, `contactId`, `reason`.
- Semantica:
  - contacto institucional deja de estar operativo.
- Consumidores:
  - `notification`.

### PrimaryContactChanged
_Esta subseccion detalla primarycontactchanged dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `organizationId`, `contactType`, `primaryContactId`, `previousPrimaryContactId`.
- Semantica:
  - cambio de contacto institucional primario por tipo.
- Consumidores:
  - `notification`, `reporting`.

### CheckoutAddressValidated
_Esta subseccion detalla checkoutaddressvalidated dentro del contexto del documento._
- Schema logico:
  - `eventId`, `eventType`, `eventVersion`, `occurredAt`, `tenantId`, `organizationId`, `addressId`, `valid`, `reasonCode`, `checkoutCorrelationId`.
- Consumidores:
  - `order`, `reporting`.

Nota sobre perfiles de usuario organizacionales:
- `directory` mantiene perfiles locales vinculados a IAM, pero en el baseline semantico de `MVP` no publica todavia eventos externos dedicados para ese agregado.

## Eventos consumidos
_Responde: que hechos externos consume este contexto y hasta donde puede reaccionar._
- `RoleAssigned` (desde `identity-access`) para sincronizar estado y atributos locales del perfil de usuario organizacional.
- `UserBlocked` (desde `identity-access`) para inactivar el perfil de usuario organizacional vinculado si aplica.
- `solicitar_validacion_checkout` (comando sync desde `order`) para validacion operativa de direccion en checkout.
