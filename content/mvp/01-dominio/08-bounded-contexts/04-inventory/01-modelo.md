---
title: "Modelo"
linkTitle: "2. Modelo"
weight: 2
url: "/mvp/dominio/contextos-delimitados/inventario/modelo/"
---

## Marco del modelo
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Modelo conceptual del BC `inventory`.

## Entidades principales
_Responde: que entidades estructuran el modelo local._
- Almacen.
- StockItem.
- Reserva.
- MovimientoStock.
- LedgerReserva.

## Value objects principales
_Responde: que objetos de valor expresan reglas relevantes sin identidad propia._
- `SkuRef`.
- `Quantity`.
- `ReservationTTL`.
- `WarehouseRef`.
- `ReorderPoint`.

## Estados importantes
_Responde: que estados son relevantes para entender el ciclo local._
| Entidad | Estados permitidos | Inicial | Terminales |
|---|---|---|---|
| StockItem | `ACTIVE`, `BLOCKED` | `ACTIVE` | `BLOCKED` |
| Reserva | `ACTIVE`, `CONFIRMED`, `EXPIRED`, `RELEASED` | `ACTIVE` | `CONFIRMED`, `EXPIRED`, `RELEASED` |
| MovimientoStock | `APPLIED` | `APPLIED` | `APPLIED` |

## Reglas de negocio nucleo
_Responde: que reglas de negocio sostienen el modelo del contexto._
- `I-INV-01`: `stock_fisico >= 0` siempre.
- `I-INV-02`: `reservas_activas <= stock_fisico`.
- `RN-RES-01`: reserva solo si `qty <= disponibilidad`.
- `RN-RES-02`: reserva todo-o-nada en MVP.

## Lecturas derivadas
_Responde: que lecturas o vistas derivadas usa este contexto sin convertirlas en verdad transaccional._
- `disponibilidad = stock_fisico - reservas_activas`.
- `low_stock = disponibilidad <= reorder_point`.
- `reserva_activa = status=ACTIVE && now < expiresAt`.
