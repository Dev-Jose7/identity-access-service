---
title: "Presupuestos de Rendimiento"
linkTitle: "1. Presupuestos de Rendimiento"
weight: 1
url: "/mvp/arquitectura/servicios/servicio-catalogo/rendimiento/presupuestos-rendimiento/"
---

## Proposito
Definir objetivos no funcionales de performance y capacidad para `catalog-service`, incluyendo presupuestos de latencia, throughput, error y escalamiento.

## Alcance y fronteras
- Incluye NFR de endpoints de consulta y administracion de Catalog, listeners internos consumidos desde Inventory y drenado async del outbox.
- Incluye escenarios de carga academicos simulados para catalogo B2B de alta cardinalidad.
- Excluye resultados de pruebas ejecutadas (fase 05).

## Budget por familia HTTP Catalog
| Caso de uso | p95 objetivo | p99 objetivo | Throughput objetivo | Error budget mensual |
|---|---|---|---|---|
| Search catalog | <= 320 ms | <= 520 ms | 240 rps | 0.5% |
| Product detail | <= 260 ms | <= 420 ms | 160 rps | 0.4% |
| List variants | <= 240 ms | <= 390 ms | 120 rps | 0.4% |
| Resolve variant for order | <= 180 ms | <= 300 ms | 100 rps | 0.3% |
| Create/update product | <= 420 ms | <= 650 ms | 25 rps | 0.6% |
| Create/update variant | <= 450 ms | <= 700 ms | 30 rps | 0.6% |
| Upsert current price | <= 350 ms | <= 550 ms | 45 rps | 0.5% |
| Bulk upsert prices | <= 1800 ms | <= 3000 ms | 5 rps | 0.8% |

## Budget async Catalog
| Flujo interno | p95 objetivo | p99 objetivo | Throughput objetivo | Error budget mensual |
|---|---|---|---|---|
| Consume `StockUpdated` | <= 180 ms | <= 320 ms | 120 eps | 0.3% |
| Consume `SkuReconciled` | <= 220 ms | <= 380 ms | 80 eps | 0.3% |
| Publish outbox a Kafka | <= 250 ms | <= 450 ms | 150 eps | 0.3% |

## Curva de latencia objetivo Catalog
```mermaid
xychart-beta
  title "Catalog p95 por familia HTTP"
  x-axis ["Search", "ProductDetail", "ListVariants", "ResolveVariant", "ProductCmd", "VariantCmd", "PriceUpsert", "BulkPrices"]
  y-axis "ms" 0 --> 3200
  bar [320, 260, 240, 180, 420, 450, 350, 1800]
```

## Modelo de carga Catalog (simulado)
| Escenario | Usuarios concurrentes | Mix de trafico Catalog |
|---|---|---|
| Normal semanal | 220 | 65% search, 15% detail, 8% list variants, 7% resolve variant, 5% admin |
| Pico comercial | 420 | 72% search, 14% detail, 6% list variants, 5% resolve variant, 3% admin |
| Campana de precio | 180 | 40% search, 15% detail, 10% resolve variant, 35% price upserts |

## Carga async operativa Catalog
| Flujo async | Volumen esperado | Perfil de carga |
|---|---|---|
| `inventory.stock-updated.v1` | 120 eps pico | bursts cortos por sincronizacion de disponibilidad |
| `inventory.sku-reconciled.v1` | 80 eps pico | rafagas por reconciliacion correctiva de SKU |
| relay de outbox Catalog | 150 eps pico | picos ligados a pricing masivo y cambios administrativos |

## Capacidad base y escalamiento
| Recurso por pod Catalog | Valor base | Escalamiento recomendado |
|---|---|---|
| CPU request/limit | `400m / 1500m` | HPA por CPU + p95 search |
| RAM request/limit | `768Mi / 1536Mi` | escalar por GC + latencia |
| R2DBC connection pool | 70 conexiones | max 160 con pooling adaptativo |
| Redis max connections | 120 | escalar con sharding de keys por tenant |
| Kafka producer batch | 32KB | 64KB en lotes de pricing |

## Politicas de degradacion controlada
- Prioridad 1: mantener `resolve variant` para checkout y `search` basico.
- Prioridad 2: mantener `product detail`.
- Prioridad 3: degradar facetas complejas y consultas de timeline largo.
- Prioridad 4: mantener consumo de eventos de Inventory y drenado de outbox con backlog acotado; si falla indexacion, priorizar consistencia de cache y reintento diferido.
- Si `p95 search > 520 ms` por 10 min:
  - activar cache extendida en respuestas de busqueda,
  - desactivar temporalmente `availabilityHint` en tiempo real,
  - limitar `size` maximo a 20.


## Modelo de fallos y degradacion runtime
| Tipo de fallo | Tratamiento de performance | Impacto en budget |
|---|---|---|
| rechazo funcional (`403/404/409/422`) | se atiende con salida rapida; no habilita degradacion global | no consume `error budget` de `5xx`; si eleva latencia por encima del objetivo si consume el presupuesto de latencia |
| dependencia lenta de lectura o enrich | activar cache extendida, limitar facetas o reducir enriquecimiento opcional | consume presupuesto de latencia mientras dure el incidente |
| fallo tecnico en persistencia/outbox | preservar consistencia local y diferir propagacion via outbox cuando corresponda | si termina en `5xx` consume `error budget`; si el outbox absorbe el fallo de propagacion no cuenta como error HTTP |
| backlog de listeners inventory | priorizar dedupe y actualizacion de cache/index con backoff controlado | consume presupuesto async mientras dure el atraso; no debe degradar `resolve variant` |
| evento duplicado | `noop idempotente` | no consume throughput util ni `error budget` operativo |

## Cuellos de botella esperados
| Bottleneck | Impacto | Mitigacion |
|---|---|---|
| Filtros por atributos de alta cardinalidad | latencia de search | indices por `attribute_code + normalized_value` |
| Joins product-variant-price | latencia de detalle | proyecciones optimizadas + cache |
| Cargas masivas de precio | lock contention | procesamiento por lotes pequenos + optimistic lock |
| Consumo de eventos inventory para hints | drift temporal en filtros | TTL corto + fallback informativo |
| Drenado de outbox en picos de pricing | atraso de publicacion | relay reactivo + tuning de batch y particionado por agregado |

## SLI/SLO Catalog alineados
| SLI | SLO |
|---|---|
| Disponibilidad `GET /catalog/search` | >= 99.8% mensual |
| Disponibilidad `POST /catalog/variants/resolve` | >= 99.9% mensual |
| p95 `GET /catalog/search` | <= 320 ms |
| p95 `POST /catalog/variants/resolve` | <= 180 ms |
| lag p95 `inventory.stock-updated.v1 -> applied` | <= 2 s |
| lag p95 `inventory.sku-reconciled.v1 -> applied` | <= 3 s |
| lag p95 `outbox persisted -> kafka ack` | <= 5 s |
| ratio de errores 5xx Catalog | <= 0.3% mensual |

## Riesgos y mitigaciones
- Riesgo: crecimiento no controlado de facetas por atributos libres.
  - Mitigacion: gobernanza de atributos por categoria + limites de filtros por request.
- Riesgo: picos por actualizaciones de precio desde cargas masivas.
  - Mitigacion: colas internas de procesamiento y publish asincrono via outbox.
