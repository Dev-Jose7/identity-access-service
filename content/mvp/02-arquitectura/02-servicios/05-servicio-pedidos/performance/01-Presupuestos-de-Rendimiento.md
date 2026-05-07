---
title: "Presupuestos de Rendimiento"
linkTitle: "1. Presupuestos de Rendimiento"
weight: 1
url: "/mvp/arquitectura/servicios/servicio-pedidos/rendimiento/presupuestos-rendimiento/"
---

## Proposito
Definir objetivos de performance/capacidad para `order-service`, con foco en carrito, checkout, confirmacion de pedidos y registro de pagos manuales.

## Alcance y fronteras
- Incluye presupuestos de latencia, throughput, concurrencia y degradacion.
- Incluye estimaciones iniciales de capacidad para entorno academico realista.
- Excluye resultados de pruebas de carga ejecutadas (fase 05-validacion).

## SLO tecnicos del servicio
| Operacion | p95 objetivo | p99 objetivo | Error budget mensual |
|---|---|---|---|
| upsert item carrito | <= 140 ms | <= 260 ms | 0.8% |
| validacion checkout | <= 200 ms | <= 350 ms | 0.8% |
| confirmar pedido | <= 350 ms | <= 600 ms | 0.5% |
| registrar pago manual | <= 180 ms | <= 320 ms | 0.5% |
| listar pedidos | <= 120 ms | <= 220 ms | 1.0% |
| obtener detalle pedido | <= 100 ms | <= 180 ms | 1.0% |

## Capacidad estimada MVP
| Dimension | Valor objetivo inicial |
|---|---|
| upserts carrito por segundo pico | 100 rps |
| validaciones checkout por segundo pico | 45 rps |
| confirmaciones checkout por segundo pico | 25 rps |
| transiciones de estado por segundo pico | 20 rps |
| registros de pago manual por segundo pico | 10 rps |
| crecimiento diario de lineas de pedido | 65k filas/dia |

## Modelo de carga simplificado
```mermaid
flowchart LR
  TRF["Trafico B2B"] --> CART["Cart writes 100 rps"]
  TRF --> CHKV["Checkout validation 45 rps"]
  TRF --> CHKC["Checkout confirm 25 rps"]
  OPS["Operador Arka"] --> PAY["Manual payment 10 rps"]

  CART --> DBW["writes cart/cart_item"]
  CHKV --> EXT["calls Directory+Inventory"]
  CHKC --> DBW2["writes order/order_line"]
  PAY --> DBW3["writes payment/history"]

  DBW --> OUT["outbox append"]
  DBW2 --> OUT
  DBW3 --> OUT
  OUT --> PUB["outbox publisher"]
```

## Presupuestos de recursos (referencial)
| Recurso | Baseline | Escalado recomendado |
|---|---|---|
| CPU pod order | 1.5 vCPU | HPA por `cpu>65%` o `checkout_rps` |
| Memoria pod order | 2 GiB | escalar a 3 GiB en picos |
| Conexiones DB | 40 | pool max 100 |
| Redis ops | 6k ops/s | cluster small + pipelining |
| Kafka produce rate | 2.5k msg/s | compresion snappy |

## Perfil de carga por ventana operativa
| Ventana | Duracion | Mix dominante | Presupuesto operativo |
|---|---|---|---|
| Base semanal | 06:00-18:00 local | 55% carrito, 25% checkout-validacion, 10% checkout-confirm, 10% consultas/pagos | p95 bajo objetivo en tabla SLO + error rate < 1% |
| Pico comercial | 18:00-22:00 local | 45% carrito, 30% checkout-validacion, 18% checkout-confirm, 7% pagos | tolera 3x baseline con degradacion p95 <= 30% (NFR-008) |
| Incidente dependencia externa | ventana variable | retries de checkout + aumento consultas de estado | preservar integridad de pedido, no crear pedido incompleto |

## Presupuesto de dependencia por flujo critico (checkout confirm)
| Paso | Dependencia | Timeout objetivo | Reintento permitido | Presupuesto de error |
|---|---|---|---|---|
| validar direccion | `directory-service` | 300 ms | 1 retry solo en timeout/transient | <= 0.3% |
| validar reservas | `inventory-service` | 350 ms | 1 retry solo en timeout/transient | <= 0.3% |
| confirmar reservas | `inventory-service` | 500 ms por lote | 0 retry en conflicto funcional | <= 0.2% |
| persistir pedido+lineas | PostgreSQL | 180 ms | retry por optimistic lock (max 2) | <= 0.2% |
| publicar evento | Kafka (via outbox) | async | scheduler reintenta hasta estado `PUBLISHED` | <= 0.1% sin perdida |


## Modelo de fallos y degradacion runtime
| Tipo de fallo | Tratamiento de performance | Impacto en budget |
|---|---|---|
| rechazo funcional (`403/404/409/422`) | se atiende con salida rapida; no habilita degradacion global | no consume `error budget` de `5xx`; si arrastra p95 por encima del objetivo si consume presupuesto de latencia |
| dependencia externa lenta en checkout | timeout acotado, retry solo en transient y cierre `conflicto_checkout_timeout` sin crear pedido incompleto | consume presupuesto de latencia; no debe transformarse en sobreventa ni pedido parcial |
| fallo tecnico de DB/Kafka/Redis | priorizar checkout, acumular outbox y reducir listados | consume presupuesto de latencia y, si termina en `5xx`, tambien `error budget` |
| evento duplicado | `noop idempotente` | no consume `error budget` operativo |

## Puntos de contencion esperados
| Punto | Riesgo | Mitigacion |
|---|---|---|
| confirmacion checkout | dependencia externa + escritura multiple | budget de timeout por dependencia + idempotencia |
| actualizacion de estado pedido | writes concurrentes por operadores/scheduler | optimistic locking + retry acotado |
| outbox publisher | atraso de eventos bajo rafagas | scheduler paralelo + tuning de batch |
| listado de pedidos | scans costosos por filtros amplios | indice compuesto + pagina maxima controlada |

## Politica de degradacion
- Si Redis falla: consultas continuan a DB con limite de throughput.
- Si Kafka falla: transaccion de negocio confirma y outbox acumula.
- Si Inventory/Directory presentan latencia alta: validacion checkout responde `conflicto_checkout_timeout` sin crear pedido.
- Si DB presenta latencia alta: reducir `size` maximo de listados y priorizar endpoints criticos de checkout.

## Indicadores de capacidad a monitorear
- `order.cart.upsert.p95`
- `order.checkout.validation.p95`
- `order.checkout.confirm.p95`
- `order.payment.register.p95`
- `order.outbox.pending.count`
- `order.checkout.conflict.rate`
- `order.db.lock.wait.ms`

## SLI/SLO operativos y alertas derivadas
| SLI | SLO operativo | Trigger de alerta | Severidad |
|---|---|---|---|
| `order.checkout.confirm.p95` | <= 350 ms por 10 min | > 500 ms por 10 min | alta |
| `order.checkout.validation.p95` | <= 200 ms por 10 min | > 320 ms por 10 min | media-alta |
| `order.outbox.pending.count` | < 5,000 sostenido | >= 5,000 por 5 min | alta |
| `order.checkout.conflict.rate` | <= 2.5% semanal | > 5% por 15 min | alta |
| `order.db.lock.wait.ms` | <= 120 ms p95 | > 250 ms por 10 min | alta |
| `order.payment.register.p95` | <= 180 ms por 10 min | > 300 ms por 10 min | media |

## Matriz de pruebas de carga y aceptacion
| Escenario | FR/NFR objetivo | Carga | Criterio de aceptacion |
|---|---|---|---|
| carrito intensivo | FR-004, FR-005, NFR-001 | 100 rps sostenido 30 min | p95 upsert <= 140 ms y errores < 0.8% |
| checkout pico | FR-004, NFR-001, NFR-004 | 45 validaciones + 25 confirmaciones rps por 20 min | p95 validacion <= 200 ms, p95 confirm <= 350 ms, sin sobreventa inducida |
| stress 3x baseline | NFR-008 | 3x trafico baseline por 15 min | degradacion p95 <= 30% y sin perdida de eventos core |
| degradacion broker | NFR-003, NFR-007 | kafka down 10 min con trafico normal | transacciones core completan; outbox acumula y drena al recuperar |
| degradacion DB parcial | NFR-001, NFR-003 | latencia DB inyectada +120 ms | prioriza checkout y mantiene errores controlados con rate-limit |

Runbooks de respuesta minima:
- `ORD-RB-01`: latencia alta en confirmacion checkout.
- `ORD-RB-03`: backlog de outbox elevado.
- `ORD-RB-05`: lag de scheduler de carrito abandonado.

## Riesgos y mitigaciones
- Riesgo: subestimar picos en eventos comerciales y generar conflictos de checkout.
  - Mitigacion: autoscaling por rps y prewarming de conexiones.
- Riesgo: crecimiento rapido del historico de estado y auditoria.
  - Mitigacion: particion mensual y archivado operacional.
