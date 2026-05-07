---
title: "Vista C4 Nivel 3"
linkTitle: "1. Vista C4 L3"
weight: 1
url: "/mvp/arquitectura/servicios/servicio-pedidos/arquitectura-interna/vista-c4-nivel-3/"
---

## Proposito
Definir la vista C4 de componente para `order-service`, detallando limites internos, dependencias y responsabilidades tecnicas para implementacion reactiva con Spring WebFlux.

## Alcance y fronteras
- Incluye componentes internos de Order en la vista componente: `Adapter-in`, `Application service`, `Domain`, `Adapter-out`.
- Incluye unicamente clases de implementacion representativas de cada componente para identificacion rapida.
- Excluye de la vista componente: DTOs, mappers, interfaces de puertos, utilidades y clases de configuracion.
- Incluye dependencias con `api-gateway-service`, `identity-access-service`, `directory-service`, `catalog-service`, `inventory-service`, `config-server`, `eureka-server`, `kafka-cluster`, `redis-cache` y `Order DB`.
- Excluye decisiones de codigo de otros servicios core.

## Rol del servicio en el sistema
`order-service` es la autoridad semantica de conversion compra-intencion a compra-compromiso:
- gestiona carrito activo por organizacion/usuario,
- mantiene consistencia de items con reservas,
- coordina checkout con validaciones de direccion, politica operativa por pais, precio y reservas,
- crea pedido con snapshots inmutables de direccion/precio,
- registra pago manual y recalcula estado de pago agregado,
- publica eventos de pedido/pago para Notification y Reporting.

## C4 componente del servicio
La vista de componentes se divide por caso de uso para reducir cruces visuales sin perder detalle estructural del servicio. Cada diagrama conserva los mismos grupos (`Adapter-in`, `Application service`, `Domain`, `Adapter-out`) y muestra solo las relaciones relevantes para ese flujo.

{{< tabs groupid="order-c4-l3" >}}
{{< tab title="GetActiveCart" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    CART_CTRL["CartHttpController"]
  end

  subgraph APP["Application service"]
    GET_CART_UC["GetActiveCartUseCase"]
  end

  subgraph DOM["Domain"]
    CART_AGG["CartAggregate"]
    TENANT_POLICY["TenantIsolationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    CART_REPO["CartR2dbcRepositoryAdapter"]
    CART_ITEM_REPO["CartItemR2dbcRepositoryAdapter"]
    ORDER_CACHE_ADP["OrderCacheRedisAdapter"]
  end

  CART_CTRL --> GET_CART_UC
  GET_CART_UC --> CART_AGG
  GET_CART_UC --> TENANT_POLICY
  GET_CART_UC --> CART_REPO
  GET_CART_UC --> CART_ITEM_REPO
  GET_CART_UC --> ORDER_CACHE_ADP

  CART_REPO --> ORDB["Order DB (PostgreSQL)"]
  CART_ITEM_REPO --> ORDB
  ORDER_CACHE_ADP --> REDIS["redis-cache"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="UpsertCartItem" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    CART_CTRL["CartHttpController"]
  end

  subgraph APP["Application service"]
    UPSERT_ITEM_UC["UpsertCartItemUseCase"]
  end

  subgraph DOM["Domain"]
    CART_AGG["CartAggregate"]
    TENANT_POLICY["TenantIsolationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    CART_REPO["CartR2dbcRepositoryAdapter"]
    CART_ITEM_REPO["CartItemR2dbcRepositoryAdapter"]
    CHECKOUT_ATTEMPT_REPO["CheckoutAttemptR2dbcRepositoryAdapter"]
    ORDER_AUDIT_REPO["OrderAuditR2dbcRepositoryAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
    CLAIMS_ADP["PrincipalContextAdapter"]
    PERM_ADP["RbacPermissionEvaluatorAdapter"]
    INVENTORY_CLIENT["InventoryReservationHttpClientAdapter"]
    CATALOG_CLIENT["CatalogVariantHttpClientAdapter"]
  end

  CART_CTRL --> UPSERT_ITEM_UC
  UPSERT_ITEM_UC --> CART_AGG
  UPSERT_ITEM_UC --> TENANT_POLICY
  UPSERT_ITEM_UC --> CLAIMS_ADP
  UPSERT_ITEM_UC --> PERM_ADP
  UPSERT_ITEM_UC --> CART_REPO
  UPSERT_ITEM_UC --> CART_ITEM_REPO
  UPSERT_ITEM_UC --> INVENTORY_CLIENT
  UPSERT_ITEM_UC --> CATALOG_CLIENT
  UPSERT_ITEM_UC --> CHECKOUT_ATTEMPT_REPO
  UPSERT_ITEM_UC --> ORDER_AUDIT_REPO
  UPSERT_ITEM_UC --> OUTBOX_ADP
  OUTBOX_ADP --> EVENT_PUB

  CART_REPO --> ORDB["Order DB (PostgreSQL)"]
  CART_ITEM_REPO --> ORDB
  CHECKOUT_ATTEMPT_REPO --> ORDB
  ORDER_AUDIT_REPO --> ORDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="RemoveCartItem" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    CART_CTRL["CartHttpController"]
  end

  subgraph APP["Application service"]
    REMOVE_ITEM_UC["RemoveCartItemUseCase"]
  end

  subgraph DOM["Domain"]
    CART_AGG["CartAggregate"]
    TENANT_POLICY["TenantIsolationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    CART_REPO["CartR2dbcRepositoryAdapter"]
    CART_ITEM_REPO["CartItemR2dbcRepositoryAdapter"]
    ORDER_AUDIT_REPO["OrderAuditR2dbcRepositoryAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
  end

  CART_CTRL --> REMOVE_ITEM_UC
  REMOVE_ITEM_UC --> CART_AGG
  REMOVE_ITEM_UC --> TENANT_POLICY
  REMOVE_ITEM_UC --> CART_REPO
  REMOVE_ITEM_UC --> CART_ITEM_REPO
  REMOVE_ITEM_UC --> ORDER_AUDIT_REPO
  REMOVE_ITEM_UC --> OUTBOX_ADP
  OUTBOX_ADP --> EVENT_PUB

  CART_REPO --> ORDB["Order DB (PostgreSQL)"]
  CART_ITEM_REPO --> ORDB
  ORDER_AUDIT_REPO --> ORDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="ClearCart" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    CART_CTRL["CartHttpController"]
  end

  subgraph APP["Application service"]
    CLEAR_CART_UC["ClearCartUseCase"]
  end

  subgraph DOM["Domain"]
    CART_AGG["CartAggregate"]
    TENANT_POLICY["TenantIsolationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    CART_REPO["CartR2dbcRepositoryAdapter"]
    CART_ITEM_REPO["CartItemR2dbcRepositoryAdapter"]
    ORDER_AUDIT_REPO["OrderAuditR2dbcRepositoryAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
  end

  CART_CTRL --> CLEAR_CART_UC
  CLEAR_CART_UC --> CART_AGG
  CLEAR_CART_UC --> TENANT_POLICY
  CLEAR_CART_UC --> CART_REPO
  CLEAR_CART_UC --> CART_ITEM_REPO
  CLEAR_CART_UC --> ORDER_AUDIT_REPO
  CLEAR_CART_UC --> OUTBOX_ADP
  OUTBOX_ADP --> EVENT_PUB

  CART_REPO --> ORDB["Order DB (PostgreSQL)"]
  CART_ITEM_REPO --> ORDB
  ORDER_AUDIT_REPO --> ORDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="RequestCheckoutValidation" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    CHECKOUT_CTRL["CheckoutHttpController"]
    DIR_EVENT_LISTENER["DirectoryCheckoutValidationEventListener"]
  end

  subgraph APP["Application service"]
    REQUEST_CHECKOUT_VALIDATION_UC["RequestCheckoutValidationUseCase"]
  end

  subgraph DOM["Domain"]
    CHECKOUT_POLICY["CheckoutPolicy"]
    REGIONAL_POLICY["RegionalOperationalPolicy"]
    TENANT_POLICY["TenantIsolationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    DIRECTORY_CLIENT["DirectoryCheckoutValidationHttpClientAdapter"]
    DIRECTORY_COUNTRY_POLICY_CLIENT["DirectoryOperationalCountryPolicyHttpClientAdapter"]
    INVENTORY_CLIENT["InventoryReservationHttpClientAdapter"]
    CATALOG_CLIENT["CatalogVariantHttpClientAdapter"]
    CHECKOUT_ATTEMPT_REPO["CheckoutAttemptR2dbcRepositoryAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
  end

  CHECKOUT_CTRL --> REQUEST_CHECKOUT_VALIDATION_UC
  DIR_EVENT_LISTENER --> REQUEST_CHECKOUT_VALIDATION_UC
  REQUEST_CHECKOUT_VALIDATION_UC --> CHECKOUT_POLICY
  REQUEST_CHECKOUT_VALIDATION_UC --> REGIONAL_POLICY
  REQUEST_CHECKOUT_VALIDATION_UC --> TENANT_POLICY
  REQUEST_CHECKOUT_VALIDATION_UC --> DIRECTORY_CLIENT
  REQUEST_CHECKOUT_VALIDATION_UC --> DIRECTORY_COUNTRY_POLICY_CLIENT
  REQUEST_CHECKOUT_VALIDATION_UC --> INVENTORY_CLIENT
  REQUEST_CHECKOUT_VALIDATION_UC --> CATALOG_CLIENT
  REQUEST_CHECKOUT_VALIDATION_UC --> CHECKOUT_ATTEMPT_REPO
  REQUEST_CHECKOUT_VALIDATION_UC --> OUTBOX_ADP
  OUTBOX_ADP --> EVENT_PUB

  CHECKOUT_ATTEMPT_REPO --> ORDB["Order DB (PostgreSQL)"]
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="ConfirmOrder" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    CHECKOUT_CTRL["CheckoutHttpController"]
  end

  subgraph APP["Application service"]
    CONFIRM_ORDER_UC["ConfirmOrderUseCase"]
  end

  subgraph DOM["Domain"]
    ORDER_AGG["OrderAggregate"]
    CHECKOUT_POLICY["CheckoutPolicy"]
    REGIONAL_POLICY["RegionalOperationalPolicy"]
    TENANT_POLICY["TenantIsolationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    CART_REPO["CartR2dbcRepositoryAdapter"]
    CART_ITEM_REPO["CartItemR2dbcRepositoryAdapter"]
    ORDER_REPO["PurchaseOrderR2dbcRepositoryAdapter"]
    ORDER_LINE_REPO["OrderLineR2dbcRepositoryAdapter"]
    CHECKOUT_ATTEMPT_REPO["CheckoutAttemptR2dbcRepositoryAdapter"]
    ORDER_STATUS_HISTORY_REPO["OrderStatusHistoryR2dbcRepositoryAdapter"]
    ORDER_AUDIT_REPO["OrderAuditR2dbcRepositoryAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
    CLAIMS_ADP["PrincipalContextAdapter"]
    PERM_ADP["RbacPermissionEvaluatorAdapter"]
    INVENTORY_CLIENT["InventoryReservationHttpClientAdapter"]
    DIRECTORY_COUNTRY_POLICY_CLIENT["DirectoryOperationalCountryPolicyHttpClientAdapter"]
  end

  CHECKOUT_CTRL --> CONFIRM_ORDER_UC
  CONFIRM_ORDER_UC --> ORDER_AGG
  CONFIRM_ORDER_UC --> CHECKOUT_POLICY
  CONFIRM_ORDER_UC --> REGIONAL_POLICY
  CONFIRM_ORDER_UC --> TENANT_POLICY
  CONFIRM_ORDER_UC --> CLAIMS_ADP
  CONFIRM_ORDER_UC --> PERM_ADP
  CONFIRM_ORDER_UC --> CART_REPO
  CONFIRM_ORDER_UC --> CART_ITEM_REPO
  CONFIRM_ORDER_UC --> ORDER_REPO
  CONFIRM_ORDER_UC --> ORDER_LINE_REPO
  CONFIRM_ORDER_UC --> INVENTORY_CLIENT
  CONFIRM_ORDER_UC --> DIRECTORY_COUNTRY_POLICY_CLIENT
  CONFIRM_ORDER_UC --> CHECKOUT_ATTEMPT_REPO
  CONFIRM_ORDER_UC --> ORDER_STATUS_HISTORY_REPO
  CONFIRM_ORDER_UC --> ORDER_AUDIT_REPO
  CONFIRM_ORDER_UC --> OUTBOX_ADP
  OUTBOX_ADP --> EVENT_PUB

  CART_REPO --> ORDB["Order DB (PostgreSQL)"]
  CART_ITEM_REPO --> ORDB
  ORDER_REPO --> ORDB
  ORDER_LINE_REPO --> ORDB
  CHECKOUT_ATTEMPT_REPO --> ORDB
  ORDER_STATUS_HISTORY_REPO --> ORDB
  ORDER_AUDIT_REPO --> ORDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="CancelOrder" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    ORDER_CMD_CTRL["OrderHttpController"]
  end

  subgraph APP["Application service"]
    CANCEL_ORDER_UC["CancelOrderUseCase"]
  end

  subgraph DOM["Domain"]
    ORDER_AGG["OrderAggregate"]
    TENANT_POLICY["TenantIsolationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    ORDER_REPO["PurchaseOrderR2dbcRepositoryAdapter"]
    ORDER_STATUS_HISTORY_REPO["OrderStatusHistoryR2dbcRepositoryAdapter"]
    ORDER_AUDIT_REPO["OrderAuditR2dbcRepositoryAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
    INVENTORY_CLIENT["InventoryReservationHttpClientAdapter"]
  end

  ORDER_CMD_CTRL --> CANCEL_ORDER_UC
  CANCEL_ORDER_UC --> ORDER_AGG
  CANCEL_ORDER_UC --> TENANT_POLICY
  CANCEL_ORDER_UC --> ORDER_REPO
  CANCEL_ORDER_UC --> ORDER_STATUS_HISTORY_REPO
  CANCEL_ORDER_UC --> INVENTORY_CLIENT
  CANCEL_ORDER_UC --> ORDER_AUDIT_REPO
  CANCEL_ORDER_UC --> OUTBOX_ADP
  OUTBOX_ADP --> EVENT_PUB

  ORDER_REPO --> ORDB["Order DB (PostgreSQL)"]
  ORDER_STATUS_HISTORY_REPO --> ORDB
  ORDER_AUDIT_REPO --> ORDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="RegisterManualPayment" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    PAYMENT_CTRL["OrderPaymentHttpController"]
  end

  subgraph APP["Application service"]
    REGISTER_PAYMENT_UC["RegisterManualPaymentUseCase"]
  end

  subgraph DOM["Domain"]
    PAYMENT_AGG["PaymentAggregate"]
    PAYMENT_STATUS_POLICY["PaymentStatusPolicy"]
    MANUAL_PAYMENT_SVC["ManualPaymentValidationService"]
    TENANT_POLICY["TenantIsolationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    ORDER_REPO["PurchaseOrderR2dbcRepositoryAdapter"]
    PAYMENT_REPO["PaymentRecordR2dbcRepositoryAdapter"]
    ORDER_STATUS_HISTORY_REPO["OrderStatusHistoryR2dbcRepositoryAdapter"]
    ORDER_AUDIT_REPO["OrderAuditR2dbcRepositoryAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
    CLAIMS_ADP["PrincipalContextAdapter"]
    PERM_ADP["RbacPermissionEvaluatorAdapter"]
  end

  PAYMENT_CTRL --> REGISTER_PAYMENT_UC
  REGISTER_PAYMENT_UC --> PAYMENT_AGG
  REGISTER_PAYMENT_UC --> PAYMENT_STATUS_POLICY
  REGISTER_PAYMENT_UC --> MANUAL_PAYMENT_SVC
  REGISTER_PAYMENT_UC --> TENANT_POLICY
  REGISTER_PAYMENT_UC --> CLAIMS_ADP
  REGISTER_PAYMENT_UC --> PERM_ADP
  REGISTER_PAYMENT_UC --> ORDER_REPO
  REGISTER_PAYMENT_UC --> PAYMENT_REPO
  REGISTER_PAYMENT_UC --> ORDER_STATUS_HISTORY_REPO
  REGISTER_PAYMENT_UC --> ORDER_AUDIT_REPO
  REGISTER_PAYMENT_UC --> OUTBOX_ADP
  OUTBOX_ADP --> EVENT_PUB

  ORDER_REPO --> ORDB["Order DB (PostgreSQL)"]
  PAYMENT_REPO --> ORDB
  ORDER_STATUS_HISTORY_REPO --> ORDB
  ORDER_AUDIT_REPO --> ORDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="UpdateOrderStatus" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    ORDER_CMD_CTRL["OrderHttpController"]
  end

  subgraph APP["Application service"]
    UPDATE_ORDER_STATUS_UC["UpdateOrderStatusUseCase"]
  end

  subgraph DOM["Domain"]
    ORDER_AGG["OrderAggregate"]
    ORDER_LIFECYCLE_POLICY["OrderLifecyclePolicy"]
    TENANT_POLICY["TenantIsolationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    ORDER_REPO["PurchaseOrderR2dbcRepositoryAdapter"]
    ORDER_STATUS_HISTORY_REPO["OrderStatusHistoryR2dbcRepositoryAdapter"]
    ORDER_AUDIT_REPO["OrderAuditR2dbcRepositoryAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
  end

  ORDER_CMD_CTRL --> UPDATE_ORDER_STATUS_UC
  UPDATE_ORDER_STATUS_UC --> ORDER_AGG
  UPDATE_ORDER_STATUS_UC --> ORDER_LIFECYCLE_POLICY
  UPDATE_ORDER_STATUS_UC --> TENANT_POLICY
  UPDATE_ORDER_STATUS_UC --> ORDER_REPO
  UPDATE_ORDER_STATUS_UC --> ORDER_STATUS_HISTORY_REPO
  UPDATE_ORDER_STATUS_UC --> ORDER_AUDIT_REPO
  UPDATE_ORDER_STATUS_UC --> OUTBOX_ADP
  OUTBOX_ADP --> EVENT_PUB

  ORDER_REPO --> ORDB["Order DB (PostgreSQL)"]
  ORDER_STATUS_HISTORY_REPO --> ORDB
  ORDER_AUDIT_REPO --> ORDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="ListOrders" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    ORDER_QUERY_CTRL["OrderQueryHttpController"]
  end

  subgraph APP["Application service"]
    LIST_ORDERS_UC["ListOrdersUseCase"]
  end

  subgraph DOM["Domain"]
    ORDER_AGG["OrderAggregate"]
    TENANT_POLICY["TenantIsolationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    ORDER_REPO["PurchaseOrderR2dbcRepositoryAdapter"]
    ORDER_CACHE_ADP["OrderCacheRedisAdapter"]
  end

  ORDER_QUERY_CTRL --> LIST_ORDERS_UC
  LIST_ORDERS_UC --> ORDER_AGG
  LIST_ORDERS_UC --> TENANT_POLICY
  LIST_ORDERS_UC --> ORDER_REPO
  LIST_ORDERS_UC --> ORDER_CACHE_ADP

  ORDER_REPO --> ORDB["Order DB (PostgreSQL)"]
  ORDER_CACHE_ADP --> REDIS["redis-cache"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="GetOrderDetail" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    ORDER_QUERY_CTRL["OrderQueryHttpController"]
  end

  subgraph APP["Application service"]
    GET_ORDER_DETAIL_UC["GetOrderDetailUseCase"]
  end

  subgraph DOM["Domain"]
    ORDER_AGG["OrderAggregate"]
    TENANT_POLICY["TenantIsolationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    ORDER_REPO["PurchaseOrderR2dbcRepositoryAdapter"]
    ORDER_LINE_REPO["OrderLineR2dbcRepositoryAdapter"]
    ORDER_CACHE_ADP["OrderCacheRedisAdapter"]
  end

  ORDER_QUERY_CTRL --> GET_ORDER_DETAIL_UC
  GET_ORDER_DETAIL_UC --> ORDER_AGG
  GET_ORDER_DETAIL_UC --> TENANT_POLICY
  GET_ORDER_DETAIL_UC --> ORDER_REPO
  GET_ORDER_DETAIL_UC --> ORDER_LINE_REPO
  GET_ORDER_DETAIL_UC --> ORDER_CACHE_ADP

  ORDER_REPO --> ORDB["Order DB (PostgreSQL)"]
  ORDER_LINE_REPO --> ORDB
  ORDER_CACHE_ADP --> REDIS["redis-cache"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="ListOrderPayments" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    ORDER_QUERY_CTRL["OrderQueryHttpController"]
  end

  subgraph APP["Application service"]
    LIST_ORDER_PAYMENTS_UC["ListOrderPaymentsUseCase"]
  end

  subgraph DOM["Domain"]
    PAYMENT_AGG["PaymentAggregate"]
    TENANT_POLICY["TenantIsolationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    PAYMENT_REPO["PaymentRecordR2dbcRepositoryAdapter"]
  end

  ORDER_QUERY_CTRL --> LIST_ORDER_PAYMENTS_UC
  LIST_ORDER_PAYMENTS_UC --> PAYMENT_AGG
  LIST_ORDER_PAYMENTS_UC --> TENANT_POLICY
  LIST_ORDER_PAYMENTS_UC --> PAYMENT_REPO

  PAYMENT_REPO --> ORDB["Order DB (PostgreSQL)"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="GetOrderTimeline" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    ORDER_QUERY_CTRL["OrderQueryHttpController"]
  end

  subgraph APP["Application service"]
    GET_ORDER_TIMELINE_UC["GetOrderTimelineUseCase"]
  end

  subgraph DOM["Domain"]
    ORDER_AGG["OrderAggregate"]
    ORDER_LIFECYCLE_POLICY["OrderLifecyclePolicy"]
    TENANT_POLICY["TenantIsolationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    ORDER_STATUS_HISTORY_REPO["OrderStatusHistoryR2dbcRepositoryAdapter"]
  end

  ORDER_QUERY_CTRL --> GET_ORDER_TIMELINE_UC
  GET_ORDER_TIMELINE_UC --> ORDER_AGG
  GET_ORDER_TIMELINE_UC --> ORDER_LIFECYCLE_POLICY
  GET_ORDER_TIMELINE_UC --> TENANT_POLICY
  GET_ORDER_TIMELINE_UC --> ORDER_STATUS_HISTORY_REPO

  ORDER_STATUS_HISTORY_REPO --> ORDB["Order DB (PostgreSQL)"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="HandleReservationExpired" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    INV_EVENT_LISTENER["InventoryReservationEventListener"]
  end

  subgraph APP["Application service"]
    HANDLE_RESERVATION_EXPIRED_UC["HandleReservationExpiredUseCase"]
  end

  subgraph DOM["Domain"]
    RESERVATION_POLICY["ReservationConsistencyPolicy"]
    CART_AGG["CartAggregate"]
  end

  subgraph OUT["Adapter-out"]
    CART_REPO["CartR2dbcRepositoryAdapter"]
    CART_ITEM_REPO["CartItemR2dbcRepositoryAdapter"]
    ORDER_AUDIT_REPO["OrderAuditR2dbcRepositoryAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
  end

  INV_EVENT_LISTENER --> HANDLE_RESERVATION_EXPIRED_UC
  HANDLE_RESERVATION_EXPIRED_UC --> RESERVATION_POLICY
  HANDLE_RESERVATION_EXPIRED_UC --> CART_AGG
  HANDLE_RESERVATION_EXPIRED_UC --> CART_REPO
  HANDLE_RESERVATION_EXPIRED_UC --> CART_ITEM_REPO
  HANDLE_RESERVATION_EXPIRED_UC --> ORDER_AUDIT_REPO
  HANDLE_RESERVATION_EXPIRED_UC --> OUTBOX_ADP
  OUTBOX_ADP --> EVENT_PUB

  CART_REPO --> ORDB["Order DB (PostgreSQL)"]
  CART_ITEM_REPO --> ORDB
  ORDER_AUDIT_REPO --> ORDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="HandleVariantDiscontinued" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    CAT_EVENT_LISTENER["CatalogVariantEventListener"]
  end

  subgraph APP["Application service"]
    HANDLE_VARIANT_DISCONTINUED_UC["HandleVariantDiscontinuedUseCase"]
  end

  subgraph DOM["Domain"]
    CART_AGG["CartAggregate"]
    RESERVATION_POLICY["ReservationConsistencyPolicy"]
  end

  subgraph OUT["Adapter-out"]
    CART_ITEM_REPO["CartItemR2dbcRepositoryAdapter"]
    ORDER_AUDIT_REPO["OrderAuditR2dbcRepositoryAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
  end

  CAT_EVENT_LISTENER --> HANDLE_VARIANT_DISCONTINUED_UC
  HANDLE_VARIANT_DISCONTINUED_UC --> CART_AGG
  HANDLE_VARIANT_DISCONTINUED_UC --> RESERVATION_POLICY
  HANDLE_VARIANT_DISCONTINUED_UC --> CART_ITEM_REPO
  HANDLE_VARIANT_DISCONTINUED_UC --> ORDER_AUDIT_REPO
  HANDLE_VARIANT_DISCONTINUED_UC --> OUTBOX_ADP
  OUTBOX_ADP --> EVENT_PUB

  CART_ITEM_REPO --> ORDB["Order DB (PostgreSQL)"]
  ORDER_AUDIT_REPO --> ORDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="HandleUserBlocked" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    IAM_EVENT_LISTENER["IamUserBlockedEventListener"]
  end

  subgraph APP["Application service"]
    HANDLE_USER_BLOCKED_UC["HandleUserBlockedUseCase"]
  end

  subgraph DOM["Domain"]
    ORDER_AGG["OrderAggregate"]
    ORDER_LIFECYCLE_POLICY["OrderLifecyclePolicy"]
    TENANT_POLICY["TenantIsolationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    ORDER_REPO["PurchaseOrderR2dbcRepositoryAdapter"]
    ORDER_STATUS_HISTORY_REPO["OrderStatusHistoryR2dbcRepositoryAdapter"]
    ORDER_AUDIT_REPO["OrderAuditR2dbcRepositoryAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
  end

  IAM_EVENT_LISTENER --> HANDLE_USER_BLOCKED_UC
  HANDLE_USER_BLOCKED_UC --> ORDER_AGG
  HANDLE_USER_BLOCKED_UC --> ORDER_LIFECYCLE_POLICY
  HANDLE_USER_BLOCKED_UC --> TENANT_POLICY
  HANDLE_USER_BLOCKED_UC --> ORDER_REPO
  HANDLE_USER_BLOCKED_UC --> ORDER_STATUS_HISTORY_REPO
  HANDLE_USER_BLOCKED_UC --> ORDER_AUDIT_REPO
  HANDLE_USER_BLOCKED_UC --> OUTBOX_ADP
  OUTBOX_ADP --> EVENT_PUB

  ORDER_REPO --> ORDB["Order DB (PostgreSQL)"]
  ORDER_STATUS_HISTORY_REPO --> ORDB
  ORDER_AUDIT_REPO --> ORDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="DetectAbandonedCarts" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    ABANDONED_SCH["AbandonedCartSchedulerListener"]
  end

  subgraph APP["Application service"]
    DETECT_ABANDONED_CARTS_UC["DetectAbandonedCartsUseCase"]
  end

  subgraph DOM["Domain"]
    CART_ABANDONMENT_POLICY["CartAbandonmentPolicy"]
    CART_AGG["CartAggregate"]
  end

  subgraph OUT["Adapter-out"]
    CART_REPO["CartR2dbcRepositoryAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
    CLOCK_ADP["SystemClockAdapter"]
  end

  ABANDONED_SCH --> DETECT_ABANDONED_CARTS_UC
  DETECT_ABANDONED_CARTS_UC --> CART_ABANDONMENT_POLICY
  DETECT_ABANDONED_CARTS_UC --> CART_AGG
  DETECT_ABANDONED_CARTS_UC --> CART_REPO
  DETECT_ABANDONED_CARTS_UC --> OUTBOX_ADP
  DETECT_ABANDONED_CARTS_UC --> CLOCK_ADP
  OUTBOX_ADP --> EVENT_PUB

  CART_REPO --> ORDB["Order DB (PostgreSQL)"]
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< /tabs >}}

## Componentes base por capa (vista componente)
| Capa | Clases base | Responsabilidad tecnica |
|---|---|---|
| `Adapter-in` | `CartHttpController`, `CheckoutHttpController`, `OrderHttpController`, `OrderPaymentHttpController`, `OrderQueryHttpController`, `InventoryReservationEventListener`, `CatalogVariantEventListener`, `DirectoryCheckoutValidationEventListener`, `IamUserBlockedEventListener`, `AbandonedCartSchedulerListener`, `TriggerContextResolver`, `TriggerContext` | Recibir HTTP/eventos/scheduler, validar entrada y traducir a casos de uso |
| `Application service` | `GetActiveCartUseCase`, `UpsertCartItemUseCase`, `RemoveCartItemUseCase`, `ClearCartUseCase`, `RequestCheckoutValidationUseCase`, `ConfirmOrderUseCase`, `CancelOrderUseCase`, `RegisterManualPaymentUseCase`, `UpdateOrderStatusUseCase`, `ListOrdersUseCase`, `GetOrderDetailUseCase`, `ListOrderPaymentsUseCase`, `GetOrderTimelineUseCase`, `HandleReservationExpiredUseCase`, `HandleVariantDiscontinuedUseCase`, `HandleUserBlockedUseCase`, `DetectAbandonedCartsUseCase` | Orquestar carrito/checkout/pedido/pago con idempotencia, ACLs, dedupe de eventos y consistencia semantica |
| `Domain` | `CartAggregate`, `OrderAggregate`, `PaymentAggregate`, `PaymentRecord`, `CheckoutPolicy`, `OrderLifecyclePolicy`, `PaymentStatusPolicy`, `CartAbandonmentPolicy`, `TenantIsolationPolicy`, `ReservationConsistencyPolicy`, `RegionalOperationalPolicy`, `ManualPaymentValidationService` | Mantener invariantes de confirmacion, transiciones de estado, estado de pago agregado y reglas regionales por pais |
| `Adapter-out` | `CartR2dbcRepositoryAdapter`, `CartItemR2dbcRepositoryAdapter`, `PurchaseOrderR2dbcRepositoryAdapter`, `OrderLineR2dbcRepositoryAdapter`, `PaymentRecordR2dbcRepositoryAdapter`, `CheckoutAttemptR2dbcRepositoryAdapter`, `OrderStatusHistoryR2dbcRepositoryAdapter`, `OrderAuditR2dbcRepositoryAdapter`, `OutboxPersistenceAdapter`, `KafkaDomainEventPublisherAdapter`, `OrderCacheRedisAdapter`, `PrincipalContextAdapter`, `RbacPermissionEvaluatorAdapter`, `InventoryReservationHttpClientAdapter`, `DirectoryCheckoutValidationHttpClientAdapter`, `DirectoryOperationalCountryPolicyHttpClientAdapter`, `CatalogVariantHttpClientAdapter`, `SystemClockAdapter` | Conectar DB, seguridad, cache, broker y dependencias core externas |

## Nota de modelado
- Esta vista componente no detalla estructura de carpetas.
- Esta vista componente lista solo implementaciones de `Adapter-in`, `Application service`, `Domain` y `Adapter-out`.
- DTOs/mappers/interfaces/config se detallan en la vista de codigo.
- El detalle de paquetes/codigo se mantiene en:
  - `02-Vista-de-Codigo.md`

## Dependencias externas permitidas
| Dependencia | Tipo | Uso en Order | Criticidad |
|---|---|---|---|
| `api-gateway-service` | plataforma | entrada principal de trafico B2B y backoffice | alta |
| `identity-access-service` | core | autenticacion y claims de tenant/rol | critica |
| `directory-service` | core | validacion de direccion/organizacion y resolucion de parametros operativos por pais en checkout | alta |
| `catalog-service` | core | validacion semantica de variante vendible y precio snapshot | alta |
| `inventory-service` | core | reservar, confirmar y liberar reservas | critica |
| `Order DB (PostgreSQL)` | datos | fuente de verdad de carrito/pedido/pago | critica |
| `redis-cache` | soporte | cache de consultas de detalle/listado de pedidos | media |
| `kafka-cluster` | soporte | publicacion/consumo de eventos de pedido/pago/carrito | alta |
| `config-server` | plataforma | configuracion centralizada de TTL, reglas y toggles | alta |
| `eureka-server` | plataforma | service discovery | media |

## Modelo de autenticacion y autorizacion runtime
| Flujo | Autenticacion | Autorizacion y legitimidad |
|---|---|---|
| HTTP command/query | `api-gateway-service` autentica el JWT y solo enruta requests confiables. | `order-service` materializa `PrincipalContext` con `PrincipalContextPort`/`PrincipalContextAdapter`, valida permiso base con `PermissionEvaluatorPort` y `RbacPermissionEvaluatorAdapter`, y cierra tenant, organizacion, ownership y reglas del pedido con `TenantIsolationPolicy`, `CheckoutPolicy`, `OrderLifecyclePolicy`, `PaymentStatusPolicy`, `ReservationConsistencyPolicy` y `ManualPaymentValidationService`. |
| eventos / scheduler | No depende de JWT de usuario. | `InventoryReservationEventListener`, `CatalogVariantEventListener`, `DirectoryCheckoutValidationEventListener`, `IamUserBlockedEventListener` y `AbandonedCartSchedulerListener` materializan `TriggerContext` mediante `TriggerContextResolver`, validan `tenant`, dedupe y legitimidad del trigger antes de tocar carrito, pedido o pago. |


## Modelo de errores y excepciones runtime
| Responsabilidad | Componentes | Aplicacion |
|---|---|---|
| Decision semantica | `Application service`, `Domain service`, `TenantIsolationPolicy`, `CheckoutPolicy`, `OrderLifecyclePolicy`, `PaymentStatusPolicy`, `ReservationConsistencyPolicy`, `ManualPaymentValidationService` | Los casos de Order expresan rechazo temprano y rechazo de decision mediante familias canonicas de acceso/contexto (`ApplicationException`, `AuthorizationDeniedException`, `TenantIsolationException`, `ResourceNotFoundException`) y de decision (`DomainException`, `DomainRuleViolationException`, `ConflictException`) sin filtrar errores tecnicos al cliente o trigger. |
| Cierre HTTP | `CartHttpController`, `CheckoutHttpController`, `OrderHttpController`, `OrderPaymentHttpController`, `OrderQueryHttpController`, `OrderWebFluxConfiguration` | El adapter-in HTTP traduce la familia semantica o tecnica a un envelope canonico con `errorCode`, `category`, `traceId`, `correlationId` y `timestamp`. |
| Cierre async | `InventoryReservationEventListener`, `CatalogVariantEventListener`, `DirectoryCheckoutValidationEventListener`, `IamUserBlockedEventListener`, `AbandonedCartSchedulerListener`, `ProcessedEventR2dbcRepositoryAdapter` | Los flujos event-driven o scheduler tratan duplicados como `noop idempotente`, distinguen fallos retryable/no-retryable y cierran la incidencia por reintento, DLQ o auditoria operativa. |

## Soporte de observabilidad
| Elemento | Componentes principales | Funcion arquitectonica |
|---|---|---|
| Configuracion de metricas y trazas | `OrderObservabilityConfiguration` | Expone la configuracion base para instrumentacion transversal del servicio y puente de trazas/metricas. |
| Auditoria operativa de pedidos | `OrderAuditPort`, `OrderAuditR2dbcRepositoryAdapter`, `ReactiveOrderAuditRepository` | Registran evidencia tecnica de checkout, confirmacion, cancelacion, pagos, eventos compensatorios y mutaciones de carrito/pedido. |
| Emision de eventos observables | `OutboxPersistenceAdapter`, `OutboxPublisherScheduler`, `KafkaDomainEventPublisherAdapter` | Materializan y publican eventos de orden hacia Kafka para integracion y trazabilidad near-real-time. |

Nota:
- Esta vista solo documenta los componentes que habilitan observabilidad dentro de la arquitectura.
- La definicion detallada de metricas, logs, trazas, alertas y dashboards corresponde al pilar de calidad u operacion.

## Canales de eventos (naming canonico)
Convencion aplicada: `<bc>.<event-name>.v<major>`.

| Tipo | Evento | Topic canonico |
|---|---|---|
| Emitido | `CartItemAdded` | `order.cart-item-added.v1` |
| Emitido | `CartItemUpdated` | `order.cart-item-updated.v1` |
| Emitido | `CartItemRemoved` | `order.cart-item-removed.v1` |
| Emitido | `CartCleared` | `order.cart-cleared.v1` |
| Emitido | `OrderCheckoutValidationFailed` | `order.checkout-validation-failed.v1` |
| Emitido | `OrderCreated` | `order.order-created.v1` |
| Emitido | `OrderConfirmed` | `order.order-confirmed.v1` |
| Emitido | `OrderStatusChanged` | `order.order-status-changed.v1` |
| Emitido | `OrderCancelled` | `order.order-cancelled.v1` |
| Emitido | `OrderPaymentRegistered` | `order.payment-registered.v1` |
| Emitido | `OrderPaymentStatusUpdated` | `order.payment-status-updated.v1` |
| Emitido | `CartAbandonedDetected` | `order.cart-abandoned-detected.v1` |
| Consumido | `StockReservationExpired` | `inventory.stock-reservation-expired.v1` |
| Consumido | `StockReservationConfirmed` | `inventory.stock-reservation-confirmed.v1` |
| Consumido | `VariantDiscontinued` | `catalog.variant-discontinued.v1` |
| Consumido | `PriceUpdated` | `catalog.price-updated.v1` |
| Consumido | `CheckoutAddressValidated` | `directory.checkout-address-validated.v1` |
| Consumido | `UserBlocked` | `iam.user-blocked.v1` |

## Mapa de procesos -> componentes clave
| Proceso | Componentes principales | Invariantes protegidos |
|---|---|---|
| Gestionar carrito y reserva | `CartHttpController`, `UpsertCartItemUseCase`, `CartAggregate`, `InventoryReservationHttpClientAdapter` | no stock negativo indirecto, no cruce tenant |
| Validar checkout | `CheckoutHttpController`, `RequestCheckoutValidationUseCase`, `DirectoryCheckoutValidationHttpClientAdapter`, `DirectoryOperationalCountryPolicyHttpClientAdapter`, `InventoryReservationHttpClientAdapter` | direccion valida + reservas vigentes + politica regional vigente |
| Crear pedido | `ConfirmOrderUseCase`, `OrderAggregate`, `PaymentAggregate`, `OutboxPersistenceAdapter` | `I-ORD-01`, `I-ORD-02`, entrada a `PENDING_APPROVAL` + snapshot inmutable |
| Confirmar aprobacion comercial | `UpdateOrderStatusUseCase`, `OrderAggregate`, `OrderLifecyclePolicy`, `OutboxPersistenceAdapter` | `I-ORD-01`, `I-ORD-02`, transicion `PENDING_APPROVAL -> CONFIRMED` |
| Gestionar ciclo de estado | `UpdateOrderStatusUseCase`, `OrderLifecyclePolicy`, `OrderStatusHistoryR2dbcRepositoryAdapter` | transiciones validas y auditables |
| Registrar pago manual | `OrderPaymentHttpController`, `RegisterManualPaymentUseCase`, `PaymentAggregate`, `PaymentRecord` | `RN-PAY-01`, `RN-PAY-02`, `I-PAY-01` |
| Cancelar pedidos por usuario bloqueado | `IamUserBlockedEventListener`, `HandleUserBlockedUseCase`, `OrderAggregate`, `OrderLifecyclePolicy` | cancelacion idempotente de pedidos no terminales por evento IAM |
| Detectar carrito abandonado | `AbandonedCartSchedulerListener`, `DetectAbandonedCartsUseCase`, `CartAbandonmentPolicy` | idempotencia de deteccion y no duplicidad de evento |

## Matriz de dependencias sync y fallback
| Dependencia | Uso critico | Timeout objetivo | Fallback definido | Error semantico |
|---|---|---|---|---|
| `identity-access-service` | authz tenant/rol en mutaciones | 200-300 ms | denegar mutacion y auditar | `acceso_cruzado_detectado` / `unauthorized` |
| `directory-service` | validacion de direccion checkout + resolucion de politica regional por pais | 300 ms | reintento acotado + estado INVALID en `checkout_attempt` | `conflicto_checkout` / `configuracion_pais_no_disponible` |
| `catalog-service` | variante vendible + precio vigente | 300 ms | no confirmar checkout, refrescar carrito | `variante_no_vendible` / `conflicto_checkout` |
| `inventory-service` | crear/confirmar/liberar reserva | 300-500 ms | abortar confirmacion, conservar carrito consistente | `reserva_expirada` / `conflicto_checkout` |
| `kafka-cluster` | publicacion de eventos de negocio | async | outbox pending + publisher scheduler | no impacta transaccion core |

## Matriz de ownership operativo
| Tema | Componente owner en Order | Evidencia |
|---|---|---|
| Idempotencia comandos mutantes | `IdempotencyRepositoryPort`, `IdempotencyR2dbcRepositoryAdapter`, `CheckoutAttemptR2dbcRepositoryAdapter` | `contracts/01-APIs.md`, `architecture/03-Casos-de-Uso-en-Ejecucion.md` |
| Integridad de transiciones de pedido | `OrderLifecyclePolicy` | `domain.model.order` en code-view |
| Integridad de estado de pago | `PaymentStatusPolicy` | `domain.model.payment` en code-view |
| Publicacion confiable de eventos | `OutboxPersistenceAdapter` + `OutboxPublisherScheduler` | `contracts/02-Eventos.md` |
| Trazabilidad y auditoria | `OrderAuditR2dbcRepositoryAdapter` | `security/01-Arquitectura-de-Seguridad.md` |

## Matriz de observabilidad por componente
| Componente | Senales minimas | Uso operativo |
|---|---|---|
| `UpsertCartItemUseCase` | `order_cart_upsert_total`, `order_cart_upsert_latency_ms` | detectar degradacion de carrito y validaciones de reserva |
| `RequestCheckoutValidationUseCase` | `order_checkout_validation_latency_ms`, `order_checkout_conflict_total` | identificar conflicto temprano de checkout |
| `ConfirmOrderUseCase` | `order_checkout_confirm_latency_ms`, `order_checkout_confirm_total` | controlar conversion de carrito a pedido |
| `RegisterManualPaymentUseCase` | `order_manual_payment_register_total`, `order_manual_payment_duplicate_total` | detectar riesgo de pago duplicado/fraude operativo |
| `UpdateOrderStatusUseCase` | `order_state_transition_total` | seguimiento de ciclo de pedido y transiciones invalidas |
| `OutboxPublisherScheduler` | `order_outbox_pending_count`, `order_outbox_publish_retry_total` | controlar backlog y salud de publicacion EDA |
| `DetectAbandonedCartsUseCase` | `order_abandoned_cart_detection_lag_seconds` | asegurar oportunidad de recuperacion comercial (FR-008) |

## Restricciones de diseno
- `MUST`: pedido se confirma solo con reservas vigentes/confirmables para todas sus lineas (`I-ORD-01`).
- `MUST`: el pedido nace por la secuencia interna `CREATED -> PENDING_APPROVAL` en `MVP`; la confirmacion comercial ocurre despues en `PENDING_APPROVAL -> CONFIRMED`.
- `MUST`: transiciones de estado de pedido siguen maquina valida (`I-ORD-02`).
- `MUST`: estado de pago agregado deriva de pagos validos vs total pedido (`I-PAY-01`).
- `MUST`: checkout requiere parametros operativos por pais vigentes (`FR-011`, `NFR-011`).
- `MUST`: operaciones mutantes de carrito/checkout/pago usan `Idempotency-Key`.
- `MUST`: eventos de negocio se publican via outbox transaccional.
- `SHOULD`: fallo de Notification no revierte pedido ni pago (`RN-NOTI-01`).

## Riesgos y trade-offs
- Riesgo: carrera entre expiracion de reservas y confirmacion de checkout.
  - Mitigacion: confirmacion atomica por lote de reservas + idempotencia por `checkoutCorrelationId`.
- Riesgo: divergencia semantica entre precio mostrado y precio confirmado.
  - Mitigacion: congelar `priceSnapshot` en linea al confirmar pedido.
- Trade-off: mantener carrito/pedido/pago en un BC reduce latencia transaccional, pero aumenta volumen interno del servicio.
