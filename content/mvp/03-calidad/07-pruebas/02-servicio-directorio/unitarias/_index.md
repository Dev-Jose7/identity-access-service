---
title: "Unitarias - servicio-directorio"
linkTitle: "1. Unitarias"
weight: 1
url: "/mvp/calidad/pruebas/servicio-directorio/unitarias/"
---

## Objetivo
Validar invariantes y politicas de dominio del directorio sin dependencias externas.

## Cobertura objetivo
- `RN-LOC-01`, `I-LOC-01`.
- `D-DIR-01`, `D-DIR-02`.
- `I-ACC-02`, `D-CROSS-01` en validacion contextual.
- Reglas de unicidad funcional (`taxId`, default address, primary contact, profile por usuario IAM).

## Fixtures base sugeridos
- `fixture_directory_organization_onboarding.yaml`
- `fixture_directory_organization_active_with_country_policy.yaml`
- `fixture_directory_address_shipping_default.yaml`
- `fixture_directory_contact_email_primary.yaml`
- `fixture_directory_country_policy_active_co.yaml`
- `fixture_directory_org_user_profile_active.yaml`

## Matriz detallada de casos unitarios
| ID | Escenario | Given | When | Then | Trazabilidad |
|---|---|---|---|---|---|
| DIR-UT-001 | crear organizacion con taxId valido | datos legales validos por pais | registrar organizacion | agregado creado en `ONBOARDING` | FR-009, FR-011 |
| DIR-UT-002 | rechazar taxId duplicado activo | organizacion activa con mismo `taxId` y `countryCode` | registrar organizacion | error `tax_id_duplicado` | FR-009, NFR-006 |
| DIR-UT-003 | activar organizacion con prerrequisitos completos | perfil legal verificado + politica pais vigente | activar organizacion | estado `ACTIVE` | FR-011, D-DIR-02 |
| DIR-UT-004 | bloquear activacion sin politica por pais | organizacion sin politica vigente | activar/operar flujo critico | error `configuracion_pais_no_disponible` | FR-011, NFR-011, I-LOC-01 |
| DIR-UT-005 | una sola direccion default por tipo | dos direcciones `SHIPPING` activas | set default segunda | primera deja `isDefault=true`, segunda toma default unica | D-DIR-01 |
| DIR-UT-006 | direccion checkout debe pertenecer a organizacion | address de otra organizacion | validar checkout address | error `direccion_no_pertenece_a_organizacion` | FR-004, D-DIR-01, I-ACC-02 |
| DIR-UT-007 | contacto primario unico por tipo | dos contactos `EMAIL` activos | set primary segundo | unicidad preservada | FR-006 |
| DIR-UT-008 | evitar doble primario inconsistente | estado con dos primarios mismo tipo | validar invariante | error `contacto_primario_inconsistente` | FR-006, NFR-006 |
| DIR-UT-009 | inactivar perfil organizacional por `UserBlocked` | `organization_user_profile` activo | aplicar politica de bloqueo IAM | estado `INACTIVE` | FR-009, politicas Directory |
| DIR-UT-010 | actualizar perfil por `RoleAssigned` sin duplicar identidad | perfil existente + evento IAM valido | aplicar sincronizacion | perfil reconciliado, sin crear credenciales/sesiones | FR-009 |
| DIR-UT-011 | no permitir mutacion sin tenant | comando mutante sin `tenantId` | validar policy | rechazo por aislamiento | NFR-005, D-CROSS-01 |
| DIR-UT-012 | resolver politica pais vigente por fecha | multiples versiones de politica | resolver por `countryCode` y fecha | retorna version `ACTIVE` vigente | FR-011, RN-LOC-01 |
| DIR-UT-013 | deactivar direccion usada como default | direccion default activa | desactivar direccion | default se recalcula o queda sin default segun regla | FR-004 |
| DIR-UT-014 | masking de contacto institucional sensible | contacto `EMAIL` y `PHONE` | proyectar para lista/resumen | `contactValue` enmascarado | NFR-010, seguridad Directory |
| DIR-UT-015 | acceso cruzado en recurso organizacional | actor tenant A sobre org tenant B | evaluar ownership/tenant | error `acceso_cruzado_detectado` | FR-009, NFR-005, I-ACC-02 |

## Criterio de exito unitario
- Escenarios `DIR-UT-001..015` en estado `Disenado` o superior, segun corrida y evidencia.
- Ninguna regla de unicidad/invariante critica queda sin validacion.
- Todos los rechazos verifican error canonico esperado.
