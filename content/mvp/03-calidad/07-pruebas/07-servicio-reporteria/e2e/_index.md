---
title: "E2E - servicio-reporteria"
linkTitle: "3. E2E"
weight: 3
url: "/mvp/calidad/pruebas/servicio-reporteria/e2e/"
---

## Objetivo
Validar journeys criticos end-to-end de ingestion, consolidacion y consumo de reportes semanales con seguridad tenant/rol, regionalizacion obligatoria y resiliencia operativa.

## Alcance E2E
- ingestion multi-fuente desde `order`, `inventory`, `catalog`, `directory`, `notification`.
- generacion y consulta de reportes semanales por pais.
- operaciones internas de recuperacion (`rebuild`, `reprocess-dlq`).
- emision y consumo de `WeeklyReportGenerated` en integracion con `notification-service`.

## Escenarios E2E priorizados
| ID | Escenario | Flujo | Resultado esperado | Trazabilidad |
|---|---|---|---|---|
| REP-E2E-001 | consolidacion semanal de ventas | eventos `OrderCreated/Confirmed/Payment*` -> proyeccion -> consulta weekly | KPI de ventas y cobro consistentes por `tenant+week` | FR-007, FR-010 |
| REP-E2E-002 | consolidacion de abastecimiento | eventos `StockUpdated/Reserved/Expired/LowStock` -> proyeccion -> consulta | reporte de abastecimiento con riesgo correcto | FR-003 |
| REP-E2E-003 | enriquecimiento comercial de reporte | eventos `ProductUpdated/PriceUpdated/PriceScheduled` | metricas comerciales recalculadas sin romper trazabilidad | FR-001, FR-007 |
| REP-E2E-004 | segmentacion por cambios de directorio | `OrganizationProfileUpdated` + `PrimaryContactChanged` | vistas operativas reflejan contexto actualizado | FR-007 |
| REP-E2E-005 | KPI de efectividad de notificaciones | `NotificationRequested/Sent/Failed/Discarded` -> Reporting | indicadores de canal disponibles en KPIs | FR-006, FR-007 |
| REP-E2E-006 | generacion semanal automatica | timer fin de semana -> `weekly/generate` -> export | `WeeklyReportGenerated` emitido con `locationRef` valido | FR-007, NFR-002 |
| REP-E2E-007 | consulta de artifact posterior a generacion | weekly generado -> `GET /artifacts/{reportId}` | metadata y descarga autorizada | FR-007 |
| REP-E2E-008 | bloqueo por ausencia de politica regional | consulta/generacion para `countryCode` sin politica | `409 configuracion_pais_no_disponible` + auditoria | FR-011, NFR-011 |
| REP-E2E-009 | no fallback global implicito | politica global disponible pero local ausente | operacion bloqueada igual | I-LOC-01 |
| REP-E2E-010 | rebuild de recuperacion por lag | simular lag alto -> `POST /internal/reports/rebuild` | proyeccion reconciliada sin duplicados | FR-003, NFR-008 |
| REP-E2E-011 | reproceso DLQ recupera eventos validos | enviar mensajes a DLQ -> `reprocess-dlq` | eventos reaplicados y `processed_events` consistente | NFR-007 |
| REP-E2E-012 | dedupe de replay de eventos | replay mismo `eventId/sourceEventId` | segundo procesamiento `noop idempotente` | RN-REP-01, NFR-009 |
| REP-E2E-013 | aislamiento tenant en consultas | actor tenant A consulta tenant B | rechazo `acceso_cruzado_detectado` | NFR-005, I-ACC-02 |
| REP-E2E-014 | control RBAC de ops internas | token sin `reporting.ops` llama `/internal/*` | rechazo `403 forbidden_scope` | NFR-005 |
| REP-E2E-015 | idempotencia de `weekly/generate` | misma `Idempotency-Key` y payload | no duplica reporte ni evento saliente | NFR-009 |
| REP-E2E-016 | trazabilidad tecnica completa | request API + listeners + outbox + evento | cadena correlacionada por `traceId/correlationId` | NFR-006 |
| REP-E2E-017 | falla de export y recuperacion | storage temporalmente no disponible | `FAILED` + `reporte_generacion_fallida`, recuperable por retry/rebuild | NFR-007 |
| REP-E2E-018 | consumidor downstream de reporte semanal | `WeeklyReportGenerated` consumido por Notification | notificacion de reporte disparada sin ruptura de contrato | FR-007 |

## Criterio de exito E2E
- Escenarios `REP-E2E-001..018` disenados para ejecucion reproducible; el estado final requiere corrida y evidencia.
- En corrida de certificacion, los flujos criticos (`001`, `002`, `006`, `008`, `013`, `014`) deben ejecutarse sin desviaciones semanticas.
- En corrida de certificacion, regionalizacion sin fallback global, seguridad tenant/scope y dedupe deben verificarse de punta a punta.

## Evidencia minima por corrida
- reporte por escenario con estado (`Implementado`/`Ejecutado`/`Validado con evidencia`).
- extractos correlacionados de `processed_events`, `weekly_report_executions`, `report_artifacts`, `reporting_audits`, `outbox_events`.
- confirmacion de compatibilidad de contratos API/eventos `v1` con consumidores actuales.
