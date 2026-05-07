---
title: "Anti-Corrupcion"
linkTitle: "8. Anti-Corrupcion"
weight: 8
url: "/mvp/dominio/contextos-delimitados/reporteria/anti-corrupcion/"
---

## Marco de traduccion
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Documentar ACLs semanticas de `reporting` con BCs productores.

## Mapeos principales
_Responde: que traducciones principales hace el contexto al cruzar sus fronteras._
| Upstream/Downstream | Termino externo | Termino en reporting | Regla |
|---|---|---|---|
| `order -> reporting` | `OrderConfirmed` | `fact_venta_confirmada` | revenue confirmado por pedido |
| `order -> reporting` | `OrderPaymentStatusUpdated` | `fact_cobro_actualizado` | no altera estado de pedido |
| `inventory -> reporting` | `StockUpdated` | `fact_stock` | disponibilidad para abastecimiento |
| `catalog -> reporting` | `ProductUpdated` | `fact_contexto_catalogo` | proyecciones comerciales se enriquecen sin mutar producto |
| `catalog -> reporting` | `PriceUpdated/PriceScheduled` | `fact_precio` | contexto comercial actual y futuro de venta |
| `notification -> reporting` | `NotificationFailed` | `fact_comunicacion_fallida` | KPI operativo de canal |

## Normalizacion de errores
_Responde: como traduce este contexto errores externos a su lenguaje canonico._
| Error tecnico | Error canonico de dominio |
|---|---|
| duplicate source event | `evento_duplicado` |
| invalid fact payload | `hecho_analitico_invalido` |
| report export timeout | `reporte_generacion_fallida` |

## Reglas ACL
_Responde: que reglas gobiernan la capa ACL para evitar contaminacion semantica._
- reporting consume hechos y no interpreta comandos fallidos como hechos de negocio.
- cualquier discrepancia se corrige desde BC core, no manualmente en vistas.
- consumers de reportes usan salida exportada, no tablas internas del BC.
