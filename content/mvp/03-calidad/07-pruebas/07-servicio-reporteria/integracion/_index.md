---
title: "Integracion - servicio-reporteria"
linkTitle: "2. Integracion"
weight: 2
url: "/mvp/calidad/pruebas/servicio-reporteria/integracion/"
---

## Objetivo
Validar contratos REST/eventos, persistencia de proyecciones/artefactos y comportamiento resiliente de ingestion, dedupe y reproceso.

## Dependencias y entornos
- BD Reporting (`analytic_facts`, `sales_projections`, `replenishment_projections`, `operations_kpi_projections`, `weekly_report_executions`, `report_artifacts`, `reporting_audits`, `outbox_events`, `processed_events`).
- broker/event bus para consumo y publicacion.
- `directory-service` para resolver politica operativa por `countryCode`.
- almacenamiento de artefactos (objeto) para CSV/PDF.
- contexto de seguridad para rol usuario y scope tecnico `reporting.ops`.

## Datos de entrada
- requests/responses de contratos `v1`.
- headers `Authorization`, `Idempotency-Key`, `X-Request-Id`.
- eventos entrantes: `order.*`, `inventory.*`, `catalog.*`, `directory.*`, `notification.*` definidos para Reporting.

## Matriz detallada de casos de integracion
| ID | Escenario | Validacion principal | Evidencia | Trazabilidad |
|---|---|---|---|---|
| REP-IT-001 | `GET /reports/sales/weekly` exitoso | consulta semanal de ventas | 200 + payload coherente + `traceId` | FR-007, NFR-002 |
| REP-IT-002 | `GET /reports/replenishment/weekly` exitoso | consulta semanal de abastecimiento | 200 + riesgos por SKU consistentes | FR-003, NFR-002 |
| REP-IT-003 | `GET /reports/operations/kpis` | consulta KPI operativos | 200 + agregados consistentes por periodo | FR-007 |
| REP-IT-004 | `GET /reports/artifacts/{reportId}` | metadata/descarga valida | 200 + ownership tenant correcto | FR-007, NFR-005 |
| REP-IT-005 | consulta sin reporte existente | week sin datos | `404 report_not_found` | FR-007 |
| REP-IT-006 | artifact inexistente | `reportId` invalido | `404 artifact_not_found` | FR-007 |
| REP-IT-007 | bloqueo por politica regional ausente en consulta | `directory` sin politica vigente en resolucion tecnica | `409 configuracion_pais_no_disponible` (mapeo de negocio sobre ausencia tecnica) | FR-011, NFR-011 |
| REP-IT-008 | `POST /internal/reports/rebuild` exitoso | operacion interna con idempotencia | 202 + `weekly_report_executions` + auditoria | FR-003, NFR-008 |
| REP-IT-009 | `POST /internal/reports/rebuild` en paralelo | rebuild activo previo | `409 rebuild_in_progress` | NFR-008 |
| REP-IT-010 | idempotencia en rebuild | misma key + mismo payload | misma respuesta, sin doble efecto | NFR-009 |
| REP-IT-011 | conflicto idempotente en rebuild | misma key + payload distinto | `409 conflicto_idempotencia` | NFR-009 |
| REP-IT-012 | `POST /internal/reports/weekly/generate` exitoso | generacion manual semanal | 202 + outbox `WeeklyReportGenerated` | FR-007 |
| REP-IT-013 | duplicado de reporte semanal | `tenant+week+type` existente | `409 reporte_duplicado` | FR-007 |
| REP-IT-014 | bloqueo regional en generacion | `directory` sin politica vigente en resolucion tecnica | `409 configuracion_pais_no_disponible` (mapeo de negocio sobre ausencia tecnica) | FR-011, RN-LOC-01 |
| REP-IT-015 | `POST /internal/reports/reprocess-dlq` | reproceso tecnico de mensajes | 202 + eventos recuperados + auditoria | NFR-007 |
| REP-IT-016 | consumo `OrderCreated` | registrar hecho analitico inicial | `analytic_facts` + `processed_events` | FR-007, NFR-006 |
| REP-IT-017 | consumo `OrderConfirmed` | actualizar ventas confirmadas | vista ventas actualizada + dedupe | FR-007 |
| REP-IT-018 | consumo `OrderPaymentRegistered` | actualizar metricas de cobro | KPI financiero actualizado | FR-010 |
| REP-IT-019 | consumo `OrderPaymentStatusUpdated` | recalculo de estado de pago | metrica de cobranza consistente | FR-010 |
| REP-IT-020 | consumo `StockUpdated` | actualizar cobertura de abastecimiento | `replenishment_projections` actualizada | FR-003 |
| REP-IT-021 | consumo `StockReservationExpired` | ajustar cobertura y riesgo | vista abastecimiento recalculada | FR-003 |
| REP-IT-022 | consumo `LowStockDetected` | elevar riesgo operativo | riesgo `HIGH` en proyeccion | FR-003 |
| REP-IT-023 | consumo `PriceUpdated/PriceScheduled` | impacto de revenue proyectado | vistas comerciales recalculadas | FR-001, FR-007 |
| REP-IT-024 | consumo `OrganizationProfileUpdated` | refresco de segmentacion | dimension organizacional actualizada | FR-007 |
| REP-IT-025 | consumo `NotificationSent/Failed` | KPI de efectividad de notificaciones | metricas de canal actualizadas | FR-006, FR-007 |
| REP-IT-026 | dedupe de evento entrante duplicado | mismo `eventId/sourceEventId` | segundo consumo `noop idempotente` | RN-REP-01, NFR-009 |
| REP-IT-027 | emision `AnalyticFactUpdated` | publicacion de hecho aplicado | outbox/evento con envelope canonico | NFR-006 |
| REP-IT-028 | emision `WeeklyReportGenerated` | publicacion de reporte semanal | evento con `tenant+week+type+locationRef` | FR-007 |
| REP-IT-029 | seguridad scope tecnico | token sin `reporting.ops` en endpoint interno | `403 forbidden_scope` + sin mutacion | NFR-005 |
| REP-IT-030 | aislamiento tenant en consulta | actor tenant A consulta tenant B | rechazo `acceso_cruzado_detectado` | NFR-005, I-ACC-02 |
| REP-IT-031 | trazabilidad extremo a extremo | request + consumo + emision | `traceId/correlationId` en API, audit y eventos | NFR-006 |
| REP-IT-032 | resiliencia en falla de broker | persistencia outbox con broker no disponible | decision local persistida + retry posterior | NFR-007 |

## Criterio de exito integracion
- Escenarios `REP-IT-001..032` disenados para verificar ausencia de breaking en contratos `v1`.
- Cobertura completa de regionalizacion, idempotencia y dedupe async.
- En corrida de certificacion, no deben ocurrir mutaciones cross-tenant.
