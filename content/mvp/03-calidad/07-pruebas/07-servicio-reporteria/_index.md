---
title: "Servicio Reporteria"
linkTitle: "7. Reporteria"
weight: 7
url: "/mvp/calidad/pruebas/servicio-reporteria/"
---

## Objetivo
Asegurar proyecciones analiticas `read-only`, generacion de reportes semanales por pais y trazabilidad tecnica completa sin romper aislamiento tenant ni reglas regionales.

## Alcance de calidad del servicio
- Flujos HTTP de consulta (`sales`, `replenishment`, `operations`, `artifacts`) y operaciones internas (`rebuild`, `weekly/generate`, `reprocess-dlq`).
- Flujos async de consumo multi-servicio (`order`, `inventory`, `catalog`, `directory`, `notification`) y emision de eventos Reporting.
- Reglas de seguridad, idempotencia, dedupe y regionalizacion operativa obligatoria.

## Fuentes de verdad usadas
- Producto: `FR-003`, `FR-007`, `FR-010`, `FR-011`, `NFR-002`, `NFR-005`, `NFR-006`, `NFR-007`, `NFR-009`, `NFR-010`, `NFR-011`.
- Dominio: `RN-REP-01`, `I-REP-01`, `RN-LOC-01`, `I-LOC-01`, `I-ACC-02`, `D-CROSS-01`.
- Arquitectura Reporting: contratos API/eventos, seguridad, datos y runtime.

## Datos de entrada comunes
- `tenant` principal: `org-co-001` (`countryCode=CO`).
- `tenant` alterno: `org-ec-001` (`countryCode=EC`).
- actor de consulta con rol `tenant_user`.
- actor de consulta operativa con rol `arka_operator`.
- actor tecnico m2m con scope `service_scope:reporting.ops`.
- cabeceras de trazabilidad: `traceId`, `correlationId`.
- cabecera de idempotencia en mutaciones internas: `Idempotency-Key`.

## Criterio de exito global
- Carpetas `unitarias`, `integracion` y `e2e` cubren escenarios `P1` y `P2` del servicio.
- Cada escenario referencia al menos un `FR/NFR` y una regla/invariante de dominio.
- Se debe verificar explicitamente la regla de regionalizacion: sin politica vigente por `countryCode` se bloquea la operacion con `configuracion_pais_no_disponible`.
- Se valida aislamiento tenant/rol/scope en lectura y operaciones internas.

## Cobertura minima obligatoria (Reporting)
| Bloque | Cobertura minima |
|---|---|
| Ingestion y proyecciones | consumo idempotente multi-evento + dedupe por `sourceEventId` |
| Reporte semanal | generacion por `tenant+week+type` y unicidad operacional |
| Regionalizacion | bloqueo sin fallback global con error semantico estable |
| Read-only | cero mutaciones sobre BC core transaccionales |
| Operaciones internas | `rebuild`, `weekly/generate`, `reprocess-dlq` con idempotencia |
| Eventos salientes | `AnalyticFactUpdated`, `WeeklyReportGenerated` con envelope canonico |
| Seguridad | tenant isolation, RBAC baseline, trazabilidad auditada |
| Resiliencia | retry, DLQ, replay controlado sin duplicar efectos |

## Errores canonicos a cubrir en pruebas
- `configuracion_pais_no_disponible`
- `report_not_found`
- `artifact_not_found`
- `reporte_duplicado`
- `rebuild_in_progress`
- `conflicto_idempotencia`
- `forbidden_scope`
- `acceso_cruzado_detectado`
- `reporte_generacion_fallida`

## Evidencia minima por corrida
- resultado por escenario con estado segun taxonomia oficial.
- evidencia de DB: `analytic_facts`, `sales_projections`, `replenishment_projections`, `operations_kpi_projections`, `weekly_report_executions`, `report_artifacts`, `reporting_audits`, `outbox_events`, `processed_events`.
- evidencia de observabilidad: logs y traces con `traceId/correlationId` por flujo.
