---
title: "Modelo Logico"
linkTitle: "1. Modelo Logico"
weight: 1
url: "/mvp/arquitectura/servicios/servicio-inventario/datos/modelo-logico/"
---

## Proposito
Definir el modelo logico de datos de `inventory-service` para soportar stock, reservas TTL, idempotencia write-side y movimientos auditables sin violar invariantes del dominio.

## Alcance y fronteras
- Incluye entidades, relaciones, ownership y reglas de integridad logica de Inventory.
- Incluye relacion semantica con Catalog y Order por referencias opacas.
- Excluye DDL definitivo y optimizaciones fisicas de motor.

## Entidades logicas
| Entidad | Tipo | Descripcion | Ownership |
|---|---|---|---|
| `warehouse` | raiz operativa | almacen fisico habilitado para stock | Inventory |
| `stock_item` | agregado stock | estado de stock por `tenant+warehouse+sku` | Inventory |
| `stock_reservation` | agregado reserva | reserva temporal de stock para carrito/checkout | Inventory |
| `stock_movement` | ledger | registro auditable de mutaciones de stock | Inventory |
| `reservation_ledger` | soporte auditoria | trazabilidad granular de cambios de reserva | Inventory |
| `idempotency_record` | soporte idempotencia HTTP | deduplicacion write-side de mutaciones HTTP por `Idempotency-Key` | Inventory |
| `inventory_audit` | soporte seguridad | bitacora de acciones y rechazos | Inventory |
| `outbox_event` | soporte integracion | eventos pendientes/publicados | Inventory |
| `processed_event` | soporte idempotencia | control de eventos consumidos | Inventory |

## Relaciones logicas
- `warehouse 1..n stock_item`
- `stock_item 1..n stock_reservation`
- `stock_item 1..n stock_movement`
- `stock_reservation 1..n reservation_ledger`
- `stock_item 0..n outbox_event`
- `idempotency_record` no depende de FK dura a agregados; referencia operacion, recurso materializado y respuesta reutilizable.

## Reglas de integridad del modelo
| Regla | Expresion logica | Fuente |
|---|---|---|
| I-INV-01 | `stock_item.physical_qty >= 0` | `03-reglas-invariantes.md` |
| I-INV-02 | `stock_item.reserved_qty <= stock_item.physical_qty` | `03-reglas-invariantes.md` |
| RN-RES-01 | reserva activa solo si `qty <= available_qty` | `03-reglas-invariantes.md` |
| RN-RES-02 | no reserva parcial en MVP | `03-reglas-invariantes.md` |
| RN-ORD-01 | confirmacion de pedido exige reservas vigentes | `03-reglas-invariantes.md` |
| RN-IDEMP-01 | `tenantId + operationName + idempotencyKey` identifica una sola mutacion HTTP reutilizable | contrato API |

## Diagrama logico (ER)
```mermaid
erDiagram
  WAREHOUSE ||--o{ STOCK_ITEM : stores
  STOCK_ITEM ||--o{ STOCK_RESERVATION : reserves
  STOCK_ITEM ||--o{ STOCK_MOVEMENT : records
  STOCK_RESERVATION ||--o{ RESERVATION_LEDGER : tracks
  STOCK_ITEM ||--o{ OUTBOX_EVENT : emits
  STOCK_ITEM {
    uuid stock_id
    string tenant_id
    uuid warehouse_id
    string sku
    long physical_qty
    long reserved_qty
    long reorder_point
    string status
  }
  STOCK_RESERVATION {
    uuid reservation_id
    string tenant_id
    uuid stock_id
    string cart_id
    string order_id
    long qty
    string status
    datetime expires_at
  }
  IDEMPOTENCY_RECORD {
    uuid idempotency_record_id
    string tenant_id
    string operation_name
    string idempotency_key
    string request_hash
    string resource_type
    string resource_id
    int response_status
    datetime created_at
    datetime expires_at
  }
  STOCK_MOVEMENT {
    uuid movement_id
    string tenant_id
    uuid stock_id
    string sku
    string movement_type
    long qty
    string reason
    datetime created_at
  }
```

## Referencias cross-service (sin FK fisica)
| Referencia | Sistema propietario | Uso en Inventory |
|---|---|---|
| `sku` | Catalog | identificar variante vendible en inventario |
| `cartId` | Order | vincular reserva a carrito |
| `orderId` | Order | confirmar consumo de reserva |
| `tenantId` | IAM/Directory | aislamiento multi-tenant |

## Lecturas derivadas
- `availability = physical_qty - reserved_qty`
- `low_stock = availability <= reorder_point`
- `reservation_active = status=ACTIVE and now < expires_at`

## Riesgos y mitigaciones
- Riesgo: drift entre estado de SKU en Catalog y stock activo.
  - Mitigacion: reconciliacion por eventos `catalog.*` y marca `status=BLOCKED` cuando aplique.
- Riesgo: crecimiento rapido de `stock_movement` por alta rotacion.
  - Mitigacion: particion temporal y politicas de archivado en modelo fisico.
