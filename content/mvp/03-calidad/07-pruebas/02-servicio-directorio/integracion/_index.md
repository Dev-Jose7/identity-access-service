---
title: "Integracion - servicio-directorio"
linkTitle: "2. Integracion"
weight: 2
url: "/mvp/calidad/pruebas/servicio-directorio/integracion/"
---

## Objetivo
Validar contratos REST/eventos, persistencia y adapters del servicio, incluyendo idempotencia, dedupe y trazabilidad tecnica.

## Dependencias y entornos
- BD con tablas del modelo Directory (`organization`, `organization_legal_profile`, `organization_user_profile`, `organization_contact`, `address`, `organization_country_policy`, `directory_audit`, `outbox_event`, `processed_event`).
- broker/event bus para publicacion y consumo de eventos.
- contexto de seguridad para usuario y `trusted_service(order-service)`.

## Datos de entrada
- requests/responses de contratos `v1`.
- headers `Authorization`, `Idempotency-Key`, `X-Request-Id` cuando aplique.
- eventos entrantes IAM: `iam.user-role-assigned.v1`, `iam.user-blocked.v1`.

## Matriz detallada de casos de integracion
| ID | Escenario | Validacion principal | Evidencia | Trazabilidad |
|---|---|---|---|---|
| DIR-IT-001 | `POST /organizations` exitoso | contrato + persistencia base | 201 + filas `organization`/`organization_legal_profile` + `directory_audit` + outbox `OrganizationRegistered` | FR-009, NFR-006 |
| DIR-IT-002 | `PATCH /organizations/{id}` | actualizacion de perfil organizacional | 200 + cambios persistidos + outbox `OrganizationProfileUpdated` | FR-009 |
| DIR-IT-003 | `PATCH /organizations/{id}/legal-data` | validacion y masking legal | 200 + `taxIdMasked` + outbox `OrganizationLegalDataUpdated` | FR-009, NFR-010 |
| DIR-IT-004 | `POST /organizations/{id}/activate` | transicion valida de estado | 200 + `status=ACTIVE` + outbox `OrganizationActivated` | FR-011 |
| DIR-IT-005 | `POST /organizations/{id}/suspend` | suspension y propagacion | 200 + `status=SUSPENDED` + outbox `OrganizationSuspended` | FR-009, NFR-006 |
| DIR-IT-006 | `POST /addresses` | alta de direccion | 201 + fila `address` + outbox `AddressRegistered` | FR-004 |
| DIR-IT-007 | `PATCH /addresses/{addressId}` | actualizacion y revalidacion de direccion | 200 + outbox `AddressUpdated` | FR-004 |
| DIR-IT-008 | `POST /addresses/{addressId}/set-default` | default unico por tipo | 200 + una sola direccion default + outbox `AddressDefaultChanged` | D-DIR-01 |
| DIR-IT-009 | `POST /addresses/{addressId}/deactivate` | desactivacion de direccion | 200 + `status=INACTIVE` + outbox `AddressDeactivated` | FR-004 |
| DIR-IT-010 | `POST /contacts` | alta de contacto institucional | 201 + `organization_contact` + outbox `ContactRegistered` | FR-006 |
| DIR-IT-011 | `PATCH /contacts/{contactId}` | actualizacion de contacto institucional | 200 + outbox `ContactUpdated` | FR-006 |
| DIR-IT-012 | `POST /contacts/{contactId}/set-primary` | primario unico por tipo | 200 + unicidad + outbox `PrimaryContactChanged` | FR-006 |
| DIR-IT-013 | `POST /contacts/{contactId}/deactivate` | desactivacion de contacto | 200 + `status=INACTIVE` + outbox `ContactDeactivated` | FR-006 |
| DIR-IT-014 | `POST /checkout-address-validations` valido | endpoint tecnico para order | 200 + outbox `CheckoutAddressValidated(valid=true)` | FR-004 |
| DIR-IT-015 | `POST /checkout-address-validations` invalido | rechazo semantico de direccion | 200/422 con `valid=false` + reasonCode estable + auditoria | FR-004, D-DIR-01 |
| DIR-IT-016 | `PUT /operational-country-settings/{countryCode}` | configuracion regional versionada | 200 + `organization_country_policy` + outbox `CountryOperationalPolicyConfigured` | FR-011, NFR-011 |
| DIR-IT-017 | `GET /operational-country-settings/{countryCode}` sin vigente | consulta tecnica sin recurso vigente | `404 configuracion_pais_no_disponible` + auditoria | FR-011, I-LOC-01, D-DIR-02 |
| DIR-IT-018 | idempotencia en mutacion admin | misma clave + mismo payload | mismo resultado sin doble side effect | NFR-009 |
| DIR-IT-019 | conflicto idempotente | misma clave + payload distinto | rechazo de conflicto, sin segunda mutacion | NFR-009 |
| DIR-IT-020 | consumo `RoleAssigned` | reconciliar profile local | upsert `organization_user_profile` + registro en `processed_event` | FR-009 |
| DIR-IT-021 | consumo `UserBlocked` | inactivar profile local | profile `INACTIVE` + `processed_event` + auditoria | FR-009, NFR-005 |
| DIR-IT-022 | dedupe evento IAM duplicado | mismo `eventId` dos veces | segundo consumo `noop idempotente` | NFR-009 |
| DIR-IT-023 | control tenant/ownership en mutaciones | actor tenant A sobre org tenant B | rechazo 403/409 + sin cambios DB | NFR-005, I-ACC-02, D-CROSS-01 |
| DIR-IT-024 | propagacion `traceId/correlationId` | mutacion + evento de salida | ids presentes en response, audit y outbox | NFR-006 |
| DIR-IT-025 | masking PII en listados/summary | consultas de contactos/datos legales | valores sensibles enmascarados | NFR-010, seguridad Directory |

## Criterio de exito integracion
- Escenarios `DIR-IT-001..025` disenados para verificar ausencia de breaking en contratos `v1`.
- Outbox, dedupe y auditoria disenados para verificarse con evidencia persistida durante la corrida.
- En corrida de certificacion, no deben ocurrir mutaciones cross-tenant.
