---
title: "Integracion - servicio-notificaciones"
linkTitle: "2. Integracion"
weight: 2
url: "/mvp/calidad/pruebas/servicio-notificaciones/integracion/"
---

## Objetivo
Validar contratos REST/eventos, persistencia de solicitudes/intentos y comportamiento resiliente (retry, DLQ, reproceso) del servicio.

## Dependencias y entornos
- BD Notification (`notification_request`, `notification_attempt`, `notification_policy`, `notification_audit`, `outbox_event`, `processed_event`).
- broker/event bus para consumo y publicacion.
- provider adapter (mock/fake) para dispatch y callbacks.
- contexto de seguridad m2m y validacion de callback provider.

## Datos de entrada
- requests/responses de contratos `v1`.
- headers `Authorization`, `Idempotency-Key`, `X-Request-Id` en mutaciones internas.
- eventos entrantes: `order.*`, `inventory.*`, `directory.*`, `reporting.weekly-generated`, `iam.user-blocked`.

## Matriz detallada de casos de integracion
| ID | Escenario | Validacion principal | Evidencia | Trazabilidad |
|---|---|---|---|---|
| NOTI-IT-001 | `POST /requests` exitoso | contrato + persistencia solicitud | 201 + `notification_request` + `NotificationRequested` | FR-006, NFR-006 |
| NOTI-IT-002 | idempotencia en `POST /requests` | misma key + mismo payload | resultado reutilizado sin duplicar solicitud | NFR-009 |
| NOTI-IT-003 | conflicto idempotente request | misma key + payload distinto | `409 conflicto_idempotencia` | NFR-009 |
| NOTI-IT-004 | `POST /{id}/dispatch` exitoso | envio inmediato | estado `SENT` + intento + evento `NotificationSent` | FR-006 |
| NOTI-IT-005 | dispatch falla retryable | timeout/provider down | estado `FAILED` + `NotificationFailed(retryable=true)` | FR-006, NFR-007 |
| NOTI-IT-006 | `POST /{id}/retry` | reintento controlado | attemptNumber incrementa + policy respetada | FR-006 |
| NOTI-IT-007 | `POST /{id}/discard` | descarte no recuperable | estado `DISCARDED` + evento `NotificationDiscarded` | FR-006 |
| NOTI-IT-008 | callback provider valido | reconciliacion de entrega | actualiza request/attempt + auditoria | FR-006 |
| NOTI-IT-009 | callback provider invalido | firma/token invalido | rechazo + sin mutacion + auditoria de seguridad | NFR-005 |
| NOTI-IT-010 | dedupe natural callback | mismo providerRef+callbackEventId | segundo callback noop | NFR-009 |
| NOTI-IT-011 | `GET /requests` paginado | consulta operativa | page/size/filtros correctos | FR-006 |
| NOTI-IT-012 | `GET /{id}` y `/attempts` | detalle de solicitud e intentos | consistencia de estados e intentos | FR-006 |
| NOTI-IT-013 | `POST /reprocess-dlq` | reproceso tecnico por lote | mensajes reprocesados y trazados | NFR-007 |
| NOTI-IT-014 | consumo `OrderConfirmed` | generar solicitud de confirmacion | `NotificationRequested` + `processed_event` | FR-006 |
| NOTI-IT-015 | consumo `OrderStatusChanged` | generar aviso de estado | solicitud por canal y tenant correcto | FR-006 |
| NOTI-IT-016 | consumo `OrderPaymentRegistered` | generar aviso de pago manual | solicitud trazable | FR-010 |
| NOTI-IT-017 | consumo `CartAbandonedDetected` | generar recordatorio | solicitud no bloqueante | FR-008 |
| NOTI-IT-018 | consumo `StockReservationExpired` | aviso preventivo | solicitud creada + dedupe | FR-006 |
| NOTI-IT-019 | consumo `LowStockDetected` | alerta operativa | solicitud creada | FR-003, FR-006 |
| NOTI-IT-020 | consumo `ContactRegistered/Updated/PrimaryContactChanged` | resolver destinatario institucional | contexto de entrega actualizado + dedupe | FR-006 |
| NOTI-IT-021 | consumo `WeeklyReportGenerated` | distribucion de reporte | solicitud creada para destinatarios definidos | FR-007 |
| NOTI-IT-022 | consumo `UserBlocked` | comunicacion de seguridad (si aplica policy) | solicitud creada o descartada segun policy | FR-009 |
| NOTI-IT-023 | dedupe evento upstream duplicado | mismo `eventId` dos veces | segundo consumo `noop idempotente` | NFR-009 |
| NOTI-IT-024 | seguridad scope m2m | scope insuficiente en endpoints internos | 403 `forbidden_scope` + sin cambios DB | NFR-005 |
| NOTI-IT-025 | aislamiento tenant/ownership | actor tenant A sobre solicitud tenant B | rechazo `acceso_cruzado_detectado` | NFR-005, I-ACC-02 |
| NOTI-IT-026 | trazabilidad completa | mutacion + evento + callback | `traceId/correlationId` en request/audit/outbox/evento | NFR-006 |
| NOTI-IT-027 | ruta DLQ no recuperable | payload/evento irreparable | enrutado a DLQ + auditoria | NFR-007 |
| NOTI-IT-028 | no rollback del core ante fallo | fallo dispatch tras evento Order | solo falla Notification; core Order intacto | RN-NOTI-01, I-NOTI-01 |

## Criterio de exito integracion
- Escenarios `NOTI-IT-001..028` disenados para verificar ausencia de breaking en contratos `v1`.
- Dedupe, retry, DLQ y reproceso disenados para verificarse con evidencia persistida durante la corrida.
- En corrida de certificacion, no deben ocurrir mutaciones cross-tenant.
