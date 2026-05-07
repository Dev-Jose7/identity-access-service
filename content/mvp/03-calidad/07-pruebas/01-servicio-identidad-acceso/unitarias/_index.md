---
title: "Unitarias - servicio-identidad-acceso"
linkTitle: "1. Unitarias"
weight: 1
url: "/mvp/calidad/pruebas/servicio-identidad-acceso/unitarias/"
---

## Objetivo
Validar reglas de dominio IAM, invariantes de seguridad y transiciones de estado sin dependencias externas.

## Cobertura objetivo
- `RN-ACC-01`, `RN-ACC-02`.
- `I-ACC-01`, `I-ACC-02`.
- `D-ACC-01`, `D-ACC-02` (a nivel de validacion de modelo).
- Diccionario de errores canonicos IAM.

## Fixtures base sugeridos
- `fixture_iam_user_active_with_password_local.yaml`
- `fixture_iam_user_blocked.yaml`
- `fixture_iam_session_active.yaml`
- `fixture_iam_session_revoked.yaml`
- `fixture_iam_role_assignment_valid.yaml`
- `fixture_iam_role_assignment_cross_tenant_invalid.yaml`

## Matriz detallada de casos unitarios
| ID | Escenario | Given | When | Then | Trazabilidad |
|---|---|---|---|---|---|
| IAM-UT-001 | login permitido para usuario activo | usuario `ACTIVE` + credencial `PASSWORD` activa | validar autenticacion | autenticacion aceptada | FR-009, RN-ACC-01, I-ACC-01 |
| IAM-UT-002 | login rechazado para usuario bloqueado | usuario `BLOCKED` | validar autenticacion | error `usuario_no_habilitado` | FR-009, RN-ACC-01, I-ACC-01 |
| IAM-UT-003 | login rechazado para credenciales invalidas | usuario activo + password invalido | autenticar | error `credenciales_invalidas` + incremento de intento fallido | FR-009, NFR-006 |
| IAM-UT-004 | sesion no puede volver de `REVOKED` a `ACTIVE` | sesion `REVOKED` | intentar reactivar | error `token_expirado_o_revocado` | RN-ACC-01, I-ACC-01 |
| IAM-UT-005 | sesion expirada se considera invalida | sesion con `expiresAt < now` | validar sesion | rechazo semantico | FR-009, I-ACC-01 |
| IAM-UT-006 | refresh rota par de tokens | sesion activa + refresh valido | refrescar sesion | nuevo `accessJti` y `refreshJti` | FR-009, I-ACC-01 |
| IAM-UT-007 | rol invalido se rechaza | comando con `roleCode` no permitido | asignar rol | error `rol_invalido` | FR-009, RN-ACC-02 |
| IAM-UT-008 | asignacion cross-tenant rechazada | usuario tenant A + rol tenant B | asignar rol | error `acceso_cruzado_detectado` | FR-009, NFR-005, I-ACC-02 |
| IAM-UT-009 | revocacion masiva cambia estado de sesiones activas | usuario con N sesiones `ACTIVE` | revocar sesiones | todas pasan a `REVOKED` y se retorna conteo | FR-009, I-ACC-01 |
| IAM-UT-010 | user blocked emite hecho de seguridad | usuario activo | bloquear usuario | estado `BLOCKED` + evento de dominio `UserBlocked` | FR-009, NFR-006 |
| IAM-UT-011 | policy de tenant obliga tenantId en mutaciones | comando mutante sin tenant | validar policy | rechazo por aislamiento | NFR-005, D-CROSS-01 |
| IAM-UT-012 | policy de autorizacion exige permiso explicito | actor sin permiso admin | ejecutar comando admin | error `operacion_no_permitida` | FR-009, RN-ACC-02 |
| IAM-UT-013 | dedupe semantico en comando idempotente | misma clave + mismo payload | reintentar comando | mismo resultado sin doble efecto | NFR-009, NFR-006 |
| IAM-UT-014 | conflicto idempotente por payload distinto | misma clave + payload distinto | reintentar comando | rechazo `conflicto_idempotencia`/equivalente | NFR-009 |

## Escenarios diferidos (fuera del baseline)
- `IAM-UT-015` (MFA para `arka_admin`) queda diferido a hardening posterior y
  no forma parte de la cobertura obligatoria del baseline `MVP`.

## Criterio de exito unitario
- Escenarios `IAM-UT-001..014` en estado `Disenado` o superior, segun corrida y evidencia.
- Ningun caso de rechazo produce mutacion de estado no permitida.
- Cada rechazo verifica error canonico esperado.
