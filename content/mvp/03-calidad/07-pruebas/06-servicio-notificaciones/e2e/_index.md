---
title: "E2E - servicio-notificaciones"
linkTitle: "3. E2E"
weight: 3
url: "/mvp/calidad/pruebas/servicio-notificaciones/e2e/"
---

## Objetivo
Validar journeys de notificacion de punta a punta desde eventos de negocio hasta entrega/fallo controlado, sin afectar el core transaccional.

## Alcance E2E
- envio por eventos de Order/Inventory/Reporting.
- callbacks de proveedor y reconciliacion.
- retries, descarte y DLQ.
- seguridad m2m, tenant y callback trust.

## Escenarios E2E priorizados
| ID | Escenario | Flujo | Resultado esperado | Trazabilidad |
|---|---|---|---|---|
| NOTI-E2E-001 | confirmacion de pedido notifica cliente | `OrderConfirmed` -> request -> dispatch | `NotificationSent` y traza completa | FR-006 |
| NOTI-E2E-002 | cambio de estado notifica no bloqueante | `OrderStatusChanged` -> dispatch con falla | `NotificationFailed`, pedido intacto | FR-006, RN-NOTI-01 |
| NOTI-E2E-003 | pago manual genera aviso | `OrderPaymentRegistered` -> dispatch | notificacion de pago emitida | FR-010 |
| NOTI-E2E-004 | carrito abandonado genera recordatorio | `CartAbandonedDetected` -> request -> dispatch | notificacion de recuperacion emitida | FR-008 |
| NOTI-E2E-005 | reserva expirada avisa al cliente | `StockReservationExpired` -> request | aviso preventivo publicado | FR-006 |
| NOTI-E2E-006 | low stock genera alerta operativa | `LowStockDetected` -> request | alerta operacional enviada | FR-003, FR-006 |
| NOTI-E2E-007 | callback provider cierra entrega | dispatch -> callback DELIVERED | reconciliacion de intento/estado consistente | FR-006 |
| NOTI-E2E-008 | callback invalido es rechazado | callback con firma/token invalido | sin mutacion de estado + auditoria seguridad | NFR-005 |
| NOTI-E2E-009 | retries controlados hasta descarte | fallos retryables sucesivos | `FAILED` -> reintentos -> `DISCARDED` | NFR-007 |
| NOTI-E2E-010 | reproceso de DLQ recupera mensajes validos | enviar a DLQ -> `reprocess-dlq` | mensajes reaplicados sin duplicidad | NFR-007, NFR-009 |
| NOTI-E2E-011 | aislamiento tenant en endpoints internos | actor tenant A opera tenant B | rechazo `acceso_cruzado_detectado` | NFR-005 |
| NOTI-E2E-012 | dedupe de eventos upstream | replay mismo `eventId` | segundo consumo `noop idempotente` | NFR-009 |
| NOTI-E2E-013 | resiliencia provider timeout | provider timeout masivo | backlog controlado + no rollback core | NFR-007 |
| NOTI-E2E-014 | trazabilidad tecnica completa | evento negocio -> envio/callback | cadena `evento->request->attempt->outbox->evento` correlacionada | NFR-006 |

## Criterio de exito E2E
- Escenarios `NOTI-E2E-001..014` disenados para ejecucion reproducible; el estado final requiere corrida y evidencia.
- En corrida de certificacion, los flujos criticos (`001`, `002`, `004`, `009`, `011`) deben ejecutarse sin desviaciones semanticas.
- Se debe verificar explicitamente que la falla de notificacion no revierte core transaccional.

## Evidencia minima por corrida
- Reporte por escenario con estado (`Implementado`/`Ejecutado`/`Validado con evidencia`).
- Extractos de `notification_audit`, `notification_attempt`, `outbox_event`, `processed_event` correlacionados.
- Confirmacion de contratos API/eventos `v1` sin ruptura para consumidores.
