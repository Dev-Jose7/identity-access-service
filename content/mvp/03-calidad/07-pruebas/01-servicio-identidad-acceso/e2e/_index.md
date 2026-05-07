---
title: "E2E - servicio-identidad-acceso"
linkTitle: "3. E2E"
weight: 3
url: "/mvp/calidad/pruebas/servicio-identidad-acceso/e2e/"
---

## Objetivo
Validar journeys IAM criticos de negocio y seguridad, incluyendo rutas de rechazo, comportamiento cross-servicio por eventos y cumplimiento de baseline operativo del MVP.

## Alcance E2E
- journey de sesion completa de usuario B2B.
- journey administrativo IAM sobre usuarios/roles/sesiones.
- controles de tenant/rol y errores canonicos.
- propagacion de eventos IAM hacia consumidores esperados.

## Escenarios E2E priorizados
| ID | Escenario | Flujo | Resultado esperado | Trazabilidad |
|---|---|---|---|---|
| IAM-E2E-001 | jornada completa de sesion | login -> introspect -> refresh -> logout -> introspect | flujo exitoso hasta logout; despues token inactivo | FR-009, RN-ACC-01, I-ACC-01 |
| IAM-E2E-002 | usuario bloqueado no opera | login fallido/operacion protegida | rechazo `usuario_no_habilitado` sin mutacion permitida | FR-009, RN-ACC-01 |
| IAM-E2E-003 | asignacion de rol y efecto en permisos | admin asigna rol -> consulta permisos | permisos efectivos actualizados y trazables | FR-009, RN-ACC-02 |
| IAM-E2E-004 | bloqueo administrativo revoca sesiones | admin block user sobre usuario activo | sesiones del usuario quedan `REVOKED`; siguientes requests fallan | FR-009, NFR-006 |
| IAM-E2E-005 | revocacion masiva por usuario | admin revoke sessions | corte inmediato de sesiones activas + evento `SessionsRevokedByUser` | FR-009 |
| IAM-E2E-006 | intento cross-tenant en endpoint admin | actor tenant A muta usuario tenant B | rechazo `acceso_cruzado_detectado` + auditoria de seguridad | NFR-005, I-ACC-02, D-CROSS-01 |
| IAM-E2E-007 | resiliencia de emision de eventos | login exitoso con falla transitoria de broker | decision de negocio persiste + outbox pendiente + reintento posterior | NFR-007, NFR-006 |
| IAM-E2E-008 | consumo de `OrganizationSuspended` end-to-end | directory suspende tenant -> IAM consume evento | usuarios del tenant quedan bloqueados y se audita bloqueo | FR-009, NFR-005 |
| IAM-E2E-009 | consistencia JWKS con validacion de token en borde | obtener JWKS -> validar JWT emitido | firma verificable por `kid` vigente | NFR-009, postura JWKS oficial |
| IAM-E2E-011 | trazabilidad tecnica completa | ejecutar mutacion admin + emision de evento | cadena `request -> db -> outbox -> evento/log` conserva `traceId/correlationId` | NFR-006, NFR-007 |
| IAM-E2E-012 | compatibilidad backward en contratos v1 | ejecutar regression suite API/eventos v1 | sin cambios breaking visibles a consumidores | NFR-009 |

## Escenarios diferidos (fuera del baseline)
- `IAM-E2E-010` (MFA para `arka_admin`) queda diferido a hardening posterior y
  no integra la certificacion minima obligatoria del baseline `MVP`.

## Criterio de exito E2E
- Escenarios `IAM-E2E-001..009`, `011` y `012` disenados para ejecucion reproducible; el estado final requiere corrida y evidencia.
- En corrida de certificacion, los flujos criticos (`001`, `004`, `006`, `008`) deben ejecutarse sin desviaciones semanticas.
- Errores canonicos de seguridad y aislamiento disenados para verificar ausencia de side effects indebidos.

## Evidencia minima por corrida
- Reporte de ejecucion con IDs de escenario y estado (`Implementado`/`Ejecutado`/`Validado con evidencia`).
- Extractos de auditoria (`auth_audit`) y outbox relacionados por `traceId/correlationId`.
- Registro de eventos publicados/consumidos con validacion de dedupe.
