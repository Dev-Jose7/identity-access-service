---
title: "Resumen"
linkTitle: "1. Resumen"
weight: 1
url: "/mvp/dominio/contextos-delimitados/identidad-acceso/resumen/"
---

## Marco del contexto
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Resumen ejecutivo del bounded context `identity-access`.

## Mision del contexto
_Responde: que verdad local gobierna este bounded context y para que existe dentro del dominio._
- Gestionar autenticacion, sesiones y autorizacion base por `tenant` para usuarios B2B.
- Mantener permisos efectivos, revocacion y evidencia de seguridad sin exponer el modelo interno de IAM a otros BC.
- Reaccionar al estado operativo de la organizacion referenciado desde `directory` para habilitar o bloquear el acceso del tenant.

## Procesos y casos de uso cubiertos
_Responde: que procesos del MVP cubre este contexto y con que casos de uso contribuye._
| Proceso | Casos de uso | Resultado |
|---|---|---|
| P1 Gestion de acceso B2B | UC-IAM-01..UC-IAM-11 | sesion valida, permisos efectivos, revocacion auditable y queries IAM coherentes |
| Seguridad operativa de acceso | bloqueo de usuario, revocacion masiva, reaccion a `OrganizationSuspended` y contencion por fallos de autenticacion | contencion de riesgo y aislamiento por tenant |

## Responsabilidades
_Responde: que responsabilidades locales asume este contexto._
- Autenticar usuario B2B por `tenantId`, `email` y credencial local activa.
- Emitir, refrescar y revocar sesiones activas.
- Resolver permisos efectivos por usuario, rol y asignacion vigente.
- Bloquear usuarios, revocar sesiones por lote y asignar roles administrativos.
- Registrar evidencia tecnica de seguridad y publicar eventos IAM por outbox.

## Limites (que NO hace)
_Responde: que queda explicitamente fuera del contexto para proteger sus fronteras._
- No administra direccion, perfiles de usuario organizacionales, contactos institucionales ni parametros operativos por pais (eso pertenece a `directory`).
- No decide verdad comercial de catalogo, stock, pedido o pago.
- No obliga a los servicios core a consultar IAM en el hot path de toda mutacion HTTP protegida.

## Dependencias externas
_Responde: de que otros contextos o contratos depende este contexto._
- `directory` para referencia de organizacion/tenant operable o suspendido.
- `order`, `notification` y `reporting` como consumidores de eventos IAM.
- `api-gateway-service` como borde principal de autenticacion HTTP para rutas protegidas fuera del propio IAM.

## FR/NFR relacionados
_Responde: que requisitos del producto aterrizan principalmente en este contexto._
- FR-009.
- NFR-005, NFR-006.

## Riesgos del contexto
_Responde: que riesgos locales existen y como se mitigan._
- Riesgo: desalineacion entre claims ya emitidos y permisos/estado actual del usuario.
  - Mitigacion: eventos `SessionRevoked`, `SessionsRevokedByUser`, `RoleAssigned` y `UserBlocked` + fallback de introspeccion cuando el estado no sea confiable.
- Riesgo: bypass de tenant o permiso en endpoints administrativos.
  - Mitigacion: doble validacion `permiso base + tenant + legitimidad del actor`.
- Riesgo: consumidores sin idempotencia ante eventos de revocacion o bloqueo.
  - Mitigacion: `eventId` + dedupe obligatorio en consumidores.
