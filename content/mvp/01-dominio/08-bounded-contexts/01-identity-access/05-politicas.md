---
title: "Politicas"
linkTitle: "6. Politicas"
weight: 6
url: "/mvp/dominio/contextos-delimitados/identidad-acceso/politicas/"
---

## Marco de politicas
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Politicas de reaccion de `identity-access`.

## Politicas (if event -> then command)
_Responde: que reacciones automatizadas ejecuta el contexto frente a eventos relevantes._
| Trigger (evento) | Condicion | Accion (comando) | Observaciones |
|---|---|---|---|
| `OrganizationSuspended` (directory) | tenant con usuarios operables en IAM | `bloquear_usuario` (por lote) | cada bloqueo revoca sesiones activas dentro del mismo caso |
| `AuthFailedThreshold` | umbral de fallos excedido para usuario/tenant | `bloquear_usuario` | politica anti abuso; el bloqueo cierra la superficie operativa y deja evidencia tecnica |

## Retries / compensacion
_Responde: como maneja este contexto reintentos y compensaciones sin romper su modelo._
- Retry: max 3 intentos para listeners y publicacion de eventos IAM cuando el fallo es retryable.
- Compensacion: si publicacion falla, conservar outbox pendiente sin revertir bloqueo/revocacion ya materializados.
- Duplicados: un evento ya procesado se trata como `noop idempotente`.

## Timeouts
_Responde: que limites temporales gobiernan las interacciones de este contexto._
- login: objetivo `p95 <= 250 ms`.
- refresh/logout/introspect: objetivo `p95 <= 120-180 ms` segun flujo.
