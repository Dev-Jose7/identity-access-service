---
title: "Integracion - servicio-inventario"
linkTitle: "2. Integracion"
weight: 2
url: "/mvp/calidad/pruebas/servicio-inventario/integracion/"
---

## Objetivo
Validar contratos REST/eventos, persistencia operativa, idempotencia write-side y dedupe async de Inventory.

## Dependencias y entornos
- BD Inventory (`stock_items`, `stock_reservations`, `stock_movements`, `reservation_ledgers`, `idempotency_records`, `inventory_audits`, `outbox_events`, `processed_events`).
- broker/event bus para publicacion/consumo.
- contexto de seguridad para usuarios, `order_service` y scheduler.

## Datos de entrada
- requests/responses de contratos `v1`.
- headers `Authorization`, `Idempotency-Key`, `X-Request-Id` en mutaciones.
- eventos entrantes Catalog: `catalog.variant-created.v1`, `catalog.variant-updated.v1`, `catalog.variant-discontinued.v1`, `catalog.product-retired.v1`.

## Matriz detallada de casos de integracion
| ID | Escenario | Validacion principal | Evidencia | Trazabilidad |
|---|---|---|---|---|
| INV-IT-001 | `POST /stock/initialize` | contrato + alta de stock base | 201 + `stock_items` + `stock_movements` + outbox `StockInitialized/StockUpdated` | FR-002, NFR-006 |
| INV-IT-002 | `POST /stock/adjustments` | ajuste absoluto con auditoria | 200 + movimiento + `inventory_audits` + outbox `StockAdjusted` | FR-002 |
| INV-IT-003 | `POST /stock/increase` | incremento valido | 200 + `StockIncreased` + `StockUpdated` | FR-002 |
| INV-IT-004 | `POST /stock/decrease` valido | decremento dentro de limites | 200 + `StockDecreased` + `StockUpdated` | FR-002 |
| INV-IT-005 | `POST /stock/decrease` invalido | no violar stock negativo | 409 `stock_negativo_invalido` + sin mutacion efectiva | I-INV-01 |
| INV-IT-006 | `POST /stock/bulk-adjustments` | lote con idempotencia | resultado consistente por item + auditoria | FR-002, NFR-009 |
| INV-IT-007 | `POST /reservations` exitoso | reserva activa con TTL | 201 + fila `stock_reservations` + outbox `StockReserved` | FR-004 |
| INV-IT-008 | `POST /reservations` sin disponibilidad | rechazo por stock insuficiente | 409 `stock_insuficiente` + sin reserva parcial | FR-004, RN-RES-02 |
| INV-IT-009 | `PATCH /reservations/{id}/extend` | extension de TTL | 200 + `expiresAt` actualizado + `StockReservationExtended` | FR-004 |
| INV-IT-010 | `POST /reservations/{id}/confirm` | confirmacion de reserva vigente | 200 + estado `CONFIRMED` + `StockReservationConfirmed` | FR-004 |
| INV-IT-011 | `POST /reservations/{id}/confirm` expirada | rechazo semantico | 409 `reserva_expirada` + sin consumo | FR-004 |
| INV-IT-012 | `POST /reservations/{id}/release` | liberacion de reserva activa | 200 + estado `RELEASED` + `StockReservationReleased` | FR-004 |
| INV-IT-013 | `POST /reservations/expire` | expiracion por lote | reservas vencidas a `EXPIRED` + `StockReservationExpired` | FR-004 |
| INV-IT-014 | `POST /internal/.../validate-reservations` valido | validacion sync checkout | `valid=true` con lista consistente | FR-004 |
| INV-IT-015 | `POST /internal/.../validate-reservations` invalido | reserva no vigente/no encontrada | `valid=false` + reasonCode estable | FR-004 |
| INV-IT-016 | `GET /availability` | disponibilidad reservable coherente | `available = physical - reserved` | FR-004, NFR-004 |
| INV-IT-017 | `GET /stock/{sku}` | snapshot por SKU/warehouse | respuesta consistente con DB | FR-002 |
| INV-IT-018 | `GET /warehouses/{id}/stock` | listado paginado operativo | page/size aplicados correctamente | FR-002 |
| INV-IT-019 | `GET /reservations/{id}` | detalle de reserva | estado y timestamps correctos | FR-004 |
| INV-IT-020 | `GET /reservations/timeline` | timeline de reservas | orden temporal y filtros correctos | NFR-006 |
| INV-IT-021 | idempotencia write-side | misma clave + mismo payload | reutiliza respuesta desde `idempotency_records` | NFR-009 |
| INV-IT-022 | conflicto idempotente | misma clave + hash distinto | 409 `conflicto_idempotencia` | NFR-009 |
| INV-IT-023 | consumo `catalog.variant-discontinued` | reconciliar SKU bloqueado | `stock_item.status=BLOCKED` + `SkuReconciled` + `processed_events` | politicas Inventory |
| INV-IT-024 | consumo `catalog.product-retired` | bloquear SKUs asociados | bloqueo + `processed_events` + auditoria | politicas Inventory |
| INV-IT-025 | dedupe evento Catalog duplicado | mismo `eventId` dos veces | segundo consumo `noop idempotente` | NFR-009 |
| INV-IT-026 | emision `LowStockDetected` | umbral bajo alcanzado | outbox/evento a notification/reporting | FR-003 |
| INV-IT-027 | seguridad tenant/ownership | actor tenant A sobre stock tenant B | rechazo 403/409 + sin cambios | NFR-005, I-ACC-02 |
| INV-IT-028 | propagacion `traceId/correlationId` | mutacion + evento | ids en response, auditoria y outbox | NFR-006 |

## Criterio de exito integracion
- Escenarios `INV-IT-001..028` disenados para verificar ausencia de breaking en contratos `v1`.
- Idempotencia HTTP (`idempotency_records`) y dedupe async (`processed_events`) disenados para verificarse con evidencia persistida durante la corrida.
- En corrida de certificacion, no deben ocurrir mutaciones cross-tenant.
