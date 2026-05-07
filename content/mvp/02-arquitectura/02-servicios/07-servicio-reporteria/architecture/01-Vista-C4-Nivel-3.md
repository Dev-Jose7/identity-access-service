---
title: "Vista C4 Nivel 3"
linkTitle: "1. Vista C4 L3"
weight: 1
url: "/mvp/arquitectura/servicios/servicio-reporteria/arquitectura-interna/vista-c4-nivel-3/"
---

## Proposito
Definir la vista C4 de componente para `reporting-service`, detallando limites internos, dependencias y responsabilidades tecnicas para proyecciones analiticas y generacion de reportes semanales.

## Alcance y fronteras
- Incluye componentes internos de Reporting en la vista componente: `Adapter-in`, `Application service`, `Domain`, `Adapter-out`.
- Incluye clases de implementacion representativas para identificacion rapida.
- Excluye DTOs, mappers, interfaces de puertos y clases de configuracion.
- Incluye dependencias con `api-gateway-service`, `order-service`, `inventory-service`, `catalog-service`, `directory-service`, `notification-service`, `kafka-cluster`, `redis-cache`, `object-storage` y `Reporting DB`.
- Excluye decisiones de codigo de otros servicios.

## Rol del servicio en el sistema
`reporting-service` es la autoridad semantica de analitica derivada:
- consume hechos de `order`, `inventory`, `catalog`, `directory` y `notification`,
- normaliza hechos analiticos con dedupe por `sourceEventId`,
- mantiene proyecciones materializadas de ventas y abastecimiento,
- genera reportes semanales exportables por tenant,
- publica `AnalyticFactUpdated` y `WeeklyReportGenerated` para observabilidad y distribucion.

## C4 componente del servicio
La vista de componentes se divide por caso de uso para reducir cruces visuales sin perder detalle estructural del servicio. Cada diagrama conserva los mismos grupos (`Adapter-in`, `Application service`, `Domain`, `Adapter-out`) y muestra solo las relaciones relevantes para ese flujo.

{{< tabs groupid="reporting-c4-l3" >}}
{{< tab title="RegisterAnalyticFact" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    CORE_LISTENER["CoreEventsListener"]
    NOTI_LISTENER["NotificationEventsListener"]
  end

  subgraph APP["Application service"]
    REGISTER_FACT_UC["RegisterAnalyticFactUseCase"]
  end

  subgraph DOM["Domain"]
    REPORTING_AGG["ReportingViewAggregate"]
    PROJECTION_POLICY["ProjectionPolicy"]
    FACT_DEDUPE_POLICY["FactDedupPolicy"]
    TENANT_POLICY["TenantIsolationPolicy"]
    DATA_CLASSIFICATION_POLICY["DataClassificationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    FACT_REPO["AnalyticFactR2dbcRepositoryAdapter"]
    SALES_REPO["SalesProjectionR2dbcRepositoryAdapter"]
    SUPPLY_REPO["ReplenishmentProjectionR2dbcRepositoryAdapter"]
    KPI_REPO["OperationKpiR2dbcRepositoryAdapter"]
    CHECKPOINT_REPO["ConsumerCheckpointR2dbcRepositoryAdapter"]
    AUDIT_REPO["ReportingAuditR2dbcRepositoryAdapter"]
    PROCESSED_EVENT_REPO["ProcessedEventR2dbcRepositoryAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
  end

  CORE_LISTENER --> REGISTER_FACT_UC
  NOTI_LISTENER --> REGISTER_FACT_UC
  REGISTER_FACT_UC --> FACT_DEDUPE_POLICY
  REGISTER_FACT_UC --> TENANT_POLICY
  REGISTER_FACT_UC --> DATA_CLASSIFICATION_POLICY
  REGISTER_FACT_UC --> REPORTING_AGG
  REGISTER_FACT_UC --> PROJECTION_POLICY
  REGISTER_FACT_UC --> FACT_REPO
  REGISTER_FACT_UC --> CHECKPOINT_REPO
  REGISTER_FACT_UC --> SALES_REPO
  REGISTER_FACT_UC --> SUPPLY_REPO
  REGISTER_FACT_UC --> KPI_REPO
  REGISTER_FACT_UC --> AUDIT_REPO
  REGISTER_FACT_UC --> PROCESSED_EVENT_REPO
  REGISTER_FACT_UC --> OUTBOX_ADP
  OUTBOX_ADP --> EVENT_PUB

  FACT_REPO --> RDB["Reporting DB (PostgreSQL)"]
  SALES_REPO --> RDB
  SUPPLY_REPO --> RDB
  KPI_REPO --> RDB
  CHECKPOINT_REPO --> RDB
  AUDIT_REPO --> RDB
  PROCESSED_EVENT_REPO --> RDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="RefreshSalesProjection" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    CORE_LISTENER["CoreEventsListener"]
    NOTI_LISTENER["NotificationEventsListener"]
  end

  subgraph APP["Application service"]
    REFRESH_SALES_UC["RefreshSalesProjectionUseCase"]
  end

  subgraph DOM["Domain"]
    REPORTING_AGG["ReportingViewAggregate"]
    PROJECTION_POLICY["ProjectionPolicy"]
    TENANT_POLICY["TenantIsolationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    FACT_REPO["AnalyticFactR2dbcRepositoryAdapter"]
    SALES_REPO["SalesProjectionR2dbcRepositoryAdapter"]
    AUDIT_REPO["ReportingAuditR2dbcRepositoryAdapter"]
  end

  CORE_LISTENER --> REFRESH_SALES_UC
  NOTI_LISTENER --> REFRESH_SALES_UC
  REFRESH_SALES_UC --> REPORTING_AGG
  REFRESH_SALES_UC --> PROJECTION_POLICY
  REFRESH_SALES_UC --> TENANT_POLICY
  REFRESH_SALES_UC --> FACT_REPO
  REFRESH_SALES_UC --> SALES_REPO
  REFRESH_SALES_UC --> AUDIT_REPO

  FACT_REPO --> RDB["Reporting DB (PostgreSQL)"]
  SALES_REPO --> RDB
  AUDIT_REPO --> RDB
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="RefreshReplenishmentProjection" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    CORE_LISTENER["CoreEventsListener"]
    NOTI_LISTENER["NotificationEventsListener"]
  end

  subgraph APP["Application service"]
    REFRESH_SUPPLY_UC["RefreshReplenishmentProjectionUseCase"]
  end

  subgraph DOM["Domain"]
    REPORTING_AGG["ReportingViewAggregate"]
    PROJECTION_POLICY["ProjectionPolicy"]
    TENANT_POLICY["TenantIsolationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    FACT_REPO["AnalyticFactR2dbcRepositoryAdapter"]
    SUPPLY_REPO["ReplenishmentProjectionR2dbcRepositoryAdapter"]
    AUDIT_REPO["ReportingAuditR2dbcRepositoryAdapter"]
  end

  CORE_LISTENER --> REFRESH_SUPPLY_UC
  NOTI_LISTENER --> REFRESH_SUPPLY_UC
  REFRESH_SUPPLY_UC --> REPORTING_AGG
  REFRESH_SUPPLY_UC --> PROJECTION_POLICY
  REFRESH_SUPPLY_UC --> TENANT_POLICY
  REFRESH_SUPPLY_UC --> FACT_REPO
  REFRESH_SUPPLY_UC --> SUPPLY_REPO
  REFRESH_SUPPLY_UC --> AUDIT_REPO

  FACT_REPO --> RDB["Reporting DB (PostgreSQL)"]
  SUPPLY_REPO --> RDB
  AUDIT_REPO --> RDB
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="RefreshOperationKpi" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    CORE_LISTENER["CoreEventsListener"]
    NOTI_LISTENER["NotificationEventsListener"]
  end

  subgraph APP["Application service"]
    REFRESH_KPI_UC["RefreshOperationKpiUseCase"]
  end

  subgraph DOM["Domain"]
    REPORTING_AGG["ReportingViewAggregate"]
    PROJECTION_POLICY["ProjectionPolicy"]
    TENANT_POLICY["TenantIsolationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    FACT_REPO["AnalyticFactR2dbcRepositoryAdapter"]
    KPI_REPO["OperationKpiR2dbcRepositoryAdapter"]
    AUDIT_REPO["ReportingAuditR2dbcRepositoryAdapter"]
  end

  CORE_LISTENER --> REFRESH_KPI_UC
  NOTI_LISTENER --> REFRESH_KPI_UC
  REFRESH_KPI_UC --> REPORTING_AGG
  REFRESH_KPI_UC --> PROJECTION_POLICY
  REFRESH_KPI_UC --> TENANT_POLICY
  REFRESH_KPI_UC --> FACT_REPO
  REFRESH_KPI_UC --> KPI_REPO
  REFRESH_KPI_UC --> AUDIT_REPO

  FACT_REPO --> RDB["Reporting DB (PostgreSQL)"]
  KPI_REPO --> RDB
  AUDIT_REPO --> RDB
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="GenerateWeeklyReport" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    ADMIN_CTRL["ReportingAdminController"]
    WEEKLY_SCH["WeeklyReportSchedulerListener"]
  end

  subgraph APP["Application service"]
    GENERATE_WEEKLY_UC["GenerateWeeklyReportUseCase"]
  end

  subgraph DOM["Domain"]
    WEEKLY_AGG["WeeklyReportAggregate"]
    WEEKLY_POLICY["WeeklyCutoffPolicy"]
    TENANT_POLICY["TenantIsolationPolicy"]
    DATA_CLASSIFICATION_POLICY["DataClassificationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    SALES_REPO["SalesProjectionR2dbcRepositoryAdapter"]
    SUPPLY_REPO["ReplenishmentProjectionR2dbcRepositoryAdapter"]
    KPI_REPO["OperationKpiR2dbcRepositoryAdapter"]
    ARTIFACT_REPO["ReportArtifactR2dbcRepositoryAdapter"]
    DIR_POLICY["DirectoryOperationalCountryPolicyHttpClientAdapter"]
    EXPORTER["ReportExporterStorageAdapter"]
    AUDIT_REPO["ReportingAuditR2dbcRepositoryAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
    CLOCK_ADP["SystemClockAdapter"]
  end

  ADMIN_CTRL --> GENERATE_WEEKLY_UC
  WEEKLY_SCH --> GENERATE_WEEKLY_UC
  GENERATE_WEEKLY_UC --> WEEKLY_POLICY
  GENERATE_WEEKLY_UC --> WEEKLY_AGG
  GENERATE_WEEKLY_UC --> TENANT_POLICY
  GENERATE_WEEKLY_UC --> DATA_CLASSIFICATION_POLICY
  GENERATE_WEEKLY_UC --> SALES_REPO
  GENERATE_WEEKLY_UC --> SUPPLY_REPO
  GENERATE_WEEKLY_UC --> KPI_REPO
  GENERATE_WEEKLY_UC --> DIR_POLICY
  GENERATE_WEEKLY_UC --> EXPORTER
  GENERATE_WEEKLY_UC --> ARTIFACT_REPO
  GENERATE_WEEKLY_UC --> AUDIT_REPO
  GENERATE_WEEKLY_UC --> OUTBOX_ADP
  GENERATE_WEEKLY_UC --> CLOCK_ADP
  OUTBOX_ADP --> EVENT_PUB

  SALES_REPO --> RDB["Reporting DB (PostgreSQL)"]
  SUPPLY_REPO --> RDB
  KPI_REPO --> RDB
  ARTIFACT_REPO --> RDB
  AUDIT_REPO --> RDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
  EXPORTER --> STORAGE["object-storage"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="RebuildProjection" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    ADMIN_CTRL["ReportingAdminController"]
    REBUILD_SCH["ProjectionRebuildSchedulerListener"]
  end

  subgraph APP["Application service"]
    REBUILD_UC["RebuildProjectionUseCase"]
  end

  subgraph DOM["Domain"]
    REPORTING_AGG["ReportingViewAggregate"]
    PROJECTION_POLICY["ProjectionPolicy"]
    TENANT_POLICY["TenantIsolationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    FACT_REPO["AnalyticFactR2dbcRepositoryAdapter"]
    SALES_REPO["SalesProjectionR2dbcRepositoryAdapter"]
    SUPPLY_REPO["ReplenishmentProjectionR2dbcRepositoryAdapter"]
    KPI_REPO["OperationKpiR2dbcRepositoryAdapter"]
    DIR_POLICY["DirectoryOperationalCountryPolicyHttpClientAdapter"]
    AUDIT_REPO["ReportingAuditR2dbcRepositoryAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
  end

  ADMIN_CTRL --> REBUILD_UC
  REBUILD_SCH --> REBUILD_UC
  REBUILD_UC --> REPORTING_AGG
  REBUILD_UC --> PROJECTION_POLICY
  REBUILD_UC --> TENANT_POLICY
  REBUILD_UC --> FACT_REPO
  REBUILD_UC --> SALES_REPO
  REBUILD_UC --> SUPPLY_REPO
  REBUILD_UC --> KPI_REPO
  REBUILD_UC --> DIR_POLICY
  REBUILD_UC --> AUDIT_REPO
  REBUILD_UC --> OUTBOX_ADP
  OUTBOX_ADP --> EVENT_PUB

  FACT_REPO --> RDB["Reporting DB (PostgreSQL)"]
  SALES_REPO --> RDB
  SUPPLY_REPO --> RDB
  KPI_REPO --> RDB
  AUDIT_REPO --> RDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="ReprocessReportingDlq" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    DLQ_SCH["ReportingDlqReprocessorListener"]
  end

  subgraph APP["Application service"]
    REPROCESS_DLQ_UC["ReprocessReportingDlqUseCase"]
  end

  subgraph DOM["Domain"]
    FACT_DEDUPE_POLICY["FactDedupPolicy"]
    PROJECTION_POLICY["ProjectionPolicy"]
  end

  subgraph OUT["Adapter-out"]
    PROCESSED_EVENT_REPO["ProcessedEventR2dbcRepositoryAdapter"]
    CHECKPOINT_REPO["ConsumerCheckpointR2dbcRepositoryAdapter"]
    OUTBOX_ADP["OutboxPersistenceAdapter"]
    EVENT_PUB["KafkaDomainEventPublisherAdapter"]
  end

  DLQ_SCH --> REPROCESS_DLQ_UC
  REPROCESS_DLQ_UC --> FACT_DEDUPE_POLICY
  REPROCESS_DLQ_UC --> PROJECTION_POLICY
  REPROCESS_DLQ_UC --> PROCESSED_EVENT_REPO
  REPROCESS_DLQ_UC --> CHECKPOINT_REPO
  REPROCESS_DLQ_UC --> OUTBOX_ADP
  OUTBOX_ADP --> EVENT_PUB

  PROCESSED_EVENT_REPO --> RDB["Reporting DB (PostgreSQL)"]
  CHECKPOINT_REPO --> RDB
  EVENT_PUB --> KAFKA["kafka-cluster"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="GetWeeklySalesReport" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    QUERY_CTRL["ReportingQueryController"]
  end

  subgraph APP["Application service"]
    GET_SALES_UC["GetWeeklySalesReportUseCase"]
  end

  subgraph DOM["Domain"]
    REPORTING_AGG["ReportingViewAggregate"]
    TENANT_POLICY["TenantIsolationPolicy"]
    DATA_CLASSIFICATION_POLICY["DataClassificationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    SALES_REPO["SalesProjectionR2dbcRepositoryAdapter"]
    DIR_POLICY["DirectoryOperationalCountryPolicyHttpClientAdapter"]
    CACHE_ADP["ReportingCacheRedisAdapter"]
  end

  QUERY_CTRL --> GET_SALES_UC
  GET_SALES_UC --> REPORTING_AGG
  GET_SALES_UC --> TENANT_POLICY
  GET_SALES_UC --> DATA_CLASSIFICATION_POLICY
  GET_SALES_UC --> SALES_REPO
  GET_SALES_UC --> DIR_POLICY
  GET_SALES_UC --> CACHE_ADP

  SALES_REPO --> RDB["Reporting DB (PostgreSQL)"]
  CACHE_ADP --> REDIS["redis-cache"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="GetWeeklyReplenishmentReport" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    QUERY_CTRL["ReportingQueryController"]
  end

  subgraph APP["Application service"]
    GET_SUPPLY_UC["GetWeeklyReplenishmentReportUseCase"]
  end

  subgraph DOM["Domain"]
    REPORTING_AGG["ReportingViewAggregate"]
    TENANT_POLICY["TenantIsolationPolicy"]
    DATA_CLASSIFICATION_POLICY["DataClassificationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    SUPPLY_REPO["ReplenishmentProjectionR2dbcRepositoryAdapter"]
    DIR_POLICY["DirectoryOperationalCountryPolicyHttpClientAdapter"]
    CACHE_ADP["ReportingCacheRedisAdapter"]
  end

  QUERY_CTRL --> GET_SUPPLY_UC
  GET_SUPPLY_UC --> REPORTING_AGG
  GET_SUPPLY_UC --> TENANT_POLICY
  GET_SUPPLY_UC --> DATA_CLASSIFICATION_POLICY
  GET_SUPPLY_UC --> SUPPLY_REPO
  GET_SUPPLY_UC --> DIR_POLICY
  GET_SUPPLY_UC --> CACHE_ADP

  SUPPLY_REPO --> RDB["Reporting DB (PostgreSQL)"]
  CACHE_ADP --> REDIS["redis-cache"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="GetOperationsKpi" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    QUERY_CTRL["ReportingQueryController"]
  end

  subgraph APP["Application service"]
    GET_KPI_UC["GetOperationsKpiUseCase"]
  end

  subgraph DOM["Domain"]
    REPORTING_AGG["ReportingViewAggregate"]
    TENANT_POLICY["TenantIsolationPolicy"]
    DATA_CLASSIFICATION_POLICY["DataClassificationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    KPI_REPO["OperationKpiR2dbcRepositoryAdapter"]
    CACHE_ADP["ReportingCacheRedisAdapter"]
  end

  QUERY_CTRL --> GET_KPI_UC
  GET_KPI_UC --> REPORTING_AGG
  GET_KPI_UC --> TENANT_POLICY
  GET_KPI_UC --> DATA_CLASSIFICATION_POLICY
  GET_KPI_UC --> KPI_REPO
  GET_KPI_UC --> CACHE_ADP

  KPI_REPO --> RDB["Reporting DB (PostgreSQL)"]
  CACHE_ADP --> REDIS["redis-cache"]
{{< /mermaid >}}
{{< /tab >}}
{{< tab title="GetReportArtifact" >}}
{{< mermaid >}}
flowchart LR
  subgraph IN["Adapter-in"]
    QUERY_CTRL["ReportingQueryController"]
  end

  subgraph APP["Application service"]
    GET_ARTIFACT_UC["GetReportArtifactUseCase"]
  end

  subgraph DOM["Domain"]
    WEEKLY_AGG["WeeklyReportAggregate"]
    TENANT_POLICY["TenantIsolationPolicy"]
    DATA_CLASSIFICATION_POLICY["DataClassificationPolicy"]
  end

  subgraph OUT["Adapter-out"]
    ARTIFACT_REPO["ReportArtifactR2dbcRepositoryAdapter"]
    CACHE_ADP["ReportingCacheRedisAdapter"]
  end

  QUERY_CTRL --> GET_ARTIFACT_UC
  GET_ARTIFACT_UC --> WEEKLY_AGG
  GET_ARTIFACT_UC --> TENANT_POLICY
  GET_ARTIFACT_UC --> DATA_CLASSIFICATION_POLICY
  GET_ARTIFACT_UC --> ARTIFACT_REPO
  GET_ARTIFACT_UC --> CACHE_ADP

  ARTIFACT_REPO --> RDB["Reporting DB (PostgreSQL)"]
  CACHE_ADP --> REDIS["redis-cache"]
{{< /mermaid >}}
{{< /tab >}}
{{< /tabs >}}

## Componentes base por capa (vista componente)
| Capa | Clases base | Responsabilidad tecnica |
|---|---|---|
| `Adapter-in` | `CoreEventsListener`, `NotificationEventsListener`, `ReportingQueryController`, `ReportingAdminController`, `WeeklyReportSchedulerListener`, `ProjectionRebuildSchedulerListener`, `ReportingDlqReprocessorListener`, `TriggerContextResolver` | recibir eventos/API/scheduler, materializar contexto de trigger y traducir a casos de uso |
| `Application service` | `RegisterAnalyticFactUseCase`, `RefreshSalesProjectionUseCase`, `RefreshReplenishmentProjectionUseCase`, `RefreshOperationKpiUseCase`, `GenerateWeeklyReportUseCase`, `RebuildProjectionUseCase`, `ReprocessReportingDlqUseCase`, `GetWeeklySalesReportUseCase`, `GetWeeklyReplenishmentReportUseCase`, `GetOperationsKpiUseCase`, `GetReportArtifactUseCase` | ingesta idempotente, refresco de proyecciones y exposicion de consultas |
| `Domain` | `ReportingViewAggregate`, `WeeklyReportAggregate`, `ProjectionPolicy`, `WeeklyCutoffPolicy`, `FactDedupPolicy`, `TenantIsolationPolicy`, `DataClassificationPolicy` | proteger invariantes analiticas y corte semanal |
| `Adapter-out` | `AnalyticFactR2dbcRepositoryAdapter`, `SalesProjectionR2dbcRepositoryAdapter`, `ReplenishmentProjectionR2dbcRepositoryAdapter`, `OperationKpiR2dbcRepositoryAdapter`, `ConsumerCheckpointR2dbcRepositoryAdapter`, `ReportArtifactR2dbcRepositoryAdapter`, `ReportExporterStorageAdapter`, `DirectoryOperationalCountryPolicyHttpClientAdapter`, `ReportingAuditR2dbcRepositoryAdapter`, `OutboxPersistenceAdapter`, `KafkaDomainEventPublisherAdapter`, `ProcessedEventR2dbcRepositoryAdapter`, `ReportingCacheRedisAdapter`, `SystemClockAdapter`, `PrincipalContextAdapter`, `RbacPermissionEvaluatorAdapter` | persistencia, exportacion, dedupe, politica regional, seguridad contextual y publicacion EDA |

## Dependencias externas permitidas
| Dependencia | Tipo | Uso en Reporting | Criticidad |
|---|---|---|---|
| `api-gateway-service` | plataforma | exponer APIs de consulta/admin interno | media |
| `order-service` | core | fuente principal de hechos comerciales | critica |
| `inventory-service` | core | fuente de hechos de abastecimiento | critica |
| `catalog-service` | core | contexto de precio/producto para analitica | alta |
| `directory-service` | core | contexto de organizacion/contactos | media |
| `notification-service` | core | hechos de eficacia de comunicacion | media |
| `Reporting DB (PostgreSQL)` | datos | fuente de verdad de hechos/proyecciones/reportes | critica |
| `redis-cache` | soporte | cache de consultas de reportes/KPIs | media |
| `kafka-cluster` | soporte | consumo/publicacion de eventos | alta |
| `object-storage` | soporte | almacenamiento de artefactos exportables | alta |

## Modelo de autenticacion y autorizacion runtime
| Flujo | Autenticacion | Autorizacion y legitimidad |
|---|---|---|
| HTTP query/admin interno | `api-gateway-service` autentica el request antes de enrutar consultas o comandos administrativos. | `reporting-service` materializa `PrincipalContext` mediante `PrincipalContextAdapter`, valida permiso con `RbacPermissionEvaluatorAdapter`, aplica `TenantIsolationPolicy`/`DataClassificationPolicy` y deja la decision funcional en los `use cases` de consulta o generacion. |
| eventos / schedulers | No depende de JWT de usuario. | `CoreEventsListener`, `NotificationEventsListener`, `WeeklyReportSchedulerListener`, `ProjectionRebuildSchedulerListener` y `ReportingDlqReprocessorListener` materializan `TriggerContext` mediante `TriggerContextResolver`, validan `tenant`, dedupe, ventana operativa y legitimidad del trigger antes de refrescar proyecciones o exportar reportes. |


## Modelo de errores y excepciones runtime
| Responsabilidad | Componentes | Aplicacion |
|---|---|---|
| Decision semantica | `Application service`, `Domain service`, `TenantIsolationPolicy`, `DataClassificationPolicy`, `ProjectionPolicy`, `WeeklyCutoffPolicy`, `FactDedupPolicy` | Los casos de Reporting expresan rechazo temprano y rechazo de decision mediante familias canonicas de acceso/contexto (`ApplicationException`, `AuthorizationDeniedException`, `TenantIsolationException`, `ResourceNotFoundException`) y de decision (`DomainException`, `DomainRuleViolationException`, `ConflictException`) sin filtrar errores tecnicos al caller o trigger. |
| Cierre HTTP | `ReportingQueryController`, `ReportingAdminController`, `ReportingSecurityConfiguration` | El adapter-in HTTP traduce la familia semantica o tecnica a una salida operativa coherente con `errorCode`, `category`, `traceId`, `correlationId` y `timestamp`, aun cuando el catalogo actual no separa un handler WebFlux dedicado. |
| Cierre async | `CoreEventsListener`, `NotificationEventsListener`, `WeeklyReportSchedulerListener`, `ProjectionRebuildSchedulerListener`, `ReportingDlqReprocessorListener`, `ProcessedEventR2dbcRepositoryAdapter` | Los flujos event-driven y scheduler tratan duplicados como `noop idempotente`, distinguen fallos retryable/no-retryable y cierran la incidencia por reintento, DLQ o auditoria operativa. |

## Soporte de observabilidad
| Elemento | Componentes principales | Funcion arquitectonica |
|---|---|---|
| Configuracion de metricas y trazas | `ReportingObservabilityConfiguration` | Expone la configuracion base para instrumentacion transversal del servicio y puente de trazas/metricas. |
| Auditoria operativa de reportes | `ReportingAuditPort`, `ReportingAuditR2dbcRepositoryAdapter`, `ReportingAuditPersistenceMapper` | Registran evidencia tecnica de ingesta analitica, refresco de proyecciones, exportacion semanal y reprocesos de reporting. |
| Emision de eventos observables | `OutboxPersistenceAdapter`, `OutboxPublisherScheduler`, `KafkaDomainEventPublisherAdapter` | Materializan y publican eventos de reporting hacia Kafka para integracion y trazabilidad near-real-time. |

Nota:
- Esta vista solo documenta los componentes que habilitan observabilidad dentro de la arquitectura.
- La definicion detallada de metricas, logs, trazas, alertas y dashboards corresponde al pilar de calidad u operacion.

## Canales de eventos (naming canonico)
Convencion aplicada: `<bc>.<event-name>.v<major>`.

| Tipo | Evento | Topic canonico |
|---|---|---|
| Emitido | `AnalyticFactUpdated` | `reporting.fact-updated.v1` |
| Emitido | `WeeklyReportGenerated` | `reporting.weekly-generated.v1` |
| Consumido | `OrderCreated` | `order.order-created.v1` |
| Consumido | `OrderConfirmed` | `order.order-confirmed.v1` |
| Consumido | `OrderStatusChanged` | `order.order-status-changed.v1` |
| Consumido | `OrderPaymentRegistered` | `order.payment-registered.v1` |
| Consumido | `OrderPaymentStatusUpdated` | `order.payment-status-updated.v1` |
| Consumido | `CartAbandonedDetected` | `order.cart-abandoned-detected.v1` |
| Consumido | `StockUpdated` | `inventory.stock-updated.v1` |
| Consumido | `StockReserved` | `inventory.stock-reserved.v1` |
| Consumido | `StockReservationExpired` | `inventory.stock-reservation-expired.v1` |
| Consumido | `LowStockDetected` | `inventory.low-stock-detected.v1` |
| Consumido | `ProductUpdated` | `catalog.product-updated.v1` |
| Consumido | `PriceUpdated` | `catalog.price-updated.v1` |
| Consumido | `PriceScheduled` | `catalog.price-scheduled.v1` |
| Consumido | `OrganizationProfileUpdated` | `directory.organization-profile-updated.v1` |
| Consumido | `PrimaryContactChanged` | `directory.primary-contact-changed.v1` |
| Consumido | `NotificationRequested` | `notification.notification-requested.v1` |
| Consumido | `NotificationSent` | `notification.notification-sent.v1` |
| Consumido | `NotificationFailed` | `notification.notification-failed.v1` |
| Consumido | `NotificationDiscarded` | `notification.notification-discarded.v1` |

## Mapa de procesos -> componentes clave
| Proceso | Componentes principales | Invariantes protegidos |
|---|---|---|
| ingestar hecho analitico | `CoreEventsListener`, `RegisterAnalyticFactUseCase`, `FactDedupPolicy` | dedupe por `sourceEventId` |
| refrescar vista de ventas | `RefreshSalesProjectionUseCase`, `ProjectionPolicy`, `SalesProjectionR2dbcRepositoryAdapter` | consistencia por tenant/periodo |
| refrescar vista de abastecimiento | `RefreshReplenishmentProjectionUseCase`, `ProjectionPolicy`, `ReplenishmentProjectionR2dbcRepositoryAdapter` | no usar proyeccion como verdad transaccional |
| generar reporte semanal | `GenerateWeeklyReportUseCase`, `WeeklyCutoffPolicy`, `ReportExporterStorageAdapter` | unicidad por `tenant+week+reportType` |
| recalcular proyeccion completa | `RebuildProjectionUseCase`, `AnalyticFactR2dbcRepositoryAdapter` | recuperacion idempotente de vistas |
| reprocesar DLQ | `ReportingDlqReprocessorListener`, `ReprocessReportingDlqUseCase` | no side effects duplicados |
| consultar artefacto semanal | `GetReportArtifactUseCase`, `WeeklyReportAggregate`, `ReportArtifactR2dbcRepositoryAdapter` | acceso por tenant y artefacto existente |

## Matriz de dependencias sync y fallback
| Dependencia | Uso critico | Timeout objetivo | Fallback definido | Error semantico |
|---|---|---|---|---|
| `kafka-cluster` | ingesta de eventos upstream | async | checkpoint + reproceso DLQ | no bloquea core |
| `Reporting DB` | persistir hecho/proyeccion | 150-300 ms | retry por transient y backpressure | `internal_error` |
| `directory-service` | resolver politica operacional vigente por `countryCode` en consultas semanales y jobs de generacion/rebuild | 300 ms | sin fallback global implicito; se bloquea la operacion critica | `configuracion_pais_no_disponible` |
| `object-storage` | publicar artefacto semanal | 1-2 s por upload | reintento y requeue de generacion | `reporte_generacion_fallida` |
| `redis-cache` | consultas read-heavy | 50 ms | bypass a DB con limites | no aplica |

## Matriz de ownership operativo
| Tema | Componente owner en Reporting | Evidencia |
|---|---|---|
| idempotencia de ingesta | `FactDedupPolicy` + `ProcessedEventR2dbcRepositoryAdapter` | `contracts/02-Eventos.md`, `architecture/03-Casos-de-Uso-en-Ejecucion.md` |
| integridad de proyecciones | `ProjectionPolicy` + `ReportingViewAggregate` | `data/01-Modelo-Logico.md` |
| control de corte semanal | `WeeklyCutoffPolicy` + `WeeklyReportAggregate` | `architecture/03-Casos-de-Uso-en-Ejecucion.md` |
| publicacion confiable | `OutboxPersistenceAdapter` + `KafkaDomainEventPublisherAdapter` | `contracts/02-Eventos.md` |
| trazabilidad y auditoria | `ReportingAuditR2dbcRepositoryAdapter` | `security/01-Arquitectura-de-Seguridad.md` |

## Matriz de observabilidad por componente
| Componente | Senales minimas | Uso operativo |
|---|---|---|
| `RegisterAnalyticFactUseCase` | `reporting_ingestion_total`, `reporting_ingestion_latency_ms` | salud de ingesta por evento |
| `RefreshSalesProjectionUseCase` | `reporting_sales_refresh_duration_ms` | estabilidad de vista de ventas |
| `RefreshReplenishmentProjectionUseCase` | `reporting_supply_refresh_duration_ms` | estabilidad de vista de abastecimiento |
| `GenerateWeeklyReportUseCase` | `reporting_weekly_job_duration_ms`, `reporting_weekly_job_total` | cumplimiento de NFR-002 |
| `RebuildProjectionUseCase` | `reporting_rebuild_total`, `reporting_rebuild_duration_ms` | control de recomputo |
| `ReportingQueryController` | `reporting_query_total`, `reporting_query_latency_ms` | salud operativa de consultas y recuperacion de artefactos |
| `OutboxPublisherScheduler` | `reporting_outbox_pending_count` | evitar atraso en eventos emitidos |
| `CoreEventsListener` | `reporting_consumer_lag` | monitoreo de backlog de eventos |

## Restricciones de diseno
- `MUST`: reporting no muta entidades transaccionales de core (`RN-REP-01`).
- `MUST`: hechos se aplican idempotentemente por `sourceEventId`.
- `MUST`: consultas y reportes aplican aislamiento por `tenantId`.
- `MUST`: eventos emitidos se publican via outbox transaccional.
- `SHOULD`: proyecciones usan consistencia eventual con mecanismo de rebuild controlado.

## Riesgos y trade-offs
- Riesgo: lag de consumo degrada frescura de reportes.
  - Mitigacion: monitoreo de lag, escalado de consumidores y recomputo por lote.
- Riesgo: almacenamiento de artefactos falla en cierre semanal.
  - Mitigacion: reintentos idempotentes y ventana de recuperacion.
- Trade-off: proyecciones materializadas mejoran latencia de consulta, pero elevan complejidad de reconciliacion.
