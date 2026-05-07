---
title: "Agregados"
linkTitle: "3. Agregados"
weight: 3
url: "/mvp/dominio/contextos-delimitados/reporteria/agregados/"
---

## Marco de agregados
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Definir agregados e invariantes de `reporting`.

## Agregados
_Responde: que agregados protegen consistencia dentro del contexto._

### ReportingViewAggregate
_Esta subseccion detalla reportingviewaggregate dentro del contexto del documento._
- Proposito: preservar integridad de vistas derivadas.
- Invariantes:
  - cada evento fuente se aplica maximo una vez (`sourceEventId`).
  - no se aceptan mutaciones directas desde consumidores externos.
- Errores:
  - `evento_duplicado`, `hecho_analitico_invalido`.

### WeeklyReportAggregate
_Esta subseccion detalla weeklyreportaggregate dentro del contexto del documento._
- Proposito: generar reporte semanal consistente por ventana.
- Invariantes:
  - reporte por `weekId+tenantId+reportType` es unico.
  - reporte `COMPLETED` es inmutable.
- Errores:
  - `reporte_duplicado`, `ventana_reporte_invalida`.

## Reglas de consistencia
_Responde: que invariantes locales debe preservar este artefacto._
- lag alto de consumo activa recalculo de vista completa.
- inconsistencia detectada no propaga mutaciones a BC core; solo recalcula.
