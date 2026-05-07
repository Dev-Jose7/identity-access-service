---
title: "Anti-Corrupcion"
linkTitle: "8. Anti-Corrupcion"
weight: 8
url: "/mvp/dominio/contextos-delimitados/identidad-acceso/anti-corrupcion/"
---

## Marco de traduccion
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Documentar traducciones semanticas entre `identity-access` y otros BC para evitar fuga de significado.

## Mapeos principales
_Responde: que traducciones principales hace el contexto al cruzar sus fronteras._
| Upstream/Downstream | Termino externo | Termino IAM | Regla |
|---|---|---|---|
| `directory -> identity-access` | `OrganizationActivated` / `OrganizationSuspended` | `tenant operable` / `tenant suspendido para acceso` | IAM reacciona sobre usuarios y sesiones sin duplicar el modelo organizacional |
| `identity-access -> servicios core` | claims confiables del JWT | `PrincipalContext` | `api-gateway-service` autentica; cada servicio autoriza localmente y solo usa fallback de introspeccion cuando el estado no es confiable |
| `identity-access -> order/directory/reporting` | `RoleAssigned` | cambio de autorizacion vigente | consumidores ajustan permisos o perfiles de usuario organizacionales locales sin reescribir catalogos IAM |
| `identity-access -> servicios core` | `SessionRevoked` / `SessionsRevokedByUser` | contexto ya no operativo | mutaciones protegidas deben rechazarse o reautenticarse |
| `identity-access -> order/directory/notification/reporting/security-monitoring` | `UserBlocked` | bloqueo operativo propagado | consumidores detienen mutaciones, inactivan perfiles de usuario organizacionales locales o alertan sin reinterpretar la sesion ni generar rollback cross-BC |

## Normalizacion de errores
_Responde: como traduce este contexto errores externos a su lenguaje canonico._
| Error tecnico | Error canonico de dominio |
|---|---|
| token malformed / token invalid signature | `token_expirado_o_revocado` |
| tenant mismatch claim/resource | `acceso_cruzado_detectado` |
| user disabled/blocked | `usuario_no_habilitado` |
| missing permission in trusted context | `operacion_no_permitida` |

## Reglas ACL
_Responde: que reglas gobiernan la capa ACL para evitar contaminacion semantica._
- IAM no expone estructuras internas de credenciales, hashes o llaves a BCs externos.
- IAM publica solo campos necesarios para contexto de seguridad (`userId`, `tenantId`, `roleId/roleCode`, `sessionId`, `reason`).
- Los servicios core no reinterpretan el modelo interno de sesion o credencial; consumen claims confiables, eventos IAM o introspeccion de fallback.
- BC consumidor no debe reinterpretar estados de sesion fuera de la taxonomia canonica.
