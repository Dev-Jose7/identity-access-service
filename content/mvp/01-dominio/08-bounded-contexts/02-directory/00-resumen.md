---
title: "Resumen"
linkTitle: "1. Resumen"
weight: 1
url: "/mvp/dominio/contextos-delimitados/directorio/resumen/"
---

## Marco del contexto
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Resumen ejecutivo del bounded context `directory`.

## Mision del contexto
_Responde: que verdad local gobierna este bounded context y para que existe dentro del dominio._
- Gestionar organizacion, direcciones, perfiles de usuario organizacionales y contactos institucionales con ownership claro por tenant.
- Proveer validacion de direccion para checkout y configuracion operativa por pais sin duplicar logica de `order`.

## Procesos y casos de uso cubiertos
_Responde: que procesos del MVP cubre este contexto y con que casos de uso contribuye._
| Proceso | Casos de uso | Resultado |
|---|---|---|
| P1 Gestion de acceso/contexto de cliente | UC-DIR-01..UC-DIR-05 | organizacion habilitada/suspendida y perfiles de usuario organizacionales coherentes con IAM |
| P2/P4 Checkout y pedido | UC-DIR-06..UC-DIR-14 | direcciones y contactos institucionales validos para pedido/comunicacion |
| Soporte operativo | UC-DIR-15..UC-DIR-17 | resumen consistente de directorio y parametros regionales |

## Responsabilidades
_Responde: que responsabilidades locales asume este contexto._
- Alta/actualizacion/suspension de organizacion.
- Gestion de perfiles de usuario organizacionales vinculados a usuarios de `identity-access`.
- Gestion de contactos institucionales con primario por tipo.
- Gestion de direcciones con default por tipo.
- Gestion de parametros operativos por pais versionables y auditables por organizacion.
- Validacion de direccion para checkout.
- Emision de eventos de cambios de directorio.

## Limites (que NO hace)
_Responde: que queda explicitamente fuera del contexto para proteger sus fronteras._
- No autentica ni administra sesiones (eso es `identity-access`).
- No gestiona credenciales, roles efectivos ni llaves de acceso.
- No confirma pedidos ni gestiona pagos.
- No decide disponibilidad de stock.

## Dependencias externas
_Responde: de que otros contextos o contratos depende este contexto._
- `identity-access` para referencia de identidad y sincronizacion de bloqueos/roles sobre perfiles locales.
- `order` como consumidor de validaciones de direccion y cambios organizacionales/regionales.
- `notification` y `reporting` como consumidores de cambios organizacionales y de contactos institucionales.

## FR/NFR relacionados
_Responde: que requisitos del producto aterrizan principalmente en este contexto._
- FR-004, FR-005, FR-006, FR-009, FR-011.
- NFR-005, NFR-006, NFR-011.

## Riesgos del contexto
_Responde: que riesgos locales existen y como se mitigan._
- Riesgo: perfil de usuario organizacional desalineado con IAM.
  - Mitigacion: sincronizacion por `RoleAssigned` y `UserBlocked` + reconciliacion controlada.
- Riesgo: direccion default faltante en organizacion activa.
  - Mitigacion: regla de activacion exige direccion valida por tipo.
- Riesgo: fuga cross-tenant en consultas administrativas.
  - Mitigacion: tenant check obligatorio en lectura y escritura.
