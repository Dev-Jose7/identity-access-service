---
title: "Servicio Identidad y Acceso"
linkTitle: "1. Identidad y Acceso"
weight: 1
url: "/mvp/calidad/pruebas/servicio-identidad-acceso/"
---

## Objetivo
Asegurar autenticacion, autorizacion y ciclo de sesion con aislamiento tenant, trazabilidad completa y contratos IAM estables para el baseline `MVP`.

## Alcance de calidad del servicio
- Flujos HTTP: `login`, `refresh`, `logout`, `introspect`, `JWKS`, `admin roles`, `admin block`, `admin sessions/revoke`, `admin sessions/list`, `admin permissions`.
- Flujos async: emision de eventos IAM por outbox y consumo idempotente de `OrganizationSuspended`.
- Reglas de seguridad: aislamiento tenant/rol, errores canonicos, auditoria `auth_audit`, dedupe `processed_event`, propagacion de `traceId` y `correlationId`.

## Fuentes de verdad usadas
- Producto: `FR-009`, `NFR-005`, `NFR-006`, `NFR-009`, `NFR-010`.
- Dominio: `RN-ACC-01`, `RN-ACC-02`, `I-ACC-01`, `I-ACC-02`, `D-ACC-01`, `D-ACC-02`, `D-CROSS-01`.
- Arquitectura IAM: contratos API/eventos, seguridad, datos, runtime.

## Datos de entrada comunes
- `tenant` principal: `org-co-001`.
- `tenant` alterno: `org-ec-001`.
- usuarios base:
  - `usr-b2b-active` (`tenant_user`).
  - `usr-b2b-admin` (`tenant_admin`).
  - `usr-arka-admin` (`arka_admin`, sin obligatoriedad de MFA en baseline actual).
  - `usr-b2b-blocked` (`BLOCKED`).
- trazabilidad tecnica obligatoria en mutaciones: `traceId`, `correlationId`.
- claves de idempotencia: `Idempotency-Key` en mutaciones que lo requieren.

## Criterio de exito global
- Carpetas `unitarias`, `integracion` y `e2e` cubren escenarios criticos `P1` y `P2` del servicio.
- Cada escenario referencia al menos un `FR/NFR` y una regla/invariante de dominio aplicable.
- Errores canonicos IAM cubiertos en pruebas:
  - `credenciales_invalidas`
  - `usuario_no_habilitado`
  - `token_expirado_o_revocado`
  - `acceso_cruzado_detectado`
  - `operacion_no_permitida`
- Se valida evidencia tecnica por flujo: `auth_audit`, `outbox_event`, `processed_event`, logs estructurados con `traceId/correlationId`.

## Cobertura minima obligatoria (IAM)
| Bloque | Cobertura minima |
|---|---|
| Dominio local | login, refresh, revocacion, politicas de rol, transiciones de sesion |
| Seguridad | aislamiento tenant, rechazo por rol y trazabilidad de eventos de seguridad |
| Contratos API | endpoints IAM activos sin breaking en `v1` |
| Contratos de eventos | `UserLoggedIn`, `AuthFailed`, `SessionRefreshed`, `SessionRevoked`, `SessionsRevokedByUser`, `RoleAssigned`, `UserBlocked` |
| Datos y trazabilidad | persistencia y consulta consistente en `user_session`, `auth_audit`, `outbox_event`, `processed_event` |

## Escenarios diferidos (fuera del baseline)
- MFA para `arka_admin` se valida en hardening posterior y no integra el
  paquete minimo obligatorio de certificacion `MVP`.
