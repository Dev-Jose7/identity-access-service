---
title: "Modelo"
linkTitle: "2. Modelo"
weight: 2
url: "/mvp/dominio/contextos-delimitados/reporteria/modelo/"
---

## Marco del modelo
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Modelo conceptual del BC `reporting`.

## Entidades principales
_Responde: que entidades estructuran el modelo local._
- HechoAnalitico.
- VistaVentasSemanal.
- VistaAbastecimientoSemanal.
- EjecucionReporteSemanal.

## Value objects principales
_Responde: que objetos de valor expresan reglas relevantes sin identidad propia._
- `WeekId`.
- `FactType`.
- `AggregationWindow`.
- `TenantMetricKey`.

## Estados importantes
_Responde: que estados son relevantes para entender el ciclo local._
| Entidad | Estados permitidos | Inicial | Terminales |
|---|---|---|---|
| HechoAnalitico | `CAPTURED`, `NORMALIZED`, `APPLIED`, `REJECTED` | `CAPTURED` | `APPLIED`, `REJECTED` |
| EjecucionReporteSemanal | `PENDING`, `RUNNING`, `COMPLETED`, `FAILED` | `PENDING` | `COMPLETED`, `FAILED` |

## Reglas de negocio nucleo
_Responde: que reglas de negocio sostienen el modelo del contexto._
- Hechos se aplican idempotentemente por `sourceEventId`.
- Vista derivada nunca escribe en BC core.
- Reporte semanal usa corte temporal consistente por tenant/pais.

## Identidad de agregados
_Responde: como se identifica cada agregado relevante del contexto._
- `ReportingViewAggregate(viewId, tenantId, period, metrics, lastEventApplied)`.
- `WeeklyReportAggregate(reportId, weekId, reportType, status, locationRef)`.
