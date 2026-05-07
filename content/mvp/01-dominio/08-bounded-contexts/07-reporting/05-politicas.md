---
title: "Politicas"
linkTitle: "6. Politicas"
weight: 6
url: "/mvp/dominio/contextos-delimitados/reporteria/politicas/"
---

## Marco de politicas
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Politicas de reaccion de `reporting`.

## Politicas (if event -> then command)
_Responde: que reacciones automatizadas ejecuta el contexto frente a eventos relevantes._
| Trigger (evento) | Condicion | Accion (comando) | Observaciones |
|---|---|---|---|
| `OrderConfirmed` | evento valido | `refrescar_vista_ventas` | suma ventas confirmadas por periodo |
| `OrderPaymentRegistered` | evento valido | `refrescar_vista_ventas` | FR-010 actualiza estado de cobro |
| `OrderPaymentStatusUpdated` | evento valido | `refrescar_vista_ventas` | FR-010 recalcula metricas de cobro |
| `StockUpdated` | evento valido | `refrescar_vista_abastecimiento` | ajusta disponibilidad y riesgo |
| `StockReservationExpired` | evento valido | `refrescar_vista_abastecimiento` | refleja perdida de cobertura |
| `ProductUpdated` | evento valido | `refrescar_vista_ventas` | contexto comercial de dimension de catalogo |
| `PriceUpdated` | evento valido | `refrescar_vista_ventas` | contexto de revenue |
| `PriceScheduled` | evento valido | `refrescar_vista_ventas` | anticipa timeline comercial futuro sin mutar pricing core |
| `timer_fin_semana` | corte semanal alcanzado | `generar_reporte_semanal` | FR-003/FR-007 |
| `lag_consumo_alto` | backlog sobre umbral | `recalcular_vista_completa` | recuperacion de consistencia |

## Retries / compensacion
_Responde: como maneja este contexto reintentos y compensaciones sin romper su modelo._
- Retry de consumo de eventos: idempotente por `sourceEventId`.
- Compensacion: recalculo completo de vista cuando hay inconsistencia.

## Timeouts
_Responde: que limites temporales gobiernan las interacciones de este contexto._
- refresco de vista por lote: 3 s.
- generacion de reporte semanal: 15 min.
