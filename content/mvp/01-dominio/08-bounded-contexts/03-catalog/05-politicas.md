---
title: "Politicas"
linkTitle: "6. Politicas"
weight: 6
url: "/mvp/dominio/contextos-delimitados/catalogo/politicas/"
---

## Marco de politicas
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Politicas de reaccion de `catalog`.

## Politicas (if event -> then reaction)
_Responde: que reacciones automatizadas ejecuta el contexto frente a eventos relevantes._
| Trigger (evento) | Condicion | Accion (reaccion interna) | Observaciones |
|---|---|---|---|
| `StockUpdated` (inventory) | SKU existe en catalogo y el evento no fue procesado | `recalcular_hint_disponibilidad` | actualiza `availabilityHint`, cache e indice sin reemplazar el stock transaccional de `inventory` ni disparar comandos de negocio |
| `SkuReconciled` (inventory) | SKU existe en catalogo y el mensaje es legitimo | `reconciliar_visibilidad_sku` | ajusta materializacion de busqueda, cache e indice con dedupe sobre `processed_event`; no implica `descontinuar_variante` |

## Nota de alineacion
- `ProductUpdated` no dispara `carga_masiva_precios`.
- Los cambios comerciales de producto se materializan dentro del mismo caso que actualiza el producto y luego se propagan por `outbox`.

## Retries / compensacion
_Responde: como maneja este contexto reintentos y compensaciones sin romper su modelo._
- Retry de publicacion de eventos: 3 intentos con backoff.
- Compensacion: conservar outbox pendiente ante falla de broker.

## Timeouts
_Responde: que limites temporales gobiernan las interacciones de este contexto._
- resolver variante checkout: 300 ms objetivo.
- upsert precio: 300 ms objetivo.
