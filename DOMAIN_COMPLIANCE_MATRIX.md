# Foundation Domain Compliance Matrix
## Slice IAM-S01 - Foundation Domain Implementation

### Reglas/Invariantes -> Artefactos Implementados

| Regla/Invariante | Tipo | Artefacto Implementado | Estado | Observaciones |
|---|---|---|---|---|
| **RN-ACC-01**: Usuario bloqueado/deshabilitado no inicia sesión | REGLA | `UserAggregate.validateCanAuthenticate()` + `UserStatus.canAuthenticate()` | ✅ COMPLETADO | Validación en `recordSuccessfulLogin()` y `recordFailedLogin()` |
| **I-ACC-01**: Sesión activa solo para usuario habilitado | INVARIANTE | `UserAggregate.validateCanAuthenticate()` | ✅ COMPLETADO | Verifica `UserStatus.ACTIVE` y credencial activa |
| **I-ACC-02**: Ninguna acción muta recursos fuera de tenant autorizado | INVARIANTE | `TenantIsolationPolicy.validateTenantAccess()` | ✅ COMPLETADO | Validación en todos los métodos de políticas |
| **RN-ACC-02**: Toda mutación valida tenant y rol permitido | REGLA | `AuthorizationPolicy.validateTenantPermission()` | ✅ COMPLETADO | Validación cruzada con tenant y permisos |
| **Sesión válida requiere tenant, user y JTIs consistentes** | INVARIANTE | `SessionAggregate` validaciones | ✅ COMPLETADO | Validación en constructores y métodos |
| **Refresh rota JTIs y no duplica sesión activa** | INVARIANTE | `SessionAggregate.refresh()` | ✅ COMPLETADO | Genera nuevos JTIs, mantiene misma sesión |
| **Bloqueo de usuario revoca sesiones activas** | REGLA | `UserAggregate.block()` + eventos | ✅ COMPLETADO | Emite `UserBlockedEvent` + revocación interna |
| **Asignación de rol es idempotente** | INVARIANTE | `UserRoleAssignment.isEquivalentAssignment()` | ✅ COMPLETADO | Verifica tenant+userId+roleId únicos |
| **Asignación de rol exige mismo tenant** | REGLA | `RoleAggregate.assignToUser()` | ✅ COMPLETADO | Validación de tenant en asignación |

### Errores Canónicos -> Excepciones Implementadas

| Error Canónico | Excepción Implementada | Ubicación | Estado | Trigger |
|---|---|---|---|---|
| `credenciales_invalidas` | `CredencialesInvalidasException` | domain/exception | ✅ COMPLETADO | Validación de credenciales en login |
| `usuario_no_habilitado` | `UsuarioNoHabilitadoException` | domain/exception | ✅ COMPLETADO | Estado usuario != ACTIVE |
| `token_expirado_o_revocado` | `TokenExpiradoORevocadoException` | domain/exception | ✅ COMPLETADO | Sesión expirada o revocada |
| `sesion_no_encontrada` | `SesionNoEncontradaException` | domain/exception | ✅ COMPLETADO | Sesión no existe en repositorio |
| `usuario_no_encontrado` | `UsuarioNoEncontradoException` | domain/exception | ✅ COMPLETADO | Usuario no existe en sistema |
| `rol_invalido` | `RolInvalidoException` | domain/exception | ✅ COMPLETADO | Rol no válido para tenant |
| `operacion_no_permitida` | `OperacionNoPermitidaException` | domain/exception | ✅ COMPLETADO | Violación de reglas de negocio |
| `acceso_cruzado_detectado` | `AccesoCruzadoDetectadoException` | domain/exception | ✅ COMPLETADO | Intento de acceso cross-tenant |

### Estados/Transiciones Implementados

| Concepto | Estados Implementados | Transiciones Válidas | Estado Inicial | Estados Terminales | Artefacto |
|---|---|---|---|---|---|
| **UserStatus** | `ACTIVE`, `BLOCKED`, `DISABLED` | `ACTIVE` → `BLOCKED/DISABLED` | `ACTIVE` | `BLOCKED`, `DISABLED` | `UserStatus` enum |
| **SessionStatus** | `ACTIVE`, `REVOKED`, `EXPIRED` | `ACTIVE` → `REVOKED/EXPIRED` | `ACTIVE` | `REVOKED`, `EXPIRED` | `SessionStatus` enum |
| **CredentialStatus** | `ACTIVE`, `ROTATED`, `COMPROMISED` | `ACTIVE` → `ROTATED/COMPROMISED` | `ACTIVE` | `COMPROMISED` | `CredentialStatus` enum |
| **AssignmentStatus** | `ACTIVE`, `REVOKED` | `ACTIVE` → `REVOKED` | `ACTIVE` | `REVOKED` | `AssignmentStatus` enum |

### Eventos de Dominio Implementados

| Evento | Productor | Artefacto | Consumidores | Estado |
|---|---|---|---|---|
| `UserLoggedInEvent` | `UserAggregate.recordSuccessfulLogin()` | `UserLoggedInEvent` | reporting, security-monitoring | ✅ COMPLETADO |
| `UserBlockedEvent` | `UserAggregate.block()` | `UserBlockedEvent` | order, directory, notification, reporting, security-monitoring | ✅ COMPLETADO |
| `SessionRefreshedEvent` | `SessionAggregate.refresh()` | `SessionRefreshedEvent` | Sin consumidores obligatorios en MVP | ✅ COMPLETADO |
| `SessionRevokedEvent` | `SessionAggregate.revoke()` | `SessionRevokedEvent` | api-gateway-service, reporting, order | ✅ COMPLETADO |
| `SessionsRevokedByUserEvent` | Servicio de revocación masiva | `SessionsRevokedByUserEvent` | api-gateway-service, security-monitoring | ✅ COMPLETADO |
| `RoleAssignedEvent` | `RoleAggregate.assignToUser()` | `RoleAssignedEvent` | order, directory, reporting | ✅ COMPLETADO |

### Servicios de Dominio Implementados

| Servicio | Responsabilidad | Métodos Clave | Estado |
|---|---|---|---|
| **PasswordPolicy** | Validación de políticas de contraseña | `validate()`, `getRequirements()` | ✅ COMPLETADO |
| **TokenPolicy** | Gestión de vida útil de tokens | `calculateAccessTokenExpiry()`, `isRefreshTokenExpired()` | ✅ COMPLETADO |
| **TenantIsolationPolicy** | Aislamiento por tenant | `validateTenantAccess()`, `validateUserTenant()` | ✅ COMPLETADO |
| **AuthorizationPolicy** | Autorización basada en permisos | `validatePermission()`, `hasPermission()` | ✅ COMPLETADO |
| **SessionPolicy** | Políticas de sesión | `validateSessionCreation()`, `isSessionExpired()` | ✅ COMPLETADO |
| **PermissionResolutionService** | Resolución de permisos desde roles | `resolveUserPermissions()`, `hasPermission()` | ✅ COMPLETADO |

### Value Objects Implementados

| Value Object | Aggregate | Propósito | Artefacto | Estado |
|---|---|---|---|---|
| **TenantId** | user | Identificador único de tenant | `user/valueobject/TenantId` | ✅ COMPLETADO |
| **EmailAddress** | user | Email validado y normalizado | `user/valueobject/EmailAddress` | ✅ COMPLETADO |
| **UserId** | user | Identificador único de usuario | `user/valueobject/UserId` | ✅ COMPLETADO |
| **PasswordHash** | user | Hash de contraseña | `user/valueobject/PasswordHash` | ✅ COMPLETADO |
| **FailedLoginCounter** | user | Contador de intentos fallidos | `user/valueobject/FailedLoginCounter` | ✅ COMPLETADO |
| **CorrelationId** | user | ID de correlación de operaciones | `user/valueobject/CorrelationId` | ✅ COMPLETADO |
| **SessionId** | session | Identificador único de sesión | `session/valueobject/SessionId` | ✅ COMPLETADO |
| **AccessJti** | session | JTI de token de acceso | `session/valueobject/AccessJti` | ✅ COMPLETADO |
| **RefreshJti** | session | JTI de token de refresh | `session/valueobject/RefreshJti` | ✅ COMPLETADO |
| **ClientDevice** | session | Información de dispositivo cliente | `session/valueobject/ClientDevice` | ✅ COMPLETADO |
| **ClientIp** | session | Dirección IP cliente | `session/valueobject/ClientIp` | ✅ COMPLETADO |
| **SessionTimestamps** | session | Timestamps de sesión | `session/valueobject/SessionTimestamps` | ✅ COMPLETADO |
| **SessionTokenRef** | session | Referencia a token de sesión | `session/valueobject/SessionTokenRef` | ✅ COMPLETADO |
| **RoleId** | role | Identificador único de rol | `role/valueobject/RoleId` | ✅ COMPLETADO |
| **RoleCode** | role | Código de rol (GAP-01 abierto) | `role/valueobject/RoleCode` | ✅ COMPLETADO |
| **PermissionCode** | role | Código de permiso | `role/valueobject/PermissionCode` | ✅ COMPLETADO |
| **PermissionSet** | role | Conjunto inmutable de permisos | `role/valueobject/PermissionSet` | ✅ COMPLETADO |

### GAP-01: RoleCode sin Enum Final

✅ **MANTENIDO ABIERTO**: `RoleCode` implementado como value object con constantes predefinidas pero permite creación dinámica según GAP-01. No se cierra con enum final.

### Resumen de Cumplimiento

- **✅ 100% Cumplimiento**: Todas las reglas, invariantes, errores canónicos y eventos documentados han sido implementados
- **🔄 GAP-01 Preservado**: `RoleCode` mantiene flexibilidad sin cerrar con enum final
- **🏗️ Arquitectura Respetada**: Estructura de paquetes sigue especificación de vista de código
- **🔒 Invariantes Protegidas**: Estados y transiciones validados en aggregates
- **📋 Eventos Completos**: Todos los eventos del baseline implementados con estructura canónica

### Gaps Detectados

1. **Ningún gap crítico**: Todos los artefactos requeridos han sido implementados
2. **Mejoras futuras sugeridas**:
   - Validación adicional de patrones de seguridad en `PasswordPolicy`
   - Métricas de uso en aggregates para monitoreo
   - Políticas de rate limiting a nivel de dominio

---

**Conclusión**: El foundation domain para `identity-access-service` está completo y cumple con todos los requisitos del baseline documentado. Listo para implementación de casos de uso y adaptadores de infraestructura.
