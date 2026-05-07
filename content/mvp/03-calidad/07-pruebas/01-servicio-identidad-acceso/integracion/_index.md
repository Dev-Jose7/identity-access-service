---
title: "Integracion - servicio-identidad-acceso"
linkTitle: "2. Integracion"
weight: 2
url: "/mvp/calidad/pruebas/servicio-identidad-acceso/integracion/"
---

## Objetivo
Validar contratos API/eventos, adapters de persistencia y seguridad, outbox/dedupe, y trazabilidad tecnica de extremo a extremo dentro del servicio IAM.

## Dependencias y entornos
- BD con tablas IAM: `user_account`, `credential*`, `user_session`, `role*`, `auth_audit`, `outbox_event`, `processed_event`.
- broker/event bus para publicacion y consumo de eventos.
- seguridad de borde (contexto autenticado para endpoints admin).

## Datos de entrada
- requests y responses conforme a contratos `v1`.
- headers segun endpoint (`Authorization`, `Idempotency-Key`, `X-Request-Id`).
- eventos entrantes versionados (`directory.organization-suspended.v1`).

## Matriz detallada de casos de integracion
| ID | Escenario | Validacion principal | Evidencia | Trazabilidad |
|---|---|---|---|---|
| IAM-IT-001 | `POST /auth/login` exitoso | contrato request/response + sesion creada | response 200 + fila `user_session` + `auth_audit` + `outbox_event(UserLoggedIn)` | FR-009, NFR-006 |
| IAM-IT-002 | `POST /auth/login` con password invalido | cierre semantico de rechazo | 401 + `auth_audit(login_failed)` + `outbox_event(AuthFailed)` | FR-009, NFR-006 |
| IAM-IT-003 | `POST /auth/refresh` idempotente | misma clave mismo payload | 200 consistente + un solo efecto en `user_session` + un `SessionRefreshed` | FR-009, NFR-009 |
| IAM-IT-004 | `POST /auth/logout` revoca sesion | revocacion efectiva por `sessionId/jti` | 200 + `status=REVOKED` + `outbox_event(SessionRevoked)` | FR-009, I-ACC-01 |
| IAM-IT-005 | `POST /auth/introspect` token activo | introspeccion semantica | `active=true` + permisos efectivos | FR-009 |
| IAM-IT-006 | `POST /auth/introspect` token revocado | rechazo de token invalido | `active=false` o error controlado + auditoria | FR-009, `token_expirado_o_revocado` |
| IAM-IT-007 | `GET /.well-known/jwks.json` | contrato JWKS estable | 200 + `keys[].kid/alg/use` validos | NFR-009, decision JWKS oficial |
| IAM-IT-008 | admin assign role permitido | rol/permiso + tenant validos | 200 + `user_role_assignment` + `outbox_event(RoleAssigned)` | FR-009, RN-ACC-02 |
| IAM-IT-009 | admin assign role cross-tenant | aislamiento tenant | rechazo 403/409 + sin insercion en asignaciones | NFR-005, I-ACC-02, D-CROSS-01 |
| IAM-IT-010 | admin block user | bloqueo + revocacion de sesiones | 200 + `user_account=BLOCKED` + sesiones revocadas + `UserBlocked` | FR-009, NFR-006 |
| IAM-IT-011 | admin revoke sessions | revocacion masiva idempotente | 200 + sesiones `REVOKED` + `SessionsRevokedByUser` | FR-009, NFR-009 |
| IAM-IT-012 | `GET /admin/.../sessions` | consulta de sesiones por estado | response coherente con DB | FR-009 |
| IAM-IT-013 | `GET /admin/.../permissions` | permisos efectivos por rol/asignacion | response coherente con `role_permission` | FR-009 |
| IAM-IT-014 | consumo `OrganizationSuspended` | politica de bloqueo por lote | usuarios del tenant bloqueados + dedupe en `processed_event` | dominio IAM politicas, NFR-005 |
| IAM-IT-015 | dedupe de evento entrante duplicado | mismo `eventId` dos veces | segundo consumo `noop idempotente` sin side effects | NFR-009 |
| IAM-IT-016 | retry y DLQ en evento no recuperable | error no retryable | mensaje a DLQ + evidencia operativa | NFR-007, arquitectura-eventos IAM |
| IAM-IT-017 | propagacion `traceId/correlationId` en mutacion admin | trazabilidad transversal | ids presentes en response, auditoria y outbox | NFR-006, NFR-007 |
| IAM-IT-018 | retencion minima de auditoria y eventos tecnicos | politicas aplicadas por tipo de dato | evidencia de job/consulta de retencion | NFR-010 |

## Criterio de exito integracion
- Escenarios `IAM-IT-001..018` disenados para verificar ausencia de breaking en contratos `v1`.
- Dedupe, idempotencia y outbox disenados para verificarse con evidencia persistida.
- En corrida de certificacion, no deben ocurrir mutaciones cross-tenant.
