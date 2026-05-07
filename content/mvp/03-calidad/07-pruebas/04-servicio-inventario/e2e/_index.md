---
title: "E2E - servicio-inventario"
linkTitle: "3. E2E"
weight: 3
url: "/mvp/calidad/pruebas/servicio-inventario/e2e/"
---

## Objetivo
Validar journeys criticos de inventario de punta a punta con Order, Catalog, Notification y Reporting, asegurando anti-sobreventa e integridad de reservas.

## Alcance E2E
- flujo completo de reserva/confirmacion/liberacion/expiracion.
- validacion interna de checkout por contrato sync.
- propagacion de eventos de stock/reservas a consumidores.
- seguridad tenant/rol e idempotencia operacional.

## Escenarios E2E priorizados
| ID | Escenario | Flujo | Resultado esperado | Trazabilidad |
|---|---|---|---|---|
| INV-E2E-001 | ciclo exitoso de reserva a confirmacion | crear reserva -> validar checkout -> confirmar reserva | estado `CONFIRMED` y stock comprometido consistente | FR-004, NFR-004 |
| INV-E2E-002 | rechazo por stock insuficiente en checkout | crear reserva con qty > disponibilidad | rechazo `stock_insuficiente` sin parciales | FR-004, RN-RES-02 |
| INV-E2E-003 | liberacion manual de reserva | crear reserva -> release | disponibilidad restaurada y evento `StockReservationReleased` | FR-004 |
| INV-E2E-004 | expiracion por scheduler | crear reserva corta -> scheduler expire | estado `EXPIRED` + evento a order/notification/reporting | FR-004 |
| INV-E2E-005 | validate-reservations sync para order | order llama endpoint interno con lote mixto | respuesta `valid=false` con `invalidReservations/reasonCodes` correctos | FR-004 |
| INV-E2E-006 | ajuste de stock con trazabilidad | initialize -> increase/decrease/adjust | ledger completo + auditoria + `StockUpdated` | FR-002, NFR-006 |
| INV-E2E-007 | bulk adjustment idempotente | enviar lote con misma `Idempotency-Key` | sin duplicar side effects | NFR-009 |
| INV-E2E-008 | reaccion a variante descontinuada | catalog emite `VariantDiscontinued` | inventory bloquea SKU y evita nuevas reservas | FR-001, FR-004 |
| INV-E2E-009 | reaccion a producto retirado | catalog emite `ProductRetired` | SKUs asociados bloqueados para nuevas operaciones | FR-001 |
| INV-E2E-010 | bajo stock genera alerta operativa | ajustar stock bajo umbral | `LowStockDetected` consumible por notification/reporting | FR-003 |
| INV-E2E-011 | aislamiento tenant en mutaciones y consultas | actor tenant A opera tenant B | rechazo `acceso_cruzado_detectado` | NFR-005, D-CROSS-01 |
| INV-E2E-012 | resiliencia outbox en falla de broker | mutacion exitosa con fallo de publicacion | decision persiste y outbox reintenta | NFR-006 |
| INV-E2E-013 | dedupe de eventos catalog en runtime | replay mismo evento catalog | `noop idempotente` en segundo consumo | NFR-009 |
| INV-E2E-014 | trazabilidad tecnica completa | ejecutar mutacion + flujo async | cadena `request -> db -> audit -> idempotency -> outbox -> evento` correlacionada | NFR-006, NFR-009 |

## Criterio de exito E2E
- Escenarios `INV-E2E-001..014` disenados para ejecucion reproducible; el estado final requiere corrida y evidencia.
- En corrida de certificacion, los flujos criticos (`001`, `002`, `005`, `010`, `011`) deben ejecutarse sin desviaciones semanticas.
- En corrida de certificacion, anti-sobreventa y aislamiento tenant/rol deben verificarse de punta a punta.

## Evidencia minima por corrida
- Reporte por escenario con estado (`Implementado`/`Ejecutado`/`Validado con evidencia`).
- Extractos de `inventory_audits`, `idempotency_records`, `outbox_events`, `processed_events` correlacionados.
- Confirmacion de contratos API/eventos `v1` sin ruptura para consumidores.
