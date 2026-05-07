---
title: "Resumen"
linkTitle: "1. Resumen"
weight: 1
url: "/mvp/dominio/contextos-delimitados/pedidos/resumen/"
---

## Marco del contexto
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Resumen ejecutivo del bounded context `order`.

## Mision del contexto
_Responde: que verdad local gobierna este bounded context y para que existe dentro del dominio._
- Gestionar carrito, validacion de checkout y creacion de pedido en `PENDING_APPROVAL`.
- Gestionar confirmacion comercial explicita de pedido (`PENDING_APPROVAL -> CONFIRMED`) en el MVP.
- Registrar pago manual MVP y recalcular estado de pago agregado.

## Procesos y casos de uso cubiertos
_Responde: que procesos del MVP cubre este contexto y con que casos de uso contribuye._
| Proceso | Casos de uso | Resultado |
|---|---|---|
| P2 Compra recurrente | UC-ORD-01..UC-ORD-06 | pedido creado en `PENDING_APPROVAL` con snapshots y reservas confirmadas |
| P4 Ciclo de pedido | UC-ORD-07..UC-ORD-13 | estado de pedido auditable y consistente |
| P6 Recuperacion de carrito | UC-ORD-14, UC-ORD-15, UC-ORD-17 | ajustes por eventos y deteccion de abandono |
| P7 Pago manual | UC-ORD-09 | estado de pago recalculado sin duplicidad |
| P8 Reporte de ventas semanal | eventos de pedido/pago para `reporting` | base transaccional para consolidacion semanal de ventas |

## Responsabilidades
_Responde: que responsabilidades locales asume este contexto._
- Mantener carrito activo por usuario/organizacion.
- Orquestar validacion de checkout con `directory` e `inventory`, incluyendo politica operativa por pais.
- Crear pedido en `PENDING_APPROVAL` con correlacion idempotente y snapshots inmutables.
- Confirmar comercialmente pedido con transicion explicita a `CONFIRMED`.
- Gestionar transiciones de estado y timeline.
- Registrar pagos manuales y publicar eventos de pago.
- Emitir hechos de pedido/pago consumibles por `reporting` para FR-007.

## Limites (que NO hace)
_Responde: que queda explicitamente fuera del contexto para proteger sus fronteras._
- No autentica usuarios ni define permisos.
- No calcula disponibilidad (depende de `inventory`).
- No define vendible/precio (depende de `catalog`).
- No gobierna despacho/entrega como baseline de `MVP` (`READY_TO_DISPATCH`, `DISPATCHED`, `DELIVERED` quedan reservados).

## Dependencias externas
_Responde: de que otros contextos o contratos depende este contexto._
- `identity-access`, `directory`, `catalog`, `inventory`.
- `notification` y `reporting` como consumidores de eventos.

## FR/NFR relacionados
_Responde: que requisitos del producto aterrizan principalmente en este contexto._
- FR-004, FR-005, FR-006, FR-007, FR-008, FR-010, FR-011.
- NFR-004, NFR-005, NFR-006, NFR-011.

## Riesgos del contexto
_Responde: que riesgos locales existen y como se mitigan._
- Riesgo: carrera entre expiracion de reserva y confirmacion de checkout.
  - Mitigacion: validacion final + confirmacion de reservas por correlacion.
- Riesgo: ausencia de politica operativa por `countryCode` bloquea checkout.
  - Mitigacion: resolucion obligatoria en `directory` y error estable `configuracion_pais_no_disponible`.
- Riesgo: duplicidad de pago manual por reproceso humano.
  - Mitigacion: unicidad por `paymentReference` + `Idempotency-Key`.
