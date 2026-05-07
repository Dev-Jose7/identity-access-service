---
title: "Politicas"
linkTitle: "6. Politicas"
weight: 6
url: "/mvp/dominio/contextos-delimitados/directorio/politicas/"
---

## Marco de politicas
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Politicas de reaccion de `directory`.

## Politicas (if event -> then command)
_Responde: que reacciones automatizadas ejecuta el contexto frente a eventos relevantes._
| Trigger (evento) | Condicion | Accion (comando) | Observaciones |
|---|---|---|---|
| `RoleAssigned` (identity-access) | el usuario ya tiene perfil de usuario organizacional local en la organizacion | `actualizar_perfil_usuario_organizacion` | alinea estado y atribucion local del perfil sin reescribir la verdad IAM |
| `UserBlocked` (identity-access) | usuario tiene perfil de usuario organizacional local activo | `desactivar_perfil_usuario_organizacion` | evita uso organizacional de un usuario bloqueado |

Nota de aplicacion:
- En `MVP`, `OrganizationProfileUpdated` y `CountryOperationalPolicyConfigured` no disparan listeners internos dedicados dentro de `directory-service`.
- Su efecto operativo se materializa en validaciones sincronas posteriores, principalmente `ValidateCheckoutAddress` y `ResolveCountryOperationalPolicy`, sin introducir un loop interno adicional basado en eventos propios.

## Retries / compensacion
_Responde: como maneja este contexto reintentos y compensaciones sin romper su modelo._
- Retry de publicacion de eventos: 3 intentos con backoff.
- Compensacion: si evento no publica, conservar outbox pendiente sin revertir mutacion del directorio.

## Timeouts
_Responde: que limites temporales gobiernan las interacciones de este contexto._
- validacion taxId/geo externa: 300-500 ms por intento.
- validacion direccion checkout: 300 ms objetivo.
