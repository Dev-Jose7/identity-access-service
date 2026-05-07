---
title: "Repositorios y Puertos"
linkTitle: "7. Repositorios y Puertos"
weight: 7
url: "/mvp/dominio/contextos-delimitados/reporteria/repositorios-puertos/"
---

## Marco de puertos
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Definir puertos de entrada/salida y contratos de persistencia/integracion de `reporting`.

## Puertos de entrada (in)
_Responde: que puertos de entrada exponen casos de uso o capacidades del contexto._
| Port | Tipo | Metodos clave |
|---|---|---|
| `ReportingEventInPort` | in | `registrarHechoAnalitico`, `aplicarEventoFuente` |
| `ReportingUseCasePort` | in | `refrescarVistaVentas`, `refrescarVistaAbastecimiento`, `generarReporteSemanal`, `recalcularVistaCompleta` |
| `ReportingQueryPort` | in | `obtenerReporte`, `listarIndicadores` |

## Puertos de salida (out)
_Responde: que puertos de salida necesita el contexto para colaborar con otros sistemas._
| Port | Tipo | Metodos clave |
|---|---|---|
| `SalesViewRepositoryPort` | out | `upsertMetrics`, `getByPeriod` |
| `SupplyViewRepositoryPort` | out | `upsertMetrics`, `getByPeriod` |
| `FactRepositoryPort` | out | `saveFact`, `existsBySourceEventId` |
| `WeeklyReportRepositoryPort` | out | `saveExecution`, `findByWeekAndType`, `saveLocationRef` |
| `ReportExporterPort` | out | `exportCsv`, `exportPdf` |
| `OutboxPort` | out | `append`, `listPending`, `markPublished` |

## Contratos de consistencia
_Responde: que contratos preservan consistencia entre puertos, repositorios y reglas locales._
- aplicar hecho y actualizar vista debe ser idempotente por `sourceEventId`.
- generacion de reporte guarda ejecucion y referencia de salida antes de publicar evento.
- reporting nunca llama comandos mutantes en BC core.
