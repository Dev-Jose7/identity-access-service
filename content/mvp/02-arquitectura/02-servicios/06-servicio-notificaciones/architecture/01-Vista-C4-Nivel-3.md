---
title: "Vista C4 Nivel 3"
linkTitle: "1. Vista C4 L3"
weight: 1
url: "/mvp/arquitectura/servicios/servicio-notificaciones/arquitectura-interna/vista-c4-nivel-3/"
---

## Proposito
Definir la vista C4 de componente para `notification-service`, detallando limites internos, dependencias y responsabilidades tecnicas para comunicaciones no bloqueantes derivadas de hechos de negocio.

## Alcance y fronteras
- Incluye componentes internos de Notification en la vista componente: `Adapter-in`, `Application service`, `Domain`, `Adapter-out`.
- Incluye clases de implementacion representativas para identificacion rapida del servicio.
- Excluye DTOs, mappers, interfaces de puertos y clases de configuracion.
- Incluye dependencias con `api-gateway-service`, `identity-access-service`, `directory-service`, `order-service`, `inventory-service`, `reporting-service`, `kafka-cluster`, `redis-cache` y `Notification DB`.
- Excluye decisiones de codigo de servicios core externos.

## Rol del servicio en el sistema
`notification-service` es la autoridad semantica de comunicacion asincrona no bloqueante:
- registra solicitudes de notificacion a partir de eventos de negocio,
- selecciona canal/plantilla por politica de tenant,
- ejecuta envio a proveedor externo con control de reintentos,
- descarta solicitudes no recuperables con trazabilidad,
- publica eventos de resultado (`NotificationRequested`, `NotificationSent`, `NotificationFailed`, `NotificationDiscarded`) para Reporting y seguimiento operativo.

## C4 componente del servicio
La vista de componentes se divide por caso de uso para reducir cruces visuales sin perder detalle estructural del servicio. Cada diagrama conserva los mismos grupos (`Adapter-in`, `Application service`, `Domain`, `Adapter-out`) y muestra solo las relaciones relevantes para ese flujo.

{{< tabs groupid="notification-c4-l3" >}}
{{< tab title="RequestNotification" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    ORDER_LISTENER["OrderEventListener"]
    INV_LISTENER["InventoryEventListener"]
    REP_LISTENER["ReportingEventListener"]
    DIR_LISTENER["DirectoryEventListener"]
    INTERNAL_CTRL["InternalNotificationController"]
  end

  subgraph APP["Application service"]
    REQUEST_UC["RequestNotificationUseCase"]
  end

  subgraph DOM["Domain"]
    NOTI_AGG["NotificationAggregate"]
    ROUTING_POLICY["ChannelRoutingPolicy"]
    DEDUPE_POLICY["NotificationDedupPolicy"]
    TENANT_POLICY["TenantIsolationPolicy"]
    SANITIZE_POLICY["PayloadSanitizationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    REQUEST_REPO["NotificationRequestR2dbcRepositoryAdapter"]
    TEMPLATE_REPO["NotificationTemplateR2dbcRepositoryAdapter"]
    CHANNEL_POLICY_REPO["ChannelPolicyR2dbcRepositoryAdapter"]
    AUDIT_REPO["NotificationAuditR2dbcAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
    PROCESSED_EVENT_REPO["ProcessedEventR2dbcRepositoryAdapter"]
    RECIPIENT_RESOLVER["RecipientResolverDirectoryHttpClientAdapter"]
  end

  ORDER_LISTENER --> REQUEST_UC
  INV_LISTENER --> REQUEST_UC
  REP_LISTENER --> REQUEST_UC
  DIR_LISTENER --> REQUEST_UC
  INTERNAL_CTRL --> REQUEST_UC
  REQUEST_UC --> NOTI_AGG
  REQUEST_UC --> ROUTING_POLICY
  REQUEST_UC --> DEDUPE_POLICY
  REQUEST_UC --> TENANT_POLICY
  REQUEST_UC --> SANITIZE_POLICY
  REQUEST_UC --> REQUEST_REPO
  REQUEST_UC --> TEMPLATE_REPO
  REQUEST_UC --> CHANNEL_POLICY_REPO
  REQUEST_UC --> RECIPIENT_RESOLVER
  REQUEST_UC --> AUDIT_REPO
  REQUEST_UC --> OUTBOX_ADP
  REQUEST_UC --> PROCESSED_EVENT_REPO
  OUTBOX_ADP --> EVENT_PUB

  REQUEST_REPO --> NDB["Notification DB (PostgreSQL)"]
  TEMPLATE_REPO --> NDB
  CHANNEL_POLICY_REPO --> NDB
  AUDIT_REPO --> NDB
  PROCESSED_EVENT_REPO --> NDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="DispatchNotification" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    INTERNAL_CTRL["InternalNotificationController"]
    DISPATCH_SCH["DispatchSchedulerListener"]
  end

  subgraph APP["Application service"]
    DISPATCH_UC["DispatchNotificationUseCase"]
  end

  subgraph DOM["Domain"]
    NOTI_AGG["NotificationAggregate"]
    ATTEMPT["NotificationAttempt"]
    RETRY_POLICY["RetryPolicy"]
    TENANT_POLICY["TenantIsolationPolicy"]
    SANITIZE_POLICY["PayloadSanitizationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    REQUEST_REPO["NotificationRequestR2dbcRepositoryAdapter"]
    ATTEMPT_REPO["NotificationAttemptR2dbcRepositoryAdapter"]
    CHANNEL_POLICY_REPO["ChannelPolicyR2dbcRepositoryAdapter"]
    PROVIDER_CLIENT["ProviderHttpClientAdapter"]
    AUDIT_REPO["NotificationAuditR2dbcAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
  end

  INTERNAL_CTRL --> DISPATCH_UC
  DISPATCH_SCH --> DISPATCH_UC
  DISPATCH_UC --> NOTI_AGG
  DISPATCH_UC --> ATTEMPT
  DISPATCH_UC --> RETRY_POLICY
  DISPATCH_UC --> TENANT_POLICY
  DISPATCH_UC --> SANITIZE_POLICY
  DISPATCH_UC --> REQUEST_REPO
  DISPATCH_UC --> ATTEMPT_REPO
  DISPATCH_UC --> CHANNEL_POLICY_REPO
  DISPATCH_UC --> PROVIDER_CLIENT
  DISPATCH_UC --> AUDIT_REPO
  DISPATCH_UC --> OUTBOX_ADP
  OUTBOX_ADP --> EVENT_PUB

  REQUEST_REPO --> NDB["Notification DB (PostgreSQL)"]
  ATTEMPT_REPO --> NDB
  CHANNEL_POLICY_REPO --> NDB
  AUDIT_REPO --> NDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="DispatchNotificationBatch" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    DISPATCH_SCH["DispatchSchedulerListener"]
  end

  subgraph APP["Application service"]
    BATCH_UC["DispatchNotificationBatchUseCase"]
    DISPATCH_UC["DispatchNotificationUseCase"]
  end

  subgraph DOM["Domain"]
    NOTI_AGG["NotificationAggregate"]
    NOTI_STATUS["NotificationStatus"]
  end

  subgraph OUT["Adapter-out"]
    REQUEST_REPO["NotificationRequestR2dbcRepositoryAdapter"]
    AUDIT_REPO["NotificationAuditR2dbcAdapter"]
  end

  DISPATCH_SCH --> BATCH_UC
  BATCH_UC --> REQUEST_REPO
  BATCH_UC --> NOTI_AGG
  BATCH_UC --> NOTI_STATUS
  BATCH_UC --> DISPATCH_UC
  BATCH_UC --> AUDIT_REPO

  REQUEST_REPO --> NDB["Notification DB (PostgreSQL)"]
  AUDIT_REPO --> NDB
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="RetryNotification" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    INTERNAL_CTRL["InternalNotificationController"]
    RETRY_SCH["RetrySchedulerListener"]
  end

  subgraph APP["Application service"]
    RETRY_UC["RetryNotificationUseCase"]
  end

  subgraph DOM["Domain"]
    RETRY_POLICY["RetryPolicy"]
    ATTEMPT["NotificationAttempt"]
    NOTI_AGG["NotificationAggregate"]
    TENANT_POLICY["TenantIsolationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    REQUEST_REPO["NotificationRequestR2dbcRepositoryAdapter"]
    ATTEMPT_REPO["NotificationAttemptR2dbcRepositoryAdapter"]
    CHANNEL_POLICY_REPO["ChannelPolicyR2dbcRepositoryAdapter"]
    AUDIT_REPO["NotificationAuditR2dbcAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
    CLOCK_ADP["SystemClockAdapter"]
  end

  INTERNAL_CTRL --> RETRY_UC
  RETRY_SCH --> RETRY_UC
  RETRY_UC --> RETRY_POLICY
  RETRY_UC --> ATTEMPT
  RETRY_UC --> NOTI_AGG
  RETRY_UC --> TENANT_POLICY
  RETRY_UC --> REQUEST_REPO
  RETRY_UC --> ATTEMPT_REPO
  RETRY_UC --> CHANNEL_POLICY_REPO
  RETRY_UC --> AUDIT_REPO
  RETRY_UC --> OUTBOX_ADP
  RETRY_UC --> CLOCK_ADP
  OUTBOX_ADP --> EVENT_PUB

  REQUEST_REPO --> NDB["Notification DB (PostgreSQL)"]
  ATTEMPT_REPO --> NDB
  CHANNEL_POLICY_REPO --> NDB
  AUDIT_REPO --> NDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="DiscardNotification" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    INTERNAL_CTRL["InternalNotificationController"]
  end

  subgraph APP["Application service"]
    DISCARD_UC["DiscardNotificationUseCase"]
  end

  subgraph DOM["Domain"]
    NOTI_AGG["NotificationAggregate"]
    TENANT_POLICY["TenantIsolationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    REQUEST_REPO["NotificationRequestR2dbcRepositoryAdapter"]
    AUDIT_REPO["NotificationAuditR2dbcAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
  end

  INTERNAL_CTRL --> DISCARD_UC
  DISCARD_UC --> NOTI_AGG
  DISCARD_UC --> TENANT_POLICY
  DISCARD_UC --> REQUEST_REPO
  DISCARD_UC --> AUDIT_REPO
  DISCARD_UC --> OUTBOX_ADP
  OUTBOX_ADP --> EVENT_PUB

  REQUEST_REPO --> NDB["Notification DB (PostgreSQL)"]
  AUDIT_REPO --> NDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="ProcessProviderCallback" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    PROVIDER_CB_CTRL["ProviderCallbackController"]
  end

  subgraph APP["Application service"]
    PROCESS_CB_UC["ProcessProviderCallbackUseCase"]
  end

  subgraph DOM["Domain"]
    ATTEMPT["NotificationAttempt"]
    NOTI_AGG["NotificationAggregate"]
    SANITIZE_POLICY["PayloadSanitizationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    CALLBACK_REPO["ProviderCallbackR2dbcRepositoryAdapter"]
    REQUEST_REPO["NotificationRequestR2dbcRepositoryAdapter"]
    ATTEMPT_REPO["NotificationAttemptR2dbcRepositoryAdapter"]
    AUDIT_REPO["NotificationAuditR2dbcAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
  end

  PROVIDER_CB_CTRL --> PROCESS_CB_UC
  PROCESS_CB_UC --> ATTEMPT
  PROCESS_CB_UC --> NOTI_AGG
  PROCESS_CB_UC --> SANITIZE_POLICY
  PROCESS_CB_UC --> CALLBACK_REPO
  PROCESS_CB_UC --> REQUEST_REPO
  PROCESS_CB_UC --> ATTEMPT_REPO
  PROCESS_CB_UC --> AUDIT_REPO
  PROCESS_CB_UC --> OUTBOX_ADP
  OUTBOX_ADP --> EVENT_PUB

  CALLBACK_REPO --> NDB["Notification DB (PostgreSQL)"]
  REQUEST_REPO --> NDB
  ATTEMPT_REPO --> NDB
  AUDIT_REPO --> NDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="ListPendingNotifications" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    INTERNAL_CTRL["InternalNotificationController"]
  end

  subgraph APP["Application service"]
    LIST_PENDING_UC["ListPendingNotificationsUseCase"]
  end

  subgraph DOM["Domain"]
    NOTI_AGG["NotificationAggregate"]
    TENANT_POLICY["TenantIsolationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    REQUEST_REPO["NotificationRequestR2dbcRepositoryAdapter"]
    CACHE_ADP["NotificationCacheRedisAdapter"]
  end

  INTERNAL_CTRL --> LIST_PENDING_UC
  LIST_PENDING_UC --> NOTI_AGG
  LIST_PENDING_UC --> TENANT_POLICY
  LIST_PENDING_UC --> REQUEST_REPO
  LIST_PENDING_UC --> CACHE_ADP

  REQUEST_REPO --> NDB["Notification DB (PostgreSQL)"]
  CACHE_ADP --> REDIS["redis-cache"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="GetNotificationDetail" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    INTERNAL_CTRL["InternalNotificationController"]
  end

  subgraph APP["Application service"]
    GET_DETAIL_UC["GetNotificationDetailUseCase"]
  end

  subgraph DOM["Domain"]
    NOTI_AGG["NotificationAggregate"]
    ATTEMPT["NotificationAttempt"]
    TENANT_POLICY["TenantIsolationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    REQUEST_REPO["NotificationRequestR2dbcRepositoryAdapter"]
    ATTEMPT_REPO["NotificationAttemptR2dbcRepositoryAdapter"]
    CACHE_ADP["NotificationCacheRedisAdapter"]
  end

  INTERNAL_CTRL --> GET_DETAIL_UC
  GET_DETAIL_UC --> NOTI_AGG
  GET_DETAIL_UC --> ATTEMPT
  GET_DETAIL_UC --> TENANT_POLICY
  GET_DETAIL_UC --> REQUEST_REPO
  GET_DETAIL_UC --> ATTEMPT_REPO
  GET_DETAIL_UC --> CACHE_ADP

  REQUEST_REPO --> NDB["Notification DB (PostgreSQL)"]
  ATTEMPT_REPO --> NDB
  CACHE_ADP --> REDIS["redis-cache"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="ListNotificationAttempts" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    INTERNAL_CTRL["InternalNotificationController"]
  end

  subgraph APP["Application service"]
    LIST_ATTEMPTS_UC["ListNotificationAttemptsUseCase"]
  end

  subgraph DOM["Domain"]
    ATTEMPT["NotificationAttempt"]
    TENANT_POLICY["TenantIsolationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    REQUEST_REPO["NotificationRequestR2dbcRepositoryAdapter"]
    ATTEMPT_REPO["NotificationAttemptR2dbcRepositoryAdapter"]
  end

  INTERNAL_CTRL --> LIST_ATTEMPTS_UC
  LIST_ATTEMPTS_UC --> ATTEMPT
  LIST_ATTEMPTS_UC --> TENANT_POLICY
  LIST_ATTEMPTS_UC --> REQUEST_REPO
  LIST_ATTEMPTS_UC --> ATTEMPT_REPO

  REQUEST_REPO --> NDB["Notification DB (PostgreSQL)"]
  ATTEMPT_REPO --> NDB
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="ReprocessNotificationDlq" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    DLQ_SCH["NotificationDlqReprocessorListener"]
  end

  subgraph APP["Application service"]
    REPROCESS_DLQ_UC["ReprocessNotificationDlqUseCase"]
  end

  subgraph DOM["Domain"]
    DEDUPE_POLICY["NotificationDedupPolicy"]
    RETRY_POLICY["RetryPolicy"]
  end

  subgraph OUT["Adapter-out"]
    PROCESSED_EVENT_REPO["ProcessedEventR2dbcRepositoryAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
  end

  DLQ_SCH --> REPROCESS_DLQ_UC
  REPROCESS_DLQ_UC --> DEDUPE_POLICY
  REPROCESS_DLQ_UC --> RETRY_POLICY
  REPROCESS_DLQ_UC --> PROCESSED_EVENT_REPO
  REPROCESS_DLQ_UC --> OUTBOX_ADP
  OUTBOX_ADP --> EVENT_PUB

  PROCESSED_EVENT_REPO --> NDB["Notification DB (PostgreSQL)"]
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="HandleUserBlocked" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    IAM_LISTENER["IamEventListener"]
  end

  subgraph APP["Application service"]
    REQUEST_UC["RequestNotificationUseCase"]
  end

  subgraph DOM["Domain"]
    NOTI_AGG["NotificationAggregate"]
    ROUTING_POLICY["ChannelRoutingPolicy"]
    DEDUPE_POLICY["NotificationDedupPolicy"]
    TENANT_POLICY["TenantIsolationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    REQUEST_REPO["NotificationRequestR2dbcRepositoryAdapter"]
    TEMPLATE_REPO["NotificationTemplateR2dbcRepositoryAdapter"]
    CHANNEL_POLICY_REPO["ChannelPolicyR2dbcRepositoryAdapter"]
    AUDIT_REPO["NotificationAuditR2dbcAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    PROCESSED_EVENT_REPO["ProcessedEventR2dbcRepositoryAdapter"]
    RECIPIENT_RESOLVER["RecipientResolverDirectoryHttpClientAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
  end

  IAM_LISTENER --> REQUEST_UC
  REQUEST_UC --> NOTI_AGG
  REQUEST_UC --> ROUTING_POLICY
  REQUEST_UC --> DEDUPE_POLICY
  REQUEST_UC --> TENANT_POLICY
  REQUEST_UC --> REQUEST_REPO
  REQUEST_UC --> TEMPLATE_REPO
  REQUEST_UC --> CHANNEL_POLICY_REPO
  REQUEST_UC --> RECIPIENT_RESOLVER
  REQUEST_UC --> AUDIT_REPO
  REQUEST_UC --> OUTBOX_ADP
  REQUEST_UC --> PROCESSED_EVENT_REPO
  OUTBOX_ADP --> EVENT_PUB

  REQUEST_REPO --> NDB["Notification DB (PostgreSQL)"]
  TEMPLATE_REPO --> NDB
  CHANNEL_POLICY_REPO --> NDB
  AUDIT_REPO --> NDB
  PROCESSED_EVENT_REPO --> NDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< /tabs >}}

## Componentes base por capa (vista componente)
| Capa | Clases base | Responsabilidad tecnica |
|---|---|---|
| `Adapter-in` | `OrderEventListener`, `InventoryEventListener`, `ReportingEventListener`, `IamEventListener`, `DirectoryEventListener`, `InternalNotificationController`, `ProviderCallbackController`, `DispatchSchedulerListener`, `RetrySchedulerListener`, `NotificationDlqReprocessorListener`, `TriggerContextResolver` | recibir eventos/API/scheduler, materializar contexto de trigger y traducirlos a casos de uso |
| `Application service` | `RequestNotificationUseCase`, `DispatchNotificationUseCase`, `DispatchNotificationBatchUseCase`, `RetryNotificationUseCase`, `DiscardNotificationUseCase`, `ProcessProviderCallbackUseCase`, `ListPendingNotificationsUseCase`, `GetNotificationDetailUseCase`, `ListNotificationAttemptsUseCase`, `ReprocessNotificationDlqUseCase` | orquestar solicitud, envio, consultas operativas, reintentos, descarte y reproceso con idempotencia |
| `Domain` | `NotificationAggregate`, `NotificationAttempt`, `ChannelRoutingPolicy`, `RetryPolicy`, `NotificationDedupPolicy`, `TenantIsolationPolicy`, `PayloadSanitizationPolicy` | proteger invariantes `PENDING->SENT/FAILED/DISCARDED` y no bloqueo del core |
| `Adapter-out` | `NotificationRequestR2dbcRepositoryAdapter`, `NotificationAttemptR2dbcRepositoryAdapter`, `NotificationTemplateR2dbcRepositoryAdapter`, `ChannelPolicyR2dbcRepositoryAdapter`, `ProviderCallbackR2dbcRepositoryAdapter`, `NotificationAuditR2dbcAdapter`, `OutboxPersistenceAdapter`, `KafkaDomainEventPublisherAdapter`, `ProcessedEventR2dbcRepositoryAdapter`, `ProviderHttpClientAdapter`, `RecipientResolverDirectoryHttpClientAdapter`, `NotificationCacheRedisAdapter`, `SystemClockAdapter`, `PrincipalContextAdapter`, `RbacPermissionEvaluatorAdapter` | persistencia, provider externo, dedupe de eventos, seguridad contextual, cache y publicacion EDA |

## Nota de modelado
- Esta vista componente no detalla estructura de carpetas.
- Esta vista lista implementaciones de `Adapter-in`, `Application service`, `Domain` y `Adapter-out`.
- DTOs/mappers/interfaces/config se detallan en la vista de codigo.

## Dependencias externas permitidas
| Dependencia | Tipo | Uso en Notification | Criticidad |
|---|---|---|---|
| `api-gateway-service` | plataforma | enrutamiento de endpoints internos tecnicos | media |
| `order-service` | core | eventos de pedido/pago/carrito abandonado | alta |
| `inventory-service` | core | eventos operativos (`StockReservationExpired`, `LowStockDetected`) | media |
| `reporting-service` | core | evento `WeeklyReportGenerated` para distribucion | media |
| `identity-access-service` | core | evento `UserBlocked` para comunicacion de seguridad | media |
| `directory-service` | core | resolucion de destinatarios/contactos | alta |
| `Notification DB (PostgreSQL)` | datos | fuente de verdad de solicitud/intento/auditoria | critica |
| `redis-cache` | soporte | cache de consultas operativas de solicitudes | media |
| `kafka-cluster` | soporte | consumo/publicacion de eventos | alta |
| `provider-api` | externo | envio real de mensajes | critica |

## Modelo de autenticacion y autorizacion runtime
| Flujo | Autenticacion | Autorizacion y legitimidad |
|---|---|---|
| HTTP interno | `api-gateway-service` autentica las llamadas HTTP internas cuando entran por borde web. | `notification-service` materializa `PrincipalContext` via `PrincipalContextAdapter`, valida permiso con `RbacPermissionEvaluatorAdapter` y cierra `tenant`, `callerRef`, dedupe y operacion permitida con `TenantIsolationPolicy`, `NotificationDedupPolicy`, `ChannelRoutingPolicy` y `PayloadSanitizationPolicy`. |
| callback de proveedor | No representa usuario final ni JWT de negocio. | `ProviderCallbackController` materializa `TriggerContext` mediante un `TriggerContextResolver` tecnico del callback y el servicio consolida la legitimidad del cambio con `NotificationAggregate`, `NotificationAttempt` y `TenantIsolationPolicy`. |
| eventos / schedulers | No depende de JWT de usuario. | `OrderEventListener`, `InventoryEventListener`, `ReportingEventListener`, `IamEventListener`, `DirectoryEventListener`, `DispatchSchedulerListener`, `RetrySchedulerListener` y `NotificationDlqReprocessorListener` materializan `TriggerContext` mediante `TriggerContextResolver`, validan trigger, `tenant` y dedupe antes de mutar el estado de notificaciones. |


## Modelo de errores y excepciones runtime
| Responsabilidad | Componentes | Aplicacion |
|---|---|---|
| Decision semantica | `Application service`, `Domain service`, `TenantIsolationPolicy`, `NotificationDedupPolicy`, `ChannelRoutingPolicy`, `PayloadSanitizationPolicy`, `RetryPolicy` | Los casos de Notification expresan rechazo temprano y rechazo de decision mediante familias canonicas de acceso/contexto (`ApplicationException`, `AuthorizationDeniedException`, `TenantIsolationException`, `ResourceNotFoundException`) y de decision (`DomainException`, `DomainRuleViolationException`, `ConflictException`) sin filtrar errores tecnicos al caller o trigger. |
| Cierre HTTP | `InternalNotificationController`, `ProviderCallbackController`, `NotificationSecurityConfiguration` | El adapter-in HTTP o callback traduce la familia semantica o tecnica a una salida operativa coherente con `errorCode`, `category`, `traceId`, `correlationId` y `timestamp`, aun cuando el catalogo actual no separa un handler WebFlux dedicado. |
| Cierre async | `OrderEventListener`, `InventoryEventListener`, `ReportingEventListener`, `IamEventListener`, `DirectoryEventListener`, `DispatchSchedulerListener`, `RetrySchedulerListener`, `NotificationDlqReprocessorListener`, `ProcessedEventR2dbcRepositoryAdapter`, `RetryPolicy` | Los flujos event-driven y scheduler tratan duplicados como `noop idempotente`, distinguen fallos retryable/no-retryable y cierran la incidencia por reintento, DLQ o auditoria operativa. |

## Soporte de observabilidad
| Elemento | Componentes principales | Funcion arquitectonica |
|---|---|---|
| Configuracion de metricas y trazas | `NotificationObservabilityConfiguration` | Expone la configuracion base para instrumentacion transversal del servicio y puente de trazas/metricas. |
| Auditoria operativa de notificaciones | `NotificationAuditPort`, `NotificationAuditR2dbcAdapter`, `NotificationAuditPersistenceMapper` | Registran evidencia tecnica de solicitud, envio, callback, reintento, descarte y reproceso de notificaciones. |
| Emision de eventos observables | `OutboxPersistenceAdapter`, `OutboxPublisherScheduler`, `KafkaDomainEventPublisherAdapter` | Materializan y publican eventos de notificacion hacia Kafka para integracion y trazabilidad near-real-time. |

Nota:
- Esta vista solo documenta los componentes que habilitan observabilidad dentro de la arquitectura.
- La definicion detallada de metricas, logs, trazas, alertas y dashboards corresponde al pilar de calidad u operacion.

## Canales de eventos (naming canonico)
Convencion aplicada: `<bc>.<event-name>.v<major>`.

| Tipo | Evento | Topic canonico |
|---|---|---|
| Emitido | `NotificationRequested` | `notification.notification-requested.v1` |
| Emitido | `NotificationSent` | `notification.notification-sent.v1` |
| Emitido | `NotificationFailed` | `notification.notification-failed.v1` |
| Emitido | `NotificationDiscarded` | `notification.notification-discarded.v1` |
| Consumido | `OrderConfirmed` | `order.order-confirmed.v1` |
| Consumido | `OrderStatusChanged` | `order.order-status-changed.v1` |
| Consumido | `OrderPaymentRegistered` | `order.payment-registered.v1` |
| Consumido | `CartAbandonedDetected` | `order.cart-abandoned-detected.v1` |
| Consumido | `StockReservationExpired` | `inventory.stock-reservation-expired.v1` |
| Consumido | `LowStockDetected` | `inventory.low-stock-detected.v1` |
| Consumido | `OrganizationProfileUpdated` | `directory.organization-profile-updated.v1` |
| Consumido | `ContactRegistered` | `directory.contact-registered.v1` |
| Consumido | `ContactUpdated` | `directory.contact-updated.v1` |
| Consumido | `PrimaryContactChanged` | `directory.primary-contact-changed.v1` |
| Consumido | `WeeklyReportGenerated` | `reporting.weekly-generated.v1` |
| Consumido | `UserBlocked` | `iam.user-blocked.v1` |

## Mapa de procesos -> componentes clave
| Proceso | Componentes principales | Invariantes protegidos |
|---|---|---|
| registrar solicitud desde evento/API | `OrderEventListener`, `InternalNotificationController`, `RequestNotificationUseCase`, `NotificationAggregate` | dedupe de solicitud por `notificationKey` |
| reaccionar cambios institucionales | `DirectoryEventListener`, `RequestNotificationUseCase`, `RecipientResolverDirectoryHttpClientAdapter` | destinatario institucional vigente y consistente por tenant |
| ejecutar envio | `DispatchNotificationUseCase`, `ProviderHttpClientAdapter`, `NotificationAttempt` | cada intento se registra una sola vez |
| ejecutar batch de dispatch | `DispatchSchedulerListener`, `DispatchNotificationBatchUseCase`, `DispatchNotificationUseCase` | procesar lotes sin duplicar side effects por solicitud |
| reintentar fallas recuperables | `RetryNotificationUseCase`, `RetryPolicy`, `RetrySchedulerListener` | no exceder maxAttempts por solicitud |
| descartar no recuperables | `DiscardNotificationUseCase`, `NotificationAggregate`, `OutboxPersistenceAdapter` | cierre definitivo `DISCARDED` sin rollback core |
| callbacks de proveedor | `ProviderCallbackController`, `ProcessProviderCallbackUseCase` | reconciliacion idempotente por callback/providerRef |
| listar intentos de solicitud | `InternalNotificationController`, `ListNotificationAttemptsUseCase`, `NotificationAttempt` | exponer historial tecnico sin romper aislamiento de tenant |
| comunicar bloqueo de usuario | `IamEventListener`, `RequestNotificationUseCase`, `NotificationAggregate` | generar comunicacion de seguridad sin duplicar eventos consumidos |
| reproceso de DLQ | `NotificationDlqReprocessorListener`, `ReprocessNotificationDlqUseCase`, `ProcessedEventR2dbcRepositoryAdapter` | no duplicar side effects por reproceso |

## Matriz de dependencias sync y fallback
| Dependencia | Uso critico | Timeout objetivo | Fallback definido | Error semantico |
|---|---|---|---|---|
| `provider-api` | envio real de mensaje | 1.5-2.0 s | marcar `FAILED(retryable=true)` y reintentar | `canal_no_disponible` |
| `directory-service` | resolucion de destinatario | 300 ms | descartar o reintentar segun politica | `destinatario_invalido` |
| `kafka-cluster` | consumo/publicacion de eventos | async | outbox `PENDING` + reintento scheduler | no bloquea core |
| `Notification DB` | persistencia de solicitud/intento | 150-250 ms | retry por transient/optimistic lock | `internal_error` |
| `redis-cache` | lectura operativa de solicitudes | 50 ms | bypass a DB sin afectar envio | no aplica |

## Matriz de ownership operativo
| Tema | Componente owner en Notification | Evidencia |
|---|---|---|
| idempotencia de solicitudes | `NotificationDedupPolicy` + `ProcessedEventR2dbcRepositoryAdapter` | `contracts/02-Eventos.md`, `architecture/03-Casos-de-Uso-en-Ejecucion.md` |
| integridad de ciclo de vida | `NotificationAggregate` | `01-domain/bounded-contexts/notification/02-aggregates.md` |
| reintentos controlados | `RetryPolicy` + `RetrySchedulerListener` | `architecture/03-Casos-de-Uso-en-Ejecucion.md` |
| publicacion confiable | `OutboxPersistenceAdapter` + `KafkaDomainEventPublisherAdapter` | `contracts/02-Eventos.md` |
| trazabilidad y auditoria | `NotificationAuditR2dbcAdapter` | `security/01-Arquitectura-de-Seguridad.md` |

## Matriz de observabilidad por componente
| Componente | Senales minimas | Uso operativo |
|---|---|---|
| `RequestNotificationUseCase` | `notification_request_total`, `notification_request_latency_ms` | salud de creacion de solicitudes |
| `DispatchNotificationUseCase` | `notification_dispatch_total`, `notification_dispatch_latency_ms`, `notification_dispatch_error_total` | detectar caida de provider o payload invalido |
| `DispatchNotificationBatchUseCase` | `notification_dispatch_batch_total`, `notification_dispatch_batch_processed_total` | controlar el volumen y resultado del scheduler de despacho |
| `RetryNotificationUseCase` | `notification_retry_total`, `notification_retry_queue_depth` | controlar tormentas de reintento |
| `DiscardNotificationUseCase` | `notification_discard_total` | monitorear descarte definitivo |
| `ProcessProviderCallbackUseCase` | `notification_provider_callback_total`, `notification_callback_reconciliation_gap` | reconciliar acks tardios |
| `OutboxPublisherScheduler` | `notification_outbox_pending_count` | evitar atraso de eventos a reporting |
| `NotificationDlqReprocessorListener` | `notification_dlq_reprocess_total`, `notification_dlq_backlog_count` | control de mensajes poison/reproceso |

## Restricciones de diseno
- `MUST`: notificacion fallida no revierte pedido/pago/stock/reporting (`RN-NOTI-01`).
- `MUST`: toda solicitud e intento conserva `tenantId`, `traceId`, `correlationId`.
- `MUST`: estado `DISCARDED` no permite nuevos dispatch para la misma solicitud.
- `MUST`: operaciones mutantes internas usan `Idempotency-Key` o clave natural equivalente.
- `MUST`: eventos de resultado se publican via outbox transaccional.
- `SHOULD`: politica de canal permite fallback controlado (ej. `email -> inapp`) por tenant.

## Riesgos y trade-offs
- Riesgo: dependencia del proveedor externo genera picos de `FAILED`.
  - Mitigacion: retries con jitter, circuit breaker y descarte controlado.
- Riesgo: fuga de PII en payloads y callbacks.
  - Mitigacion: sanitizacion de payload + masking en logs + cifrado en transito.
- Trade-off: mantener notificacion desacoplada reduce impacto transaccional, pero agrega latencia eventual en comunicacion al cliente.
