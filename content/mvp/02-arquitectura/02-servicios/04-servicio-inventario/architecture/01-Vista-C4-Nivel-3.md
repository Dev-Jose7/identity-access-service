---
title: "Vista C4 Nivel 3"
linkTitle: "1. Vista C4 L3"
weight: 1
url: "/mvp/arquitectura/servicios/servicio-inventario/arquitectura-interna/vista-c4-nivel-3/"
---

## Proposito
Definir la vista C4 de componente para `inventory-service`, detallando limites internos, dependencias y responsabilidades tecnicas para implementacion reactiva con Spring WebFlux.

## Alcance y fronteras
- Incluye componentes internos de Inventory en la vista componente: `Adapter-in`, `Application service`, `Domain`, `Adapter-out`.
- Incluye unicamente clases de implementacion representativas de cada componente para identificacion rapida.
- Excluye de la vista componente: DTOs, mappers, interfaces de puertos, utilidades y clases de configuracion.
- Incluye dependencias con `api-gateway-service`, `order-service`, `catalog-service`, `config-server`, `eureka-server`, `kafka-cluster`, `redis-cache` y `Inventory DB`.
- Excluye decisiones de codigo de otros servicios core.

## Rol del servicio en el sistema
`inventory-service` es la autoridad semantica de disponibilidad reservable:
- administra stock fisico por SKU/almacen,
- crea/extiende/confirma/libera/expira reservas con TTL,
- registra movimientos auditable de inventario,
- provee validacion de reservas para checkout,
- emite hechos de inventario para Order, Catalog, Notification y Reporting.

## C4 componente del servicio
La vista de componentes se divide por caso de uso para reducir cruces visuales sin perder detalle estructural del servicio. Cada diagrama conserva los mismos grupos (`Adapter-in`, `Application service`, `Domain`, `Adapter-out`) y muestra solo las relaciones relevantes para ese flujo.

{{< tabs groupid="inventory-c4-l3" >}}
{{< tab title="InitializeStock" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    STOCK_CMD_CTRL["InventoryStockCommandHttpController"]
  end

  subgraph APP["Application service"]
    INIT_STOCK_UC["InitializeStockUseCase"]
  end

  subgraph DOM["Domain"]
    STOCK_AGG["StockAggregate"]
  end

  subgraph OUT["Adapter-out"]
    WAREHOUSE_REPO["WarehouseR2dbcRepositoryAdapter"]
    STOCK_REPO["StockItemR2dbcRepositoryAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
  end

  STOCK_CMD_CTRL --> INIT_STOCK_UC
  INIT_STOCK_UC --> STOCK_AGG
  INIT_STOCK_UC --> STOCK_REPO
  INIT_STOCK_UC --> WAREHOUSE_REPO
  INIT_STOCK_UC --> OUTBOX_ADP
  OUTBOX_ADP --> EVENT_PUB

  WAREHOUSE_REPO --> INVDB["Inventory DB (PostgreSQL)"]
  STOCK_REPO --> INVDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="AdjustStock" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    STOCK_CMD_CTRL["InventoryStockCommandHttpController"]
  end

  subgraph APP["Application service"]
    ADJUST_STOCK_UC["AdjustStockUseCase"]
  end

  subgraph DOM["Domain"]
    STOCK_AGG["StockAggregate"]
    MOVEMENT_AGG["MovementAggregate"]
    MOVEMENT_POLICY["MovementPolicy"]
  end

  subgraph OUT["Adapter-out"]
    STOCK_REPO["StockItemR2dbcRepositoryAdapter"]
    MOVEMENT_REPO["StockMovementR2dbcRepositoryAdapter"]
    AUDIT_REPO["InventoryAuditR2dbcRepositoryAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
    CLOCK_ADP["SystemClockAdapter"]
  end

  STOCK_CMD_CTRL --> ADJUST_STOCK_UC
  ADJUST_STOCK_UC --> STOCK_AGG
  ADJUST_STOCK_UC --> MOVEMENT_AGG
  ADJUST_STOCK_UC --> MOVEMENT_POLICY
  ADJUST_STOCK_UC --> STOCK_REPO
  ADJUST_STOCK_UC --> MOVEMENT_REPO
  ADJUST_STOCK_UC --> AUDIT_REPO
  ADJUST_STOCK_UC --> OUTBOX_ADP
  ADJUST_STOCK_UC --> CLOCK_ADP
  OUTBOX_ADP --> EVENT_PUB

  STOCK_REPO --> INVDB["Inventory DB (PostgreSQL)"]
  MOVEMENT_REPO --> INVDB
  AUDIT_REPO --> INVDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="IncreaseStock" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    STOCK_CMD_CTRL["InventoryStockCommandHttpController"]
  end

  subgraph APP["Application service"]
    INCREASE_STOCK_UC["IncreaseStockUseCase"]
  end

  subgraph DOM["Domain"]
    STOCK_AGG["StockAggregate"]
    MOVEMENT_AGG["MovementAggregate"]
  end

  subgraph OUT["Adapter-out"]
    STOCK_REPO["StockItemR2dbcRepositoryAdapter"]
    MOVEMENT_REPO["StockMovementR2dbcRepositoryAdapter"]
  end

  STOCK_CMD_CTRL --> INCREASE_STOCK_UC
  INCREASE_STOCK_UC --> STOCK_AGG
  INCREASE_STOCK_UC --> MOVEMENT_AGG
  INCREASE_STOCK_UC --> STOCK_REPO
  INCREASE_STOCK_UC --> MOVEMENT_REPO

  STOCK_REPO --> INVDB["Inventory DB (PostgreSQL)"]
  MOVEMENT_REPO --> INVDB
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="DecreaseStock" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    STOCK_CMD_CTRL["InventoryStockCommandHttpController"]
  end

  subgraph APP["Application service"]
    DECREASE_STOCK_UC["DecreaseStockUseCase"]
  end

  subgraph DOM["Domain"]
    STOCK_AGG["StockAggregate"]
    MOVEMENT_AGG["MovementAggregate"]
  end

  subgraph OUT["Adapter-out"]
    STOCK_REPO["StockItemR2dbcRepositoryAdapter"]
    MOVEMENT_REPO["StockMovementR2dbcRepositoryAdapter"]
  end

  STOCK_CMD_CTRL --> DECREASE_STOCK_UC
  DECREASE_STOCK_UC --> STOCK_AGG
  DECREASE_STOCK_UC --> MOVEMENT_AGG
  DECREASE_STOCK_UC --> STOCK_REPO
  DECREASE_STOCK_UC --> MOVEMENT_REPO

  STOCK_REPO --> INVDB["Inventory DB (PostgreSQL)"]
  MOVEMENT_REPO --> INVDB
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="BulkAdjustStock" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    STOCK_CMD_CTRL["InventoryStockCommandHttpController"]
  end

  subgraph APP["Application service"]
    BULK_ADJUST_UC["BulkAdjustStockUseCase"]
  end

  subgraph DOM["Domain"]
    STOCK_AGG["StockAggregate"]
    MOVEMENT_AGG["MovementAggregate"]
  end

  subgraph OUT["Adapter-out"]
    STOCK_REPO["StockItemR2dbcRepositoryAdapter"]
    MOVEMENT_REPO["StockMovementR2dbcRepositoryAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
  end

  STOCK_CMD_CTRL --> BULK_ADJUST_UC
  BULK_ADJUST_UC --> STOCK_AGG
  BULK_ADJUST_UC --> MOVEMENT_AGG
  BULK_ADJUST_UC --> STOCK_REPO
  BULK_ADJUST_UC --> MOVEMENT_REPO
  BULK_ADJUST_UC --> OUTBOX_ADP
  OUTBOX_ADP --> EVENT_PUB

  STOCK_REPO --> INVDB["Inventory DB (PostgreSQL)"]
  MOVEMENT_REPO --> INVDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="CreateReservation" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    RESERVATION_CTRL["InventoryReservationHttpController"]
  end

  subgraph APP["Application service"]
    CREATE_RESERVATION_UC["CreateReservationUseCase"]
  end

  subgraph DOM["Domain"]
    RESERVATION_AGG["ReservationAggregate"]
    STOCK_POLICY["StockAvailabilityPolicy"]
    RESERVATION_POLICY["ReservationLifecyclePolicy"]
    OVERSELL_GUARD["OversellGuardService"]
  end

  subgraph OUT["Adapter-out"]
    STOCK_REPO["StockItemR2dbcRepositoryAdapter"]
    RESERVATION_REPO["StockReservationR2dbcRepositoryAdapter"]
    AUDIT_REPO["InventoryAuditR2dbcRepositoryAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
    CLAIMS_ADP["PrincipalContextAdapter"]
    PERM_ADP["RbacPermissionEvaluatorAdapter"]
    LOCK_ADP["DistributedLockRedisAdapter"]
  end

  RESERVATION_CTRL --> CREATE_RESERVATION_UC
  CREATE_RESERVATION_UC --> RESERVATION_AGG
  CREATE_RESERVATION_UC --> STOCK_POLICY
  CREATE_RESERVATION_UC --> RESERVATION_POLICY
  CREATE_RESERVATION_UC --> OVERSELL_GUARD
  CREATE_RESERVATION_UC --> STOCK_REPO
  CREATE_RESERVATION_UC --> RESERVATION_REPO
  CREATE_RESERVATION_UC --> LOCK_ADP
  CREATE_RESERVATION_UC --> AUDIT_REPO
  CREATE_RESERVATION_UC --> OUTBOX_ADP
  CREATE_RESERVATION_UC --> CLAIMS_ADP
  CREATE_RESERVATION_UC --> PERM_ADP
  OUTBOX_ADP --> EVENT_PUB

  STOCK_REPO --> INVDB["Inventory DB (PostgreSQL)"]
  RESERVATION_REPO --> INVDB
  AUDIT_REPO --> INVDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="ExtendReservation" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    RESERVATION_CTRL["InventoryReservationHttpController"]
  end

  subgraph APP["Application service"]
    EXTEND_RESERVATION_UC["ExtendReservationUseCase"]
  end

  subgraph DOM["Domain"]
    RESERVATION_AGG["ReservationAggregate"]
  end

  subgraph OUT["Adapter-out"]
    RESERVATION_REPO["StockReservationR2dbcRepositoryAdapter"]
  end

  RESERVATION_CTRL --> EXTEND_RESERVATION_UC
  EXTEND_RESERVATION_UC --> RESERVATION_AGG
  EXTEND_RESERVATION_UC --> RESERVATION_REPO

  RESERVATION_REPO --> INVDB["Inventory DB (PostgreSQL)"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="ConfirmReservation" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    RESERVATION_CTRL["InventoryReservationHttpController"]
  end

  subgraph APP["Application service"]
    CONFIRM_RESERVATION_UC["ConfirmReservationUseCase"]
  end

  subgraph DOM["Domain"]
    RESERVATION_AGG["ReservationAggregate"]
  end

  subgraph OUT["Adapter-out"]
    RESERVATION_REPO["StockReservationR2dbcRepositoryAdapter"]
    STOCK_REPO["StockItemR2dbcRepositoryAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
  end

  RESERVATION_CTRL --> CONFIRM_RESERVATION_UC
  CONFIRM_RESERVATION_UC --> RESERVATION_AGG
  CONFIRM_RESERVATION_UC --> RESERVATION_REPO
  CONFIRM_RESERVATION_UC --> STOCK_REPO
  CONFIRM_RESERVATION_UC --> OUTBOX_ADP
  OUTBOX_ADP --> EVENT_PUB

  RESERVATION_REPO --> INVDB["Inventory DB (PostgreSQL)"]
  STOCK_REPO --> INVDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="ReleaseReservation" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    RESERVATION_CTRL["InventoryReservationHttpController"]
  end

  subgraph APP["Application service"]
    RELEASE_RESERVATION_UC["ReleaseReservationUseCase"]
  end

  subgraph DOM["Domain"]
    RESERVATION_AGG["ReservationAggregate"]
  end

  subgraph OUT["Adapter-out"]
    RESERVATION_REPO["StockReservationR2dbcRepositoryAdapter"]
    STOCK_REPO["StockItemR2dbcRepositoryAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
  end

  RESERVATION_CTRL --> RELEASE_RESERVATION_UC
  RELEASE_RESERVATION_UC --> RESERVATION_AGG
  RELEASE_RESERVATION_UC --> RESERVATION_REPO
  RELEASE_RESERVATION_UC --> STOCK_REPO
  RELEASE_RESERVATION_UC --> OUTBOX_ADP
  OUTBOX_ADP --> EVENT_PUB

  RESERVATION_REPO --> INVDB["Inventory DB (PostgreSQL)"]
  STOCK_REPO --> INVDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="ExpireReservation" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    RESERVATION_CTRL["InventoryReservationHttpController"]
    EXPIRY_LISTENER["ReservationExpirySchedulerListener"]
  end

  subgraph APP["Application service"]
    EXPIRE_RESERVATION_UC["ExpireReservationUseCase"]
  end

  subgraph DOM["Domain"]
    RESERVATION_AGG["ReservationAggregate"]
    EXPIRY_POLICY["ReservationExpiryPolicy"]
  end

  subgraph OUT["Adapter-out"]
    RESERVATION_REPO["StockReservationR2dbcRepositoryAdapter"]
    STOCK_REPO["StockItemR2dbcRepositoryAdapter"]
    MOVEMENT_REPO["StockMovementR2dbcRepositoryAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
    CLOCK_ADP["SystemClockAdapter"]
  end

  RESERVATION_CTRL --> EXPIRE_RESERVATION_UC
  EXPIRY_LISTENER --> EXPIRE_RESERVATION_UC
  EXPIRE_RESERVATION_UC --> RESERVATION_AGG
  EXPIRE_RESERVATION_UC --> EXPIRY_POLICY
  EXPIRE_RESERVATION_UC --> RESERVATION_REPO
  EXPIRE_RESERVATION_UC --> STOCK_REPO
  EXPIRE_RESERVATION_UC --> MOVEMENT_REPO
  EXPIRE_RESERVATION_UC --> OUTBOX_ADP
  EXPIRE_RESERVATION_UC --> CLOCK_ADP
  OUTBOX_ADP --> EVENT_PUB

  RESERVATION_REPO --> INVDB["Inventory DB (PostgreSQL)"]
  STOCK_REPO --> INVDB
  MOVEMENT_REPO --> INVDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="ReconcileSku" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    CATALOG_LISTENER["CatalogVariantEventListener"]
  end

  subgraph APP["Application service"]
    RECONCILE_SKU_UC["ReconcileSkuUseCase"]
  end

  subgraph DOM["Domain"]
    STOCK_AGG["StockAggregate"]
  end

  subgraph OUT["Adapter-out"]
    STOCK_REPO["StockItemR2dbcRepositoryAdapter"]
    CATALOG_CLIENT["CatalogVariantHttpClientAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
  end

  CATALOG_LISTENER --> RECONCILE_SKU_UC
  RECONCILE_SKU_UC --> STOCK_AGG
  RECONCILE_SKU_UC --> CATALOG_CLIENT
  RECONCILE_SKU_UC --> STOCK_REPO
  RECONCILE_SKU_UC --> OUTBOX_ADP
  OUTBOX_ADP --> EVENT_PUB

  STOCK_REPO --> INVDB["Inventory DB (PostgreSQL)"]
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="GetAvailability" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    STOCK_QUERY_CTRL["InventoryStockQueryHttpController"]
  end

  subgraph APP["Application service"]
    GET_AVAILABILITY_UC["GetAvailabilityUseCase"]
  end

  subgraph DOM["Domain"]
    STOCK_AGG["StockAggregate"]
    STOCK_POLICY["StockAvailabilityPolicy"]
  end

  subgraph OUT["Adapter-out"]
    STOCK_REPO["StockItemR2dbcRepositoryAdapter"]
    CACHE_ADP["InventoryCacheRedisAdapter"]
  end

  STOCK_QUERY_CTRL --> GET_AVAILABILITY_UC
  GET_AVAILABILITY_UC --> STOCK_AGG
  GET_AVAILABILITY_UC --> STOCK_POLICY
  GET_AVAILABILITY_UC --> STOCK_REPO
  GET_AVAILABILITY_UC --> CACHE_ADP

  STOCK_REPO --> INVDB["Inventory DB (PostgreSQL)"]
  CACHE_ADP --> REDIS["redis-cache"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="GetStockBySku" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    STOCK_QUERY_CTRL["InventoryStockQueryHttpController"]
  end

  subgraph APP["Application service"]
    GET_STOCK_BY_SKU_UC["GetStockBySkuUseCase"]
  end

  subgraph DOM["Domain"]
    STOCK_AGG["StockAggregate"]
  end

  subgraph OUT["Adapter-out"]
    STOCK_REPO["StockItemR2dbcRepositoryAdapter"]
  end

  STOCK_QUERY_CTRL --> GET_STOCK_BY_SKU_UC
  GET_STOCK_BY_SKU_UC --> STOCK_AGG
  GET_STOCK_BY_SKU_UC --> STOCK_REPO

  STOCK_REPO --> INVDB["Inventory DB (PostgreSQL)"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="ListWarehouseStock" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    STOCK_QUERY_CTRL["InventoryStockQueryHttpController"]
  end

  subgraph APP["Application service"]
    LIST_WAREHOUSE_STOCK_UC["ListWarehouseStockUseCase"]
  end

  subgraph DOM["Domain"]
    STOCK_AGG["StockAggregate"]
  end

  subgraph OUT["Adapter-out"]
    STOCK_REPO["StockItemR2dbcRepositoryAdapter"]
  end

  STOCK_QUERY_CTRL --> LIST_WAREHOUSE_STOCK_UC
  LIST_WAREHOUSE_STOCK_UC --> STOCK_AGG
  LIST_WAREHOUSE_STOCK_UC --> STOCK_REPO

  STOCK_REPO --> INVDB["Inventory DB (PostgreSQL)"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="ListReservationTimeline" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    STOCK_QUERY_CTRL["InventoryStockQueryHttpController"]
  end

  subgraph APP["Application service"]
    LIST_RESERVATION_TIMELINE_UC["ListReservationTimelineUseCase"]
  end

  subgraph DOM["Domain"]
    RESERVATION_AGG["ReservationAggregate"]
  end

  subgraph OUT["Adapter-out"]
    RESERVATION_REPO["StockReservationR2dbcRepositoryAdapter"]
  end

  STOCK_QUERY_CTRL --> LIST_RESERVATION_TIMELINE_UC
  LIST_RESERVATION_TIMELINE_UC --> RESERVATION_AGG
  LIST_RESERVATION_TIMELINE_UC --> RESERVATION_REPO

  RESERVATION_REPO --> INVDB["Inventory DB (PostgreSQL)"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="ValidateReservationForCheckout" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    VALIDATION_CTRL["InventoryValidationHttpController"]
  end

  subgraph APP["Application service"]
    VALIDATE_CHECKOUT_UC["ValidateReservationForCheckoutUseCase"]
  end

  subgraph DOM["Domain"]
    RESERVATION_AGG["ReservationAggregate"]
    TENANT_POLICY["TenantIsolationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    RESERVATION_REPO["StockReservationR2dbcRepositoryAdapter"]
  end

  VALIDATION_CTRL --> VALIDATE_CHECKOUT_UC
  VALIDATE_CHECKOUT_UC --> RESERVATION_AGG
  VALIDATE_CHECKOUT_UC --> TENANT_POLICY
  VALIDATE_CHECKOUT_UC --> RESERVATION_REPO

  RESERVATION_REPO --> INVDB["Inventory DB (PostgreSQL)"]
{{< /mermaid >}}
{{< /tab >}}
{{< /tabs >}}

## Componentes base por capa (vista componente)
| Capa | Clases base | Responsabilidad tecnica |
|---|---|---|
| `Adapter-in` | `InventoryStockCommandHttpController`, `InventoryReservationHttpController`, `InventoryStockQueryHttpController`, `InventoryValidationHttpController`, `CatalogVariantEventListener`, `ReservationExpirySchedulerListener`, `TriggerContextResolver` | Recibir HTTP/eventos/scheduler, validar entrada y traducir a casos de uso |
| `Application service` | `InitializeStockUseCase`, `AdjustStockUseCase`, `IncreaseStockUseCase`, `DecreaseStockUseCase`, `CreateReservationUseCase`, `ExtendReservationUseCase`, `ConfirmReservationUseCase`, `ReleaseReservationUseCase`, `ExpireReservationUseCase`, `ReconcileSkuUseCase`, `BulkAdjustStockUseCase`, `GetAvailabilityUseCase`, `GetStockBySkuUseCase`, `ListWarehouseStockUseCase`, `ListReservationTimelineUseCase`, `ValidateReservationForCheckoutUseCase` | Orquestar casos de uso, idempotencia, control de concurrencia y consistencia semantica de stock/reservas |
| `Domain` | `StockAggregate`, `ReservationAggregate`, `MovementAggregate`, `StockAvailabilityPolicy`, `ReservationLifecyclePolicy`, `MovementPolicy`, `ReservationExpiryPolicy`, `TenantIsolationPolicy`, `OversellGuardService` | Mantener invariantes de inventario, TTL de reservas y ledger auditable |
| `Adapter-out` | `WarehouseR2dbcRepositoryAdapter`, `StockItemR2dbcRepositoryAdapter`, `StockReservationR2dbcRepositoryAdapter`, `StockMovementR2dbcRepositoryAdapter`, `InventoryAuditR2dbcRepositoryAdapter`, `IdempotencyR2dbcRepositoryAdapter`, `ProcessedEventR2dbcRepositoryAdapter`, `OutboxPersistenceAdapter`, `KafkaDomainEventPublisherAdapter`, `InventoryCacheRedisAdapter`, `PrincipalContextAdapter`, `RbacPermissionEvaluatorAdapter`, `DistributedLockRedisAdapter`, `CatalogVariantHttpClientAdapter`, `SystemClockAdapter` | Conectar con DB, cache, broker, seguridad, locking distribuido y dependencias externas |

## Nota de modelado
- Esta vista componente no detalla estructura de carpetas.
- Esta vista componente lista solo implementaciones de `Adapter-in`, `Application service`, `Domain` y `Adapter-out`.
- `request/response`, `command/query/result`, mappers, interfaces y config se detallan en la vista de codigo.
- El detalle de paquetes/codigo se mantiene en:
  - [02-Vista-de-Codigo.md](/Users/jose/Development/Documentation/arkab2b-docs/content/mvp/02-arquitectura/services/inventory-service/architecture/02-Vista-de-Codigo.md)
  - [03-Casos-de-Uso-en-Ejecucion.md](/Users/jose/Development/Documentation/arkab2b-docs/content/mvp/02-arquitectura/services/inventory-service/architecture/03-Casos-de-Uso-en-Ejecucion.md)

## Dependencias externas permitidas
| Dependencia | Tipo | Uso en Inventory | Criticidad |
|---|---|---|---|
| `api-gateway-service` | plataforma | Entrada principal de trafico web privada e integraciones internas | alta |
| `Inventory DB (PostgreSQL)` | datos | Fuente de verdad de stock, reservas y movimientos | critica |
| `redis-cache` | soporte | Cache de consultas de disponibilidad y locking distribuido | alta |
| `kafka-cluster` | soporte | Publicacion/consumo de eventos de inventario | alta |
| `catalog-service` | core | Validar vigencia semantica de SKU y estado vendible | media |
| `order-service` | core | Validacion de reservas para checkout | alta |
| `config-server` | plataforma | Config centralizada de TTL, umbrales y flags | alta |
| `eureka-server` | plataforma | service discovery | media |

## Modelo de autenticacion y autorizacion runtime
| Flujo | Autenticacion | Autorizacion y legitimidad |
|---|---|---|
| HTTP command/query | `api-gateway-service` autentica el JWT y solo enruta requests confiables. | `inventory-service` materializa `PrincipalContext` con `PrincipalContextPort`/`PrincipalContextAdapter`, valida permiso base con `PermissionEvaluatorPort` y `RbacPermissionEvaluatorAdapter`, y cierra la autorizacion contextual con `TenantIsolationPolicy`, `StockAvailabilityPolicy`, `ReservationLifecyclePolicy`, `MovementPolicy` y `OversellGuardService`. |
| eventos / scheduler | No depende de JWT de usuario. | `CatalogVariantEventListener` y `ReservationExpirySchedulerListener` materializan `TriggerContext` mediante `TriggerContextResolver`, validan `tenant`, dedupe, trigger y politica operativa antes de ajustar stock o reservas. |


## Modelo de errores y excepciones runtime
| Responsabilidad | Componentes | Aplicacion |
|---|---|---|
| Decision semantica | `Application service`, `Domain service`, `TenantIsolationPolicy`, `StockAvailabilityPolicy`, `ReservationLifecyclePolicy`, `MovementPolicy`, `OversellGuardService` | Los casos de Inventory expresan rechazo temprano y rechazo de decision mediante familias canonicas de acceso/contexto (`ApplicationException`, `AuthorizationDeniedException`, `TenantIsolationException`, `ResourceNotFoundException`) y de decision (`DomainException`, `DomainRuleViolationException`, `ConflictException`) sin filtrar errores tecnicos al cliente o trigger. |
| Cierre HTTP | `InventoryStockCommandHttpController`, `InventoryReservationHttpController`, `InventoryStockQueryHttpController`, `InventoryValidationHttpController`, `InventoryWebFluxConfiguration` | El adapter-in HTTP traduce la familia semantica o tecnica a un envelope canonico con `errorCode`, `category`, `traceId`, `correlationId` y `timestamp`. |
| Cierre async | `CatalogVariantEventListener`, `ReservationExpirySchedulerListener`, `ProcessedEventR2dbcRepositoryAdapter` | Los flujos event-driven o scheduler tratan duplicados como `noop idempotente`, distinguen fallos retryable/no-retryable y cierran la incidencia por reintento, DLQ o auditoria operativa. |

## Soporte de observabilidad
| Elemento | Componentes principales | Funcion arquitectonica |
|---|---|---|
| Configuracion de metricas y trazas | `InventoryObservabilityConfiguration` | Expone la configuracion base para instrumentacion transversal del servicio y puente de trazas/metricas. |
| Auditoria operativa de inventario | `InventoryAuditPort`, `InventoryAuditR2dbcRepositoryAdapter`, `ReactiveInventoryAuditRepository` | Registran evidencia tecnica de ajustes, reservas, confirmaciones, liberaciones y reconciliaciones de stock. |
| Emision de eventos observables | `OutboxPersistenceAdapter`, `OutboxPublisherScheduler`, `KafkaDomainEventPublisherAdapter` | Materializan y publican eventos de inventario hacia Kafka para integracion y trazabilidad near-real-time. |

Nota:
- Esta vista solo documenta los componentes que habilitan observabilidad dentro de la arquitectura.
- La definicion detallada de metricas, logs, trazas, alertas y dashboards corresponde al pilar de calidad u operacion.

## Canales de eventos (naming canonico)
Convencion aplicada: `<bc>.<event-name>.v<major>`.

| Tipo | Evento | Topic canonico |
|---|---|---|
| Emitido | `StockInitialized` | `inventory.stock-initialized.v1` |
| Emitido | `StockAdjusted` | `inventory.stock-adjusted.v1` |
| Emitido | `StockIncreased` | `inventory.stock-increased.v1` |
| Emitido | `StockDecreased` | `inventory.stock-decreased.v1` |
| Emitido | `StockUpdated` | `inventory.stock-updated.v1` |
| Emitido | `StockReserved` | `inventory.stock-reserved.v1` |
| Emitido | `StockReservationExtended` | `inventory.stock-reservation-extended.v1` |
| Emitido | `StockReservationConfirmed` | `inventory.stock-reservation-confirmed.v1` |
| Emitido | `StockReservationReleased` | `inventory.stock-reservation-released.v1` |
| Emitido | `StockReservationExpired` | `inventory.stock-reservation-expired.v1` |
| Emitido | `SkuReconciled` | `inventory.sku-reconciled.v1` |
| Emitido | `LowStockDetected` | `inventory.low-stock-detected.v1` |
| Consumido | `VariantCreated` | `catalog.variant-created.v1` |
| Consumido | `VariantUpdated` | `catalog.variant-updated.v1` |
| Consumido | `VariantDiscontinued` | `catalog.variant-discontinued.v1` |
| Consumido | `ProductRetired` | `catalog.product-retired.v1` |

## Restricciones de diseno
- `MUST`: `stock_fisico` no puede quedar negativo (`I-INV-01`).
- `MUST`: `reservas_activas <= disponibilidad` (`I-INV-02`).
- `MUST`: reserva MVP es todo-o-nada (`RN-RES-02`).
- `MUST`: operaciones mutantes usan `Idempotency-Key`.
- `MUST`: `inventory-service` no muta `pedido` ni `precio`.
- `SHOULD`: operaciones de reserva/confirmacion aplican locking por `tenantId+sku+warehouseId` para minimizar carrera concurrente.

## Riesgos y trade-offs
- Riesgo: contencion alta por concurrencia de reservas en SKU caliente.
  - Mitigacion: locking granular por clave, retries acotados y aislamiento optimista.
- Riesgo: deriva entre Catalog e Inventory en ciclo de vida de SKU.
  - Mitigacion: reconciliacion por eventos `catalog.*` + runbook de correccion.
- Trade-off: ledger completo aumenta costo de escritura, pero habilita auditoria y analitica de abastecimiento.
