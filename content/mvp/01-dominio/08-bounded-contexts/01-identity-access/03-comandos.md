---
title: "Comandos"
linkTitle: "4. Comandos"
weight: 4
url: "/mvp/dominio/contextos-delimitados/identidad-acceso/comandos/"
---

## Marco de comandos
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Catalogo de comandos de `identity-access`.

## Lista de comandos
_Responde: que comandos admite el contexto y con que efecto semantico._

### autenticar_usuario
_Esta subseccion detalla autenticar_usuario dentro del contexto del documento._
- Input esperado:
  - `tenantId`, `email`, `password`, `requestId`, `clientIp`, `userAgent`.
- Precondiciones:
  - usuario existente y habilitado.
  - credencial local primaria activa.
  - tenant operable en IAM.
- Postcondiciones:
  - sesion activa creada.
  - access token y refresh token emitidos.
  - evidencia tecnica y evento de login exitoso o fallido registrados.
- Idempotencia:
  - `requestId` opcional para dedupe de intento.
- Errores:
  - `credenciales_invalidas`, `usuario_no_habilitado`.

### refrescar_sesion
_Esta subseccion detalla refrescar_sesion dentro del contexto del documento._
- Input esperado:
  - `refreshToken`, `tenantId`, `idempotencyKey`.
- Precondiciones:
  - refresh token valido y sesion activa.
  - usuario sigue habilitado.
- Postcondiciones:
  - nuevo par de tokens emitido.
  - sesion actualizada con rotacion de JTIs.
- Idempotencia:
  - obligatoria por `idempotencyKey`.
- Errores:
  - `token_expirado_o_revocado`, `acceso_cruzado_detectado`.

### cerrar_sesion
_Esta subseccion detalla cerrar_sesion dentro del contexto del documento._
- Input esperado:
  - `sessionId` o `refreshToken`, `reason`, `idempotencyKey`.
- Precondiciones:
  - sesion existente y vigente para cierre.
- Postcondiciones:
  - sesion `REVOKED`.
  - evento `SessionRevoked` emitido.
- Idempotencia:
  - obligatoria por `idempotencyKey`.
- Errores:
  - `sesion_no_encontrada`, `token_expirado_o_revocado`.

### asignar_rol
_Esta subseccion detalla asignar_rol dentro del contexto del documento._
- Input esperado:
  - `userId`, `tenantId`, `roleId|roleCode`, `assignedBy`, `idempotencyKey`.
- Precondiciones:
  - usuario y rol pertenecen al mismo tenant.
  - asignacion equivalente no activa ya.
- Postcondiciones:
  - `user_role_assignment` activa creada o mantenida idempotentemente.
  - permisos efectivos recalculables.
  - evento `RoleAssigned` emitido.
- Errores:
  - `rol_invalido`, `acceso_cruzado_detectado`, `operacion_no_permitida`.

### bloquear_usuario
_Esta subseccion detalla bloquear_usuario dentro del contexto del documento._
- Input esperado:
  - `userId`, `tenantId`, `reason`, `blockedBy`, `idempotencyKey`.
- Precondiciones:
  - usuario existente.
- Postcondiciones:
  - usuario `BLOCKED`.
  - sesiones activas revocadas dentro del mismo caso.
  - evidencia tecnica y evento `UserBlocked` emitidos.
- Errores:
  - `usuario_no_encontrado`, `acceso_cruzado_detectado`.

### revocar_sesiones_usuario
_Esta subseccion detalla revocar_sesiones_usuario dentro del contexto del documento._
- Input esperado:
  - `userId`, `tenantId`, `reason`, `idempotencyKey`.
- Precondiciones:
  - usuario existente.
- Postcondiciones:
  - sesiones activas del usuario revocadas.
  - evento `SessionsRevokedByUser` emitido.
- Errores:
  - `usuario_no_encontrado`, `acceso_cruzado_detectado`.

## Consultas relevantes (no mutantes)
_Responde: que consultas activas expone el contexto aunque no formen parte del catalogo de comandos._
- `introspectar_token`: devuelve estado de sesion y permisos efectivos del token consultado.
- `consultar_sesiones_usuario`: lista sesiones activas/revocadas de un usuario dentro del tenant.
- `consultar_permisos_usuario`: materializa el permiso efectivo del usuario a partir de roles y asignaciones.
