---
title: "Vista de Codigo"
linkTitle: "2. Vista de Codigo"
weight: 2
url: "/mvp/arquitectura/servicios/servicio-reporteria/arquitectura-interna/vista-codigo/"
---

## Proposito
Definir el catalogo completo de clases/archivos del servicio `reporting-service` para implementacion Java 21 + Spring WebFlux con arquitectura hexagonal/clean, CQRS ligero, EDA y DDD.

## Alcance y fronteras
- Incluye inventario completo de clases por carpeta para el servicio Reporting.
- Incluye separacion estricta de estructura: `domain`, `application`, `infrastructure`.
- Incluye clases de configuracion para dependencias (security, kafka, r2dbc, redis, storage, observabilidad).
- Excluye codigo de otros BC/servicios.

## Regla de completitud aplicada
- Este documento define **catalogo completo**, no minimo.
- Cada clase se mapea a una carpeta concreta del arbol canonico.
- `application` usa contratos internos `command/query/result`; el borde web usa `request/response` y mappers dedicados.
- El servicio incorpora seguridad contextual (`PrincipalContext`) para HTTP y seguridad de trigger (`TriggerContext`) para eventos/schedulers.

## Estructura estricta (Reporting)
Este arbol muestra la estructura canonica completa del servicio a nivel de carpetas. El detalle por archivo y los diagramas de clase individuales se consultan mas abajo en la vista por capas.

```tree
- src | folder
  - main | folder
    - java | code
      - com | folder
        - arka | building | primary
          - reporting | microchip | primary
            - domain | cubes | info
              - model | folder-open | info
                - fact | folder
                  - valueobject | folder
                  - enum | folder
                  - event | share-nodes | accent
                - projection | folder
                  - valueobject | folder
                  - enum | folder
                - report | folder
                  - entity | table
                  - valueobject | folder
                  - enum | folder
                  - event | share-nodes | accent
                - checkpoint | folder
                  - valueobject | folder
              - service | gear | info
              - exception | folder
            - application | sitemap | warning
              - port | plug | warning
                - in | arrow-right
                  - command | bolt
                  - query | magnifying-glass
                - out | arrow-left
                  - persistence | database
                  - external | cloud
                  - event | share-nodes | accent
                  - audit | clipboard
                  - cache | hard-drive
                  - security | shield
              - usecase | bolt | warning
                - command | bolt
                - query | magnifying-glass
              - command | terminal | warning
              - query | binoculars | warning
              - result | file-lines | warning
              - mapper | shuffle
                - command | shuffle
                - query | shuffle
                - result | shuffle
              - exception | folder
            - infrastructure | server | secondary
              - adapter | plug | secondary
                - in | arrow-right
                  - web | globe
                    - request | file-import
                    - response | file-export
                    - mapper | shuffle
                      - command | shuffle
                      - query | shuffle
                      - response | shuffle
                    - controller | globe
                  - listener | bell
                - out | arrow-left
                  - persistence | database
                    - entity | table
                    - mapper | shuffle
                    - repository | database
                  - external | cloud
                  - event | share-nodes | accent
                  - cache | hard-drive
                  - security | shield
              - config | gear
              - exception | folder
```

## Estructura detallada por capas
Esta seccion concentra el arbol navegable por capa con todos los archivos del servicio. Cada archivo sigue abriendo su diagrama de clase individual en el visor.

{{% tabs groupid="reporting-structure" %}}
{{% tab title="Domain" %}}
```tree
- com | folder
  - arka | building | primary
    - reporting | microchip | primary
      - domain | cubes | info
        - model | folder-open | info
          - fact | folder
            - valueobject | folder
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-analyticfactid" data-diagram-title="AnalyticFactId.java" aria-label="Abrir diagrama de clase para AnalyticFactId.java"><code>AnalyticFactId.java</code></button> | file-code | code
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-sourceeventid" data-diagram-title="SourceEventId.java" aria-label="Abrir diagrama de clase para SourceEventId.java"><code>SourceEventId.java</code></button> | file-code | code
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-factpayload" data-diagram-title="FactPayload.java" aria-label="Abrir diagrama de clase para FactPayload.java"><code>FactPayload.java</code></button> | file-code | code
            - enum | folder
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-analyticfactstatus" data-diagram-title="AnalyticFactStatus.java" aria-label="Abrir diagrama de clase para AnalyticFactStatus.java"><code>AnalyticFactStatus.java</code></button> | file-code | code
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-facttype" data-diagram-title="FactType.java" aria-label="Abrir diagrama de clase para FactType.java"><code>FactType.java</code></button> | file-code | code
            - event | share-nodes | accent
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-analyticfactupdatedevent" data-diagram-title="AnalyticFactUpdatedEvent.java" aria-label="Abrir diagrama de clase para AnalyticFactUpdatedEvent.java"><code>AnalyticFactUpdatedEvent.java</code></button> | share-nodes | accent
            - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reportingviewaggregate" data-diagram-title="ReportingViewAggregate.java" aria-label="Abrir diagrama de clase para ReportingViewAggregate.java"><code>ReportingViewAggregate.java</code></button> | file-code | code
            - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-analyticfact" data-diagram-title="AnalyticFact.java" aria-label="Abrir diagrama de clase para AnalyticFact.java"><code>AnalyticFact.java</code></button> | file-code | code
          - projection | folder
            - valueobject | folder
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-projectionkey" data-diagram-title="ProjectionKey.java" aria-label="Abrir diagrama de clase para ProjectionKey.java"><code>ProjectionKey.java</code></button> | file-code | code
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-topproductmetric" data-diagram-title="TopProductMetric.java" aria-label="Abrir diagrama de clase para TopProductMetric.java"><code>TopProductMetric.java</code></button> | file-code | code
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-topcustomermetric" data-diagram-title="TopCustomerMetric.java" aria-label="Abrir diagrama de clase para TopCustomerMetric.java"><code>TopCustomerMetric.java</code></button> | file-code | code
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-coveragesnapshot" data-diagram-title="CoverageSnapshot.java" aria-label="Abrir diagrama de clase para CoverageSnapshot.java"><code>CoverageSnapshot.java</code></button> | file-code | code
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-kpimetric" data-diagram-title="KpiMetric.java" aria-label="Abrir diagrama de clase para KpiMetric.java"><code>KpiMetric.java</code></button> | file-code | code
            - enum | folder
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-risklevel" data-diagram-title="RiskLevel.java" aria-label="Abrir diagrama de clase para RiskLevel.java"><code>RiskLevel.java</code></button> | file-code | code
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-projectiontype" data-diagram-title="ProjectionType.java" aria-label="Abrir diagrama de clase para ProjectionType.java"><code>ProjectionType.java</code></button> | file-code | code
            - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-salesprojection" data-diagram-title="SalesProjection.java" aria-label="Abrir diagrama de clase para SalesProjection.java"><code>SalesProjection.java</code></button> | file-code | code
            - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-replenishmentprojection" data-diagram-title="ReplenishmentProjection.java" aria-label="Abrir diagrama de clase para ReplenishmentProjection.java"><code>ReplenishmentProjection.java</code></button> | file-code | code
            - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-operationkpiprojection" data-diagram-title="OperationKpiProjection.java" aria-label="Abrir diagrama de clase para OperationKpiProjection.java"><code>OperationKpiProjection.java</code></button> | file-code | code
          - report | folder
            - entity | table
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reportartifact" data-diagram-title="ReportArtifact.java" aria-label="Abrir diagrama de clase para ReportArtifact.java"><code>ReportArtifact.java</code></button> | table | secondary
            - valueobject | folder
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reportartifactref" data-diagram-title="ReportArtifactRef.java" aria-label="Abrir diagrama de clase para ReportArtifactRef.java"><code>ReportArtifactRef.java</code></button> | file-code | code
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-weekid" data-diagram-title="WeekId.java" aria-label="Abrir diagrama de clase para WeekId.java"><code>WeekId.java</code></button> | file-code | code
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reportgenerationwindow" data-diagram-title="ReportGenerationWindow.java" aria-label="Abrir diagrama de clase para ReportGenerationWindow.java"><code>ReportGenerationWindow.java</code></button> | file-code | code
            - enum | folder
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-weeklyreportstatus" data-diagram-title="WeeklyReportStatus.java" aria-label="Abrir diagrama de clase para WeeklyReportStatus.java"><code>WeeklyReportStatus.java</code></button> | file-code | code
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reportformat" data-diagram-title="ReportFormat.java" aria-label="Abrir diagrama de clase para ReportFormat.java"><code>ReportFormat.java</code></button> | file-code | code
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reporttype" data-diagram-title="ReportType.java" aria-label="Abrir diagrama de clase para ReportType.java"><code>ReportType.java</code></button> | file-code | code
            - event | share-nodes | accent
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-weeklyreportgeneratedevent" data-diagram-title="WeeklyReportGeneratedEvent.java" aria-label="Abrir diagrama de clase para WeeklyReportGeneratedEvent.java"><code>WeeklyReportGeneratedEvent.java</code></button> | share-nodes | accent
            - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-weeklyreportaggregate" data-diagram-title="WeeklyReportAggregate.java" aria-label="Abrir diagrama de clase para WeeklyReportAggregate.java"><code>WeeklyReportAggregate.java</code></button> | file-code | code
          - checkpoint | folder
            - valueobject | folder
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-consumerref" data-diagram-title="ConsumerRef.java" aria-label="Abrir diagrama de clase para ConsumerRef.java"><code>ConsumerRef.java</code></button> | file-code | code
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-kafkatopicpartition" data-diagram-title="KafkaTopicPartition.java" aria-label="Abrir diagrama de clase para KafkaTopicPartition.java"><code>KafkaTopicPartition.java</code></button> | file-code | code
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-offsetposition" data-diagram-title="OffsetPosition.java" aria-label="Abrir diagrama de clase para OffsetPosition.java"><code>OffsetPosition.java</code></button> | file-code | code
            - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-consumercheckpoint" data-diagram-title="ConsumerCheckpoint.java" aria-label="Abrir diagrama de clase para ConsumerCheckpoint.java"><code>ConsumerCheckpoint.java</code></button> | file-code | code
        - service | gear | info
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-projectionpolicy" data-diagram-title="ProjectionPolicy.java" aria-label="Abrir diagrama de clase para ProjectionPolicy.java"><code>ProjectionPolicy.java</code></button> | gear | info
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-factdeduppolicy" data-diagram-title="FactDedupPolicy.java" aria-label="Abrir diagrama de clase para FactDedupPolicy.java"><code>FactDedupPolicy.java</code></button> | gear | info
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-weeklycutoffpolicy" data-diagram-title="WeeklyCutoffPolicy.java" aria-label="Abrir diagrama de clase para WeeklyCutoffPolicy.java"><code>WeeklyCutoffPolicy.java</code></button> | gear | info
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-tenantisolationpolicy" data-diagram-title="TenantIsolationPolicy.java" aria-label="Abrir diagrama de clase para TenantIsolationPolicy.java"><code>TenantIsolationPolicy.java</code></button> | gear | info
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-dataclassificationpolicy" data-diagram-title="DataClassificationPolicy.java" aria-label="Abrir diagrama de clase para DataClassificationPolicy.java"><code>DataClassificationPolicy.java</code></button> | gear | info
        - exception | folder
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-domainexception" data-diagram-title="DomainException.java" aria-label="Abrir diagrama de clase para DomainException.java"><code>DomainException.java</code></button> | file-code | code
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-domainruleviolationexception" data-diagram-title="DomainRuleViolationException.java" aria-label="Abrir diagrama de clase para DomainRuleViolationException.java"><code>DomainRuleViolationException.java</code></button> | file-code | code
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-conflictexception" data-diagram-title="ConflictException.java" aria-label="Abrir diagrama de clase para ConflictException.java"><code>ConflictException.java</code></button> | file-code | code
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-analyticfactrejectedexception" data-diagram-title="AnalyticFactRejectedException.java" aria-label="Abrir diagrama de clase para AnalyticFactRejectedException.java"><code>AnalyticFactRejectedException.java</code></button> | file-code | code
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-projectionwindowclosedexception" data-diagram-title="ProjectionWindowClosedException.java" aria-label="Abrir diagrama de clase para ProjectionWindowClosedException.java"><code>ProjectionWindowClosedException.java</code></button> | file-code | code
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-weeklyreportgenerationconflictexception" data-diagram-title="WeeklyReportGenerationConflictException.java" aria-label="Abrir diagrama de clase para WeeklyReportGenerationConflictException.java"><code>WeeklyReportGenerationConflictException.java</code></button> | file-code | code
```
{{% /tab %}}
{{% tab title="Application" %}}
```tree
- com | folder
  - arka | building | primary
    - reporting | microchip | primary
      - application | sitemap | warning
        - port | plug | warning
          - in | arrow-right
            - command | bolt
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-registeranalyticfactport" data-diagram-title="RegisterAnalyticFactPort.java" aria-label="Abrir diagrama de clase para RegisterAnalyticFactPort.java"><code>RegisterAnalyticFactPort.java</code></button> | plug | warning
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-generateweeklyreportport" data-diagram-title="GenerateWeeklyReportPort.java" aria-label="Abrir diagrama de clase para GenerateWeeklyReportPort.java"><code>GenerateWeeklyReportPort.java</code></button> | plug | warning
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-rebuildprojectionport" data-diagram-title="RebuildProjectionPort.java" aria-label="Abrir diagrama de clase para RebuildProjectionPort.java"><code>RebuildProjectionPort.java</code></button> | plug | warning
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reprocessreportingdlqport" data-diagram-title="ReprocessReportingDlqPort.java" aria-label="Abrir diagrama de clase para ReprocessReportingDlqPort.java"><code>ReprocessReportingDlqPort.java</code></button> | plug | warning
            - query | magnifying-glass
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reportingqueryport" data-diagram-title="ReportingQueryPort.java" aria-label="Abrir diagrama de clase para ReportingQueryPort.java"><code>ReportingQueryPort.java</code></button> | plug | warning
          - out | arrow-left
            - persistence | database
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-analyticfactrepositoryport" data-diagram-title="AnalyticFactRepositoryPort.java" aria-label="Abrir diagrama de clase para AnalyticFactRepositoryPort.java"><code>AnalyticFactRepositoryPort.java</code></button> | plug | warning
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-salesprojectionrepositoryport" data-diagram-title="SalesProjectionRepositoryPort.java" aria-label="Abrir diagrama de clase para SalesProjectionRepositoryPort.java"><code>SalesProjectionRepositoryPort.java</code></button> | plug | warning
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-replenishmentprojectionrepositoryport" data-diagram-title="ReplenishmentProjectionRepositoryPort.java" aria-label="Abrir diagrama de clase para ReplenishmentProjectionRepositoryPort.java"><code>ReplenishmentProjectionRepositoryPort.java</code></button> | plug | warning
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-operationkpirepositoryport" data-diagram-title="OperationKpiRepositoryPort.java" aria-label="Abrir diagrama de clase para OperationKpiRepositoryPort.java"><code>OperationKpiRepositoryPort.java</code></button> | plug | warning
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-checkpointrepositoryport" data-diagram-title="CheckpointRepositoryPort.java" aria-label="Abrir diagrama de clase para CheckpointRepositoryPort.java"><code>CheckpointRepositoryPort.java</code></button> | plug | warning
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-weeklyreportexecutionrepositoryport" data-diagram-title="WeeklyReportExecutionRepositoryPort.java" aria-label="Abrir diagrama de clase para WeeklyReportExecutionRepositoryPort.java"><code>WeeklyReportExecutionRepositoryPort.java</code></button> | plug | warning
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reportartifactrepositoryport" data-diagram-title="ReportArtifactRepositoryPort.java" aria-label="Abrir diagrama de clase para ReportArtifactRepositoryPort.java"><code>ReportArtifactRepositoryPort.java</code></button> | plug | warning
            - external | cloud
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reportexporterport" data-diagram-title="ReportExporterPort.java" aria-label="Abrir diagrama de clase para ReportExporterPort.java"><code>ReportExporterPort.java</code></button> | plug | warning
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-directoryoperationalcountrypolicyport" data-diagram-title="DirectoryOperationalCountryPolicyPort.java" aria-label="Abrir diagrama de clase para DirectoryOperationalCountryPolicyPort.java"><code>DirectoryOperationalCountryPolicyPort.java</code></button> | plug | warning
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-clockport" data-diagram-title="ClockPort.java" aria-label="Abrir diagrama de clase para ClockPort.java"><code>ClockPort.java</code></button> | plug | warning
            - event | share-nodes | accent
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-outboxport" data-diagram-title="OutboxPort.java" aria-label="Abrir diagrama de clase para OutboxPort.java"><code>OutboxPort.java</code></button> | plug | warning
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-processedeventport" data-diagram-title="ProcessedEventPort.java" aria-label="Abrir diagrama de clase para ProcessedEventPort.java"><code>ProcessedEventPort.java</code></button> | plug | warning
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-domaineventpublisherport" data-diagram-title="DomainEventPublisherPort.java" aria-label="Abrir diagrama de clase para DomainEventPublisherPort.java"><code>DomainEventPublisherPort.java</code></button> | plug | warning
            - audit | clipboard
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reportingauditport" data-diagram-title="ReportingAuditPort.java" aria-label="Abrir diagrama de clase para ReportingAuditPort.java"><code>ReportingAuditPort.java</code></button> | plug | warning
            - cache | hard-drive
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reportingcacheport" data-diagram-title="ReportingCachePort.java" aria-label="Abrir diagrama de clase para ReportingCachePort.java"><code>ReportingCachePort.java</code></button> | plug | warning
            - security | shield
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-principalcontext" data-diagram-title="PrincipalContext.java" aria-label="Abrir diagrama de clase para PrincipalContext.java"><code>PrincipalContext.java</code></button> | file-code | code
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-principalcontextport" data-diagram-title="PrincipalContextPort.java" aria-label="Abrir diagrama de clase para PrincipalContextPort.java"><code>PrincipalContextPort.java</code></button> | plug | warning
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-permissionevaluatorport" data-diagram-title="PermissionEvaluatorPort.java" aria-label="Abrir diagrama de clase para PermissionEvaluatorPort.java"><code>PermissionEvaluatorPort.java</code></button> | plug | warning
        - usecase | bolt | warning
          - command | bolt
            - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-registeranalyticfactusecase" data-diagram-title="RegisterAnalyticFactUseCase.java" aria-label="Abrir diagrama de clase para RegisterAnalyticFactUseCase.java"><code>RegisterAnalyticFactUseCase.java</code></button> | bolt | warning
            - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-refreshsalesprojectionusecase" data-diagram-title="RefreshSalesProjectionUseCase.java" aria-label="Abrir diagrama de clase para RefreshSalesProjectionUseCase.java"><code>RefreshSalesProjectionUseCase.java</code></button> | bolt | warning
            - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-refreshreplenishmentprojectionusecase" data-diagram-title="RefreshReplenishmentProjectionUseCase.java" aria-label="Abrir diagrama de clase para RefreshReplenishmentProjectionUseCase.java"><code>RefreshReplenishmentProjectionUseCase.java</code></button> | bolt | warning
            - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-refreshoperationkpiusecase" data-diagram-title="RefreshOperationKpiUseCase.java" aria-label="Abrir diagrama de clase para RefreshOperationKpiUseCase.java"><code>RefreshOperationKpiUseCase.java</code></button> | bolt | warning
            - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-generateweeklyreportusecase" data-diagram-title="GenerateWeeklyReportUseCase.java" aria-label="Abrir diagrama de clase para GenerateWeeklyReportUseCase.java"><code>GenerateWeeklyReportUseCase.java</code></button> | bolt | warning
            - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-rebuildprojectionusecase" data-diagram-title="RebuildProjectionUseCase.java" aria-label="Abrir diagrama de clase para RebuildProjectionUseCase.java"><code>RebuildProjectionUseCase.java</code></button> | bolt | warning
            - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reprocessreportingdlqusecase" data-diagram-title="ReprocessReportingDlqUseCase.java" aria-label="Abrir diagrama de clase para ReprocessReportingDlqUseCase.java"><code>ReprocessReportingDlqUseCase.java</code></button> | bolt | warning
          - query | magnifying-glass
            - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-getweeklysalesreportusecase" data-diagram-title="GetWeeklySalesReportUseCase.java" aria-label="Abrir diagrama de clase para GetWeeklySalesReportUseCase.java"><code>GetWeeklySalesReportUseCase.java</code></button> | bolt | warning
            - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-getweeklyreplenishmentreportusecase" data-diagram-title="GetWeeklyReplenishmentReportUseCase.java" aria-label="Abrir diagrama de clase para GetWeeklyReplenishmentReportUseCase.java"><code>GetWeeklyReplenishmentReportUseCase.java</code></button> | bolt | warning
            - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-getoperationskpiusecase" data-diagram-title="GetOperationsKpiUseCase.java" aria-label="Abrir diagrama de clase para GetOperationsKpiUseCase.java"><code>GetOperationsKpiUseCase.java</code></button> | bolt | warning
            - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-getreportartifactusecase" data-diagram-title="GetReportArtifactUseCase.java" aria-label="Abrir diagrama de clase para GetReportArtifactUseCase.java"><code>GetReportArtifactUseCase.java</code></button> | bolt | warning
        - command | terminal | warning
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-registeranalyticfactcommand" data-diagram-title="RegisterAnalyticFactCommand.java" aria-label="Abrir diagrama de clase para RegisterAnalyticFactCommand.java"><code>RegisterAnalyticFactCommand.java</code></button> | file-code | code
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-generateweeklyreportcommand" data-diagram-title="GenerateWeeklyReportCommand.java" aria-label="Abrir diagrama de clase para GenerateWeeklyReportCommand.java"><code>GenerateWeeklyReportCommand.java</code></button> | file-code | code
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-rebuildprojectioncommand" data-diagram-title="RebuildProjectionCommand.java" aria-label="Abrir diagrama de clase para RebuildProjectionCommand.java"><code>RebuildProjectionCommand.java</code></button> | file-code | code
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reprocessreportingdlqcommand" data-diagram-title="ReprocessReportingDlqCommand.java" aria-label="Abrir diagrama de clase para ReprocessReportingDlqCommand.java"><code>ReprocessReportingDlqCommand.java</code></button> | file-code | code
        - query | binoculars | warning
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-weeklysalesreportquery" data-diagram-title="WeeklySalesReportQuery.java" aria-label="Abrir diagrama de clase para WeeklySalesReportQuery.java"><code>WeeklySalesReportQuery.java</code></button> | file-code | code
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-weeklyreplenishmentreportquery" data-diagram-title="WeeklyReplenishmentReportQuery.java" aria-label="Abrir diagrama de clase para WeeklyReplenishmentReportQuery.java"><code>WeeklyReplenishmentReportQuery.java</code></button> | file-code | code
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-operationskpiquery" data-diagram-title="OperationsKpiQuery.java" aria-label="Abrir diagrama de clase para OperationsKpiQuery.java"><code>OperationsKpiQuery.java</code></button> | file-code | code
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reportartifactquery" data-diagram-title="ReportArtifactQuery.java" aria-label="Abrir diagrama de clase para ReportArtifactQuery.java"><code>ReportArtifactQuery.java</code></button> | file-code | code
        - result | file-lines | warning
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-salesreportresult" data-diagram-title="SalesReportResult.java" aria-label="Abrir diagrama de clase para SalesReportResult.java"><code>SalesReportResult.java</code></button> | file-code | code
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-replenishmentreportresult" data-diagram-title="ReplenishmentReportResult.java" aria-label="Abrir diagrama de clase para ReplenishmentReportResult.java"><code>ReplenishmentReportResult.java</code></button> | file-code | code
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-operationskpiresult" data-diagram-title="OperationsKpiResult.java" aria-label="Abrir diagrama de clase para OperationsKpiResult.java"><code>OperationsKpiResult.java</code></button> | file-code | code
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reportartifactresult" data-diagram-title="ReportArtifactResult.java" aria-label="Abrir diagrama de clase para ReportArtifactResult.java"><code>ReportArtifactResult.java</code></button> | file-code | code
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-generateweeklyreportresult" data-diagram-title="GenerateWeeklyReportResult.java" aria-label="Abrir diagrama de clase para GenerateWeeklyReportResult.java"><code>GenerateWeeklyReportResult.java</code></button> | file-code | code
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-rebuildprojectionresult" data-diagram-title="RebuildProjectionResult.java" aria-label="Abrir diagrama de clase para RebuildProjectionResult.java"><code>RebuildProjectionResult.java</code></button> | file-code | code
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reportingdlqreprocessresult" data-diagram-title="ReportingDlqReprocessResult.java" aria-label="Abrir diagrama de clase para ReportingDlqReprocessResult.java"><code>ReportingDlqReprocessResult.java</code></button> | file-code | code
        - mapper | shuffle
          - command | shuffle
            - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reportingcommandassembler" data-diagram-title="ReportingCommandAssembler.java" aria-label="Abrir diagrama de clase para ReportingCommandAssembler.java"><code>ReportingCommandAssembler.java</code></button> | shuffle | secondary
          - query | shuffle
            - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reportingqueryassembler" data-diagram-title="ReportingQueryAssembler.java" aria-label="Abrir diagrama de clase para ReportingQueryAssembler.java"><code>ReportingQueryAssembler.java</code></button> | shuffle | secondary
          - result | shuffle
            - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reportingresultassembler" data-diagram-title="ReportingResultAssembler.java" aria-label="Abrir diagrama de clase para ReportingResultAssembler.java"><code>ReportingResultAssembler.java</code></button> | shuffle | secondary
        - exception | folder
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-applicationexception" data-diagram-title="ApplicationException.java" aria-label="Abrir diagrama de clase para ApplicationException.java"><code>ApplicationException.java</code></button> | file-code | code
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-authorizationdeniedexception" data-diagram-title="AuthorizationDeniedException.java" aria-label="Abrir diagrama de clase para AuthorizationDeniedException.java"><code>AuthorizationDeniedException.java</code></button> | file-code | code
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-tenantisolationexception" data-diagram-title="TenantIsolationException.java" aria-label="Abrir diagrama de clase para TenantIsolationException.java"><code>TenantIsolationException.java</code></button> | file-code | code
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-resourcenotfoundexception" data-diagram-title="ResourceNotFoundException.java" aria-label="Abrir diagrama de clase para ResourceNotFoundException.java"><code>ResourceNotFoundException.java</code></button> | file-code | code
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-idempotencyconflictexception" data-diagram-title="IdempotencyConflictException.java" aria-label="Abrir diagrama de clase para IdempotencyConflictException.java"><code>IdempotencyConflictException.java</code></button> | file-code | code
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reportartifactnotfoundexception" data-diagram-title="ReportArtifactNotFoundException.java" aria-label="Abrir diagrama de clase para ReportArtifactNotFoundException.java"><code>ReportArtifactNotFoundException.java</code></button> | file-code | code
```
{{% /tab %}}
{{% tab title="Infrastructure" %}}
```tree
- com | folder
  - arka | building | primary
    - reporting | microchip | primary
      - infrastructure | server | secondary
        - adapter | plug | secondary
          - in | arrow-right
            - web | globe
              - request | file-import
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-weeklysalesreportrequest" data-diagram-title="WeeklySalesReportRequest.java" aria-label="Abrir diagrama de clase para WeeklySalesReportRequest.java"><code>WeeklySalesReportRequest.java</code></button> | file-import | secondary
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-weeklyreplenishmentreportrequest" data-diagram-title="WeeklyReplenishmentReportRequest.java" aria-label="Abrir diagrama de clase para WeeklyReplenishmentReportRequest.java"><code>WeeklyReplenishmentReportRequest.java</code></button> | file-import | secondary
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-operationskpirequest" data-diagram-title="OperationsKpiRequest.java" aria-label="Abrir diagrama de clase para OperationsKpiRequest.java"><code>OperationsKpiRequest.java</code></button> | file-import | secondary
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reportartifactrequest" data-diagram-title="ReportArtifactRequest.java" aria-label="Abrir diagrama de clase para ReportArtifactRequest.java"><code>ReportArtifactRequest.java</code></button> | file-import | secondary
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-generateweeklyreportrequest" data-diagram-title="GenerateWeeklyReportRequest.java" aria-label="Abrir diagrama de clase para GenerateWeeklyReportRequest.java"><code>GenerateWeeklyReportRequest.java</code></button> | file-import | secondary
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-rebuildprojectionrequest" data-diagram-title="RebuildProjectionRequest.java" aria-label="Abrir diagrama de clase para RebuildProjectionRequest.java"><code>RebuildProjectionRequest.java</code></button> | file-import | secondary
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reprocessreportingdlqrequest" data-diagram-title="ReprocessReportingDlqRequest.java" aria-label="Abrir diagrama de clase para ReprocessReportingDlqRequest.java"><code>ReprocessReportingDlqRequest.java</code></button> | file-import | secondary
              - response | file-export
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-salesreportresponse" data-diagram-title="SalesReportResponse.java" aria-label="Abrir diagrama de clase para SalesReportResponse.java"><code>SalesReportResponse.java</code></button> | file-export | secondary
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-replenishmentreportresponse" data-diagram-title="ReplenishmentReportResponse.java" aria-label="Abrir diagrama de clase para ReplenishmentReportResponse.java"><code>ReplenishmentReportResponse.java</code></button> | file-export | secondary
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-operationskpiresponse" data-diagram-title="OperationsKpiResponse.java" aria-label="Abrir diagrama de clase para OperationsKpiResponse.java"><code>OperationsKpiResponse.java</code></button> | file-export | secondary
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reportartifactresponse" data-diagram-title="ReportArtifactResponse.java" aria-label="Abrir diagrama de clase para ReportArtifactResponse.java"><code>ReportArtifactResponse.java</code></button> | file-export | secondary
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-generateweeklyreportresponse" data-diagram-title="GenerateWeeklyReportResponse.java" aria-label="Abrir diagrama de clase para GenerateWeeklyReportResponse.java"><code>GenerateWeeklyReportResponse.java</code></button> | file-export | secondary
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-rebuildprojectionresponse" data-diagram-title="RebuildProjectionResponse.java" aria-label="Abrir diagrama de clase para RebuildProjectionResponse.java"><code>RebuildProjectionResponse.java</code></button> | file-export | secondary
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reportingdlqreprocessresponse" data-diagram-title="ReportingDlqReprocessResponse.java" aria-label="Abrir diagrama de clase para ReportingDlqReprocessResponse.java"><code>ReportingDlqReprocessResponse.java</code></button> | file-export | secondary
              - mapper | shuffle
                - command | shuffle
                  - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reportingcommandmapper" data-diagram-title="ReportingCommandMapper.java" aria-label="Abrir diagrama de clase para ReportingCommandMapper.java"><code>ReportingCommandMapper.java</code></button> | shuffle | secondary
                - query | shuffle
                  - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reportingquerymapper" data-diagram-title="ReportingQueryMapper.java" aria-label="Abrir diagrama de clase para ReportingQueryMapper.java"><code>ReportingQueryMapper.java</code></button> | shuffle | secondary
                - response | shuffle
                  - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reportingresponsemapper" data-diagram-title="ReportingResponseMapper.java" aria-label="Abrir diagrama de clase para ReportingResponseMapper.java"><code>ReportingResponseMapper.java</code></button> | shuffle | secondary
              - controller | globe
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reportingquerycontroller" data-diagram-title="ReportingQueryController.java" aria-label="Abrir diagrama de clase para ReportingQueryController.java"><code>ReportingQueryController.java</code></button> | globe | secondary
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reportingadmincontroller" data-diagram-title="ReportingAdminController.java" aria-label="Abrir diagrama de clase para ReportingAdminController.java"><code>ReportingAdminController.java</code></button> | globe | secondary
            - listener | bell
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-coreeventslistener" data-diagram-title="CoreEventsListener.java" aria-label="Abrir diagrama de clase para CoreEventsListener.java"><code>CoreEventsListener.java</code></button> | bell | secondary
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-notificationeventslistener" data-diagram-title="NotificationEventsListener.java" aria-label="Abrir diagrama de clase para NotificationEventsListener.java"><code>NotificationEventsListener.java</code></button> | bell | secondary
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-weeklyreportschedulerlistener" data-diagram-title="WeeklyReportSchedulerListener.java" aria-label="Abrir diagrama de clase para WeeklyReportSchedulerListener.java"><code>WeeklyReportSchedulerListener.java</code></button> | bell | secondary
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-projectionrebuildschedulerlistener" data-diagram-title="ProjectionRebuildSchedulerListener.java" aria-label="Abrir diagrama de clase para ProjectionRebuildSchedulerListener.java"><code>ProjectionRebuildSchedulerListener.java</code></button> | bell | secondary
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reportingdlqreprocessorlistener" data-diagram-title="ReportingDlqReprocessorListener.java" aria-label="Abrir diagrama de clase para ReportingDlqReprocessorListener.java"><code>ReportingDlqReprocessorListener.java</code></button> | bell | secondary
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-triggercontextresolver" data-diagram-title="TriggerContextResolver.java" aria-label="Abrir diagrama de clase para TriggerContextResolver.java"><code>TriggerContextResolver.java</code></button> | bell | secondary
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-triggercontext" data-diagram-title="TriggerContext.java" aria-label="Abrir diagrama de clase para TriggerContext.java"><code>TriggerContext.java</code></button> | bell | secondary
          - out | arrow-left
            - persistence | database
              - entity | table
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-analyticfactentity" data-diagram-title="AnalyticFactEntity.java" aria-label="Abrir diagrama de clase para AnalyticFactEntity.java"><code>AnalyticFactEntity.java</code></button> | table | secondary
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-salesprojectionentity" data-diagram-title="SalesProjectionEntity.java" aria-label="Abrir diagrama de clase para SalesProjectionEntity.java"><code>SalesProjectionEntity.java</code></button> | table | secondary
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-replenishmentprojectionentity" data-diagram-title="ReplenishmentProjectionEntity.java" aria-label="Abrir diagrama de clase para ReplenishmentProjectionEntity.java"><code>ReplenishmentProjectionEntity.java</code></button> | table | secondary
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-operationkpiprojectionentity" data-diagram-title="OperationKpiProjectionEntity.java" aria-label="Abrir diagrama de clase para OperationKpiProjectionEntity.java"><code>OperationKpiProjectionEntity.java</code></button> | table | secondary
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-weeklyreportexecutionentity" data-diagram-title="WeeklyReportExecutionEntity.java" aria-label="Abrir diagrama de clase para WeeklyReportExecutionEntity.java"><code>WeeklyReportExecutionEntity.java</code></button> | table | secondary
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reportartifactentity" data-diagram-title="ReportArtifactEntity.java" aria-label="Abrir diagrama de clase para ReportArtifactEntity.java"><code>ReportArtifactEntity.java</code></button> | table | secondary
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-consumercheckpointentity" data-diagram-title="ConsumerCheckpointEntity.java" aria-label="Abrir diagrama de clase para ConsumerCheckpointEntity.java"><code>ConsumerCheckpointEntity.java</code></button> | table | secondary
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reportingauditentity" data-diagram-title="ReportingAuditEntity.java" aria-label="Abrir diagrama de clase para ReportingAuditEntity.java"><code>ReportingAuditEntity.java</code></button> | table | secondary
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-outboxevententity" data-diagram-title="OutboxEventEntity.java" aria-label="Abrir diagrama de clase para OutboxEventEntity.java"><code>OutboxEventEntity.java</code></button> | table | secondary
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-processedevententity" data-diagram-title="ProcessedEventEntity.java" aria-label="Abrir diagrama de clase para ProcessedEventEntity.java"><code>ProcessedEventEntity.java</code></button> | table | secondary
              - mapper | shuffle
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-analyticfactpersistencemapper" data-diagram-title="AnalyticFactPersistenceMapper.java" aria-label="Abrir diagrama de clase para AnalyticFactPersistenceMapper.java"><code>AnalyticFactPersistenceMapper.java</code></button> | shuffle | secondary
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-salesprojectionpersistencemapper" data-diagram-title="SalesProjectionPersistenceMapper.java" aria-label="Abrir diagrama de clase para SalesProjectionPersistenceMapper.java"><code>SalesProjectionPersistenceMapper.java</code></button> | shuffle | secondary
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-replenishmentprojectionpersistencemapper" data-diagram-title="ReplenishmentProjectionPersistenceMapper.java" aria-label="Abrir diagrama de clase para ReplenishmentProjectionPersistenceMapper.java"><code>ReplenishmentProjectionPersistenceMapper.java</code></button> | shuffle | secondary
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-operationkpiprojectionpersistencemapper" data-diagram-title="OperationKpiProjectionPersistenceMapper.java" aria-label="Abrir diagrama de clase para OperationKpiProjectionPersistenceMapper.java"><code>OperationKpiProjectionPersistenceMapper.java</code></button> | shuffle | secondary
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-weeklyreportexecutionpersistencemapper" data-diagram-title="WeeklyReportExecutionPersistenceMapper.java" aria-label="Abrir diagrama de clase para WeeklyReportExecutionPersistenceMapper.java"><code>WeeklyReportExecutionPersistenceMapper.java</code></button> | shuffle | secondary
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reportartifactpersistencemapper" data-diagram-title="ReportArtifactPersistenceMapper.java" aria-label="Abrir diagrama de clase para ReportArtifactPersistenceMapper.java"><code>ReportArtifactPersistenceMapper.java</code></button> | shuffle | secondary
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-consumercheckpointpersistencemapper" data-diagram-title="ConsumerCheckpointPersistenceMapper.java" aria-label="Abrir diagrama de clase para ConsumerCheckpointPersistenceMapper.java"><code>ConsumerCheckpointPersistenceMapper.java</code></button> | shuffle | secondary
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reportingauditpersistencemapper" data-diagram-title="ReportingAuditPersistenceMapper.java" aria-label="Abrir diagrama de clase para ReportingAuditPersistenceMapper.java"><code>ReportingAuditPersistenceMapper.java</code></button> | shuffle | secondary
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-outboxeventpersistencemapper" data-diagram-title="OutboxEventPersistenceMapper.java" aria-label="Abrir diagrama de clase para OutboxEventPersistenceMapper.java"><code>OutboxEventPersistenceMapper.java</code></button> | shuffle | secondary
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-processedeventpersistencemapper" data-diagram-title="ProcessedEventPersistenceMapper.java" aria-label="Abrir diagrama de clase para ProcessedEventPersistenceMapper.java"><code>ProcessedEventPersistenceMapper.java</code></button> | shuffle | secondary
              - repository | database
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-analyticfactr2dbcrepositoryadapter" data-diagram-title="AnalyticFactR2dbcRepositoryAdapter.java" aria-label="Abrir diagrama de clase para AnalyticFactR2dbcRepositoryAdapter.java"><code>AnalyticFactR2dbcRepositoryAdapter.java</code></button> | database | secondary
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-salesprojectionr2dbcrepositoryadapter" data-diagram-title="SalesProjectionR2dbcRepositoryAdapter.java" aria-label="Abrir diagrama de clase para SalesProjectionR2dbcRepositoryAdapter.java"><code>SalesProjectionR2dbcRepositoryAdapter.java</code></button> | database | secondary
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-replenishmentprojectionr2dbcrepositoryadapter" data-diagram-title="ReplenishmentProjectionR2dbcRepositoryAdapter.java" aria-label="Abrir diagrama de clase para ReplenishmentProjectionR2dbcRepositoryAdapter.java"><code>ReplenishmentProjectionR2dbcRepositoryAdapter.java</code></button> | database | secondary
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-operationkpir2dbcrepositoryadapter" data-diagram-title="OperationKpiR2dbcRepositoryAdapter.java" aria-label="Abrir diagrama de clase para OperationKpiR2dbcRepositoryAdapter.java"><code>OperationKpiR2dbcRepositoryAdapter.java</code></button> | database | secondary
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-weeklyreportexecutionr2dbcrepositoryadapter" data-diagram-title="WeeklyReportExecutionR2dbcRepositoryAdapter.java" aria-label="Abrir diagrama de clase para WeeklyReportExecutionR2dbcRepositoryAdapter.java"><code>WeeklyReportExecutionR2dbcRepositoryAdapter.java</code></button> | database | secondary
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-consumercheckpointr2dbcrepositoryadapter" data-diagram-title="ConsumerCheckpointR2dbcRepositoryAdapter.java" aria-label="Abrir diagrama de clase para ConsumerCheckpointR2dbcRepositoryAdapter.java"><code>ConsumerCheckpointR2dbcRepositoryAdapter.java</code></button> | database | secondary
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reportartifactr2dbcrepositoryadapter" data-diagram-title="ReportArtifactR2dbcRepositoryAdapter.java" aria-label="Abrir diagrama de clase para ReportArtifactR2dbcRepositoryAdapter.java"><code>ReportArtifactR2dbcRepositoryAdapter.java</code></button> | database | secondary
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reportingauditr2dbcrepositoryadapter" data-diagram-title="ReportingAuditR2dbcRepositoryAdapter.java" aria-label="Abrir diagrama de clase para ReportingAuditR2dbcRepositoryAdapter.java"><code>ReportingAuditR2dbcRepositoryAdapter.java</code></button> | database | secondary
                - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-processedeventr2dbcrepositoryadapter" data-diagram-title="ProcessedEventR2dbcRepositoryAdapter.java" aria-label="Abrir diagrama de clase para ProcessedEventR2dbcRepositoryAdapter.java"><code>ProcessedEventR2dbcRepositoryAdapter.java</code></button> | database | secondary
            - external | cloud
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reportexporterstorageadapter" data-diagram-title="ReportExporterStorageAdapter.java" aria-label="Abrir diagrama de clase para ReportExporterStorageAdapter.java"><code>ReportExporterStorageAdapter.java</code></button> | cloud | secondary
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-directoryoperationalcountrypolicyhttpclientadapter" data-diagram-title="DirectoryOperationalCountryPolicyHttpClientAdapter.java" aria-label="Abrir diagrama de clase para DirectoryOperationalCountryPolicyHttpClientAdapter.java"><code>DirectoryOperationalCountryPolicyHttpClientAdapter.java</code></button> | cloud | secondary
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-systemclockadapter" data-diagram-title="SystemClockAdapter.java" aria-label="Abrir diagrama de clase para SystemClockAdapter.java"><code>SystemClockAdapter.java</code></button> | cloud | secondary
            - event | share-nodes | accent
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-outboxpersistenceadapter" data-diagram-title="OutboxPersistenceAdapter.java" aria-label="Abrir diagrama de clase para OutboxPersistenceAdapter.java"><code>OutboxPersistenceAdapter.java</code></button> | share-nodes | accent
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-kafkadomaineventpublisheradapter" data-diagram-title="KafkaDomainEventPublisherAdapter.java" aria-label="Abrir diagrama de clase para KafkaDomainEventPublisherAdapter.java"><code>KafkaDomainEventPublisherAdapter.java</code></button> | share-nodes | accent
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-outboxpublisherscheduler" data-diagram-title="OutboxPublisherScheduler.java" aria-label="Abrir diagrama de clase para OutboxPublisherScheduler.java"><code>OutboxPublisherScheduler.java</code></button> | clock | secondary
            - cache | hard-drive
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reportingcacheredisadapter" data-diagram-title="ReportingCacheRedisAdapter.java" aria-label="Abrir diagrama de clase para ReportingCacheRedisAdapter.java"><code>ReportingCacheRedisAdapter.java</code></button> | hard-drive | secondary
            - security | shield
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-principalcontextadapter" data-diagram-title="PrincipalContextAdapter.java" aria-label="Abrir diagrama de clase para PrincipalContextAdapter.java"><code>PrincipalContextAdapter.java</code></button> | shield | secondary
              - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-rbacpermissionevaluatoradapter" data-diagram-title="RbacPermissionEvaluatorAdapter.java" aria-label="Abrir diagrama de clase para RbacPermissionEvaluatorAdapter.java"><code>RbacPermissionEvaluatorAdapter.java</code></button> | shield | secondary
        - config | gear
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reportingsecurityconfiguration" data-diagram-title="ReportingSecurityConfiguration.java" aria-label="Abrir diagrama de clase para ReportingSecurityConfiguration.java"><code>ReportingSecurityConfiguration.java</code></button> | gear | secondary
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reportingkafkaconfiguration" data-diagram-title="ReportingKafkaConfiguration.java" aria-label="Abrir diagrama de clase para ReportingKafkaConfiguration.java"><code>ReportingKafkaConfiguration.java</code></button> | gear | secondary
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reportingr2dbcconfiguration" data-diagram-title="ReportingR2dbcConfiguration.java" aria-label="Abrir diagrama de clase para ReportingR2dbcConfiguration.java"><code>ReportingR2dbcConfiguration.java</code></button> | gear | secondary
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reportingstorageconfiguration" data-diagram-title="ReportingStorageConfiguration.java" aria-label="Abrir diagrama de clase para ReportingStorageConfiguration.java"><code>ReportingStorageConfiguration.java</code></button> | gear | secondary
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reportingschedulerconfiguration" data-diagram-title="ReportingSchedulerConfiguration.java" aria-label="Abrir diagrama de clase para ReportingSchedulerConfiguration.java"><code>ReportingSchedulerConfiguration.java</code></button> | gear | secondary
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reportingobservabilityconfiguration" data-diagram-title="ReportingObservabilityConfiguration.java" aria-label="Abrir diagrama de clase para ReportingObservabilityConfiguration.java"><code>ReportingObservabilityConfiguration.java</code></button> | gear | secondary
        - exception | folder
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-infrastructureexception" data-diagram-title="InfrastructureException.java" aria-label="Abrir diagrama de clase para InfrastructureException.java"><code>InfrastructureException.java</code></button> | file-code | code
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-externaldependencyunavailableexception" data-diagram-title="ExternalDependencyUnavailableException.java" aria-label="Abrir diagrama de clase para ExternalDependencyUnavailableException.java"><code>ExternalDependencyUnavailableException.java</code></button> | file-code | code
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-retryabledependencyexception" data-diagram-title="RetryableDependencyException.java" aria-label="Abrir diagrama de clase para RetryableDependencyException.java"><code>RetryableDependencyException.java</code></button> | file-code | code
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-nonretryabledependencyexception" data-diagram-title="NonRetryableDependencyException.java" aria-label="Abrir diagrama de clase para NonRetryableDependencyException.java"><code>NonRetryableDependencyException.java</code></button> | file-code | code
          - <button type="button" class="R-tree-diagram-trigger" data-diagram-template="reporting-class-reportingdependencyunavailableexception" data-diagram-title="ReportingDependencyUnavailableException.java" aria-label="Abrir diagrama de clase para ReportingDependencyUnavailableException.java"><code>ReportingDependencyUnavailableException.java</code></button> | file-code | code
```
{{% /tab %}}
{{% /tabs %}}

## Diagramas de clase por archivo
Cada archivo del tree tiene su propio diagrama de clase con una sola clase por script.

<script type="text/plain" id="reporting-class-analyticfactid">
classDiagram
  class AnalyticFactId {
    <<class>>
    +value: String
    +validate(): void
  }
</script>

<script type="text/plain" id="reporting-class-sourceeventid">
classDiagram
  class SourceEventId {
    <<class>>
    +value: String
    +validate(): void
  }
</script>

<script type="text/plain" id="reporting-class-factpayload">
classDiagram
  class FactPayload {
    <<class>>
    +value: String
    +validate(): void
  }
</script>

<script type="text/plain" id="reporting-class-analyticfactstatus">
classDiagram
  class AnalyticFactStatus {
    <<class>>
    +value: String
    +validate(): void
  }
</script>

<script type="text/plain" id="reporting-class-facttype">
classDiagram
  class FactType {
    <<class>>
    +value: String
    +validate(): void
  }
</script>

<script type="text/plain" id="reporting-class-analyticfactupdatedevent">
classDiagram
  class AnalyticFactUpdatedEvent {
    <<event>>
    +eventId: String
    +eventType: String
    +occurredAt: Instant
  }
</script>

<script type="text/plain" id="reporting-class-reportingviewaggregate">
classDiagram
  class ReportingViewAggregate {
    <<aggregate>>
    +id: String
    +version: long
    +apply(input: Object): void
    +raiseEvent(): Object
  }
</script>

<script type="text/plain" id="reporting-class-analyticfact">
classDiagram
  class AnalyticFact {
    <<class>>
    +value: String
    +validate(): void
  }
</script>

<script type="text/plain" id="reporting-class-projectionkey">
classDiagram
  class ProjectionKey {
    <<class>>
    +value: String
    +validate(): void
  }
</script>

<script type="text/plain" id="reporting-class-topproductmetric">
classDiagram
  class TopProductMetric {
    <<class>>
    +value: String
    +validate(): void
  }
</script>

<script type="text/plain" id="reporting-class-topcustomermetric">
classDiagram
  class TopCustomerMetric {
    <<class>>
    +value: String
    +validate(): void
  }
</script>

<script type="text/plain" id="reporting-class-coveragesnapshot">
classDiagram
  class CoverageSnapshot {
    <<class>>
    +value: String
    +validate(): void
  }
</script>

<script type="text/plain" id="reporting-class-kpimetric">
classDiagram
  class KpiMetric {
    <<class>>
    +value: String
    +validate(): void
  }
</script>

<script type="text/plain" id="reporting-class-risklevel">
classDiagram
  class RiskLevel {
    <<class>>
    +value: String
    +validate(): void
  }
</script>

<script type="text/plain" id="reporting-class-projectiontype">
classDiagram
  class ProjectionType {
    <<class>>
    +value: String
    +validate(): void
  }
</script>

<script type="text/plain" id="reporting-class-salesprojection">
classDiagram
  class SalesProjection {
    <<class>>
    +value: String
    +validate(): void
  }
</script>

<script type="text/plain" id="reporting-class-replenishmentprojection">
classDiagram
  class ReplenishmentProjection {
    <<class>>
    +value: String
    +validate(): void
  }
</script>

<script type="text/plain" id="reporting-class-operationkpiprojection">
classDiagram
  class OperationKpiProjection {
    <<class>>
    +value: String
    +validate(): void
  }
</script>

<script type="text/plain" id="reporting-class-reportartifact">
classDiagram
  class ReportArtifact {
    <<class>>
    +value: String
    +validate(): void
  }
</script>

<script type="text/plain" id="reporting-class-reportartifactref">
classDiagram
  class ReportArtifactRef {
    <<class>>
    +value: String
    +validate(): void
  }
</script>

<script type="text/plain" id="reporting-class-weekid">
classDiagram
  class WeekId {
    <<class>>
    +value: String
    +validate(): void
  }
</script>

<script type="text/plain" id="reporting-class-reportgenerationwindow">
classDiagram
  class ReportGenerationWindow {
    <<class>>
    +value: String
    +validate(): void
  }
</script>

<script type="text/plain" id="reporting-class-weeklyreportstatus">
classDiagram
  class WeeklyReportStatus {
    <<class>>
    +value: String
    +validate(): void
  }
</script>

<script type="text/plain" id="reporting-class-reportformat">
classDiagram
  class ReportFormat {
    <<class>>
    +value: String
    +validate(): void
  }
</script>

<script type="text/plain" id="reporting-class-reporttype">
classDiagram
  class ReportType {
    <<class>>
    +value: String
    +validate(): void
  }
</script>

<script type="text/plain" id="reporting-class-weeklyreportgeneratedevent">
classDiagram
  class WeeklyReportGeneratedEvent {
    <<event>>
    +eventId: String
    +eventType: String
    +occurredAt: Instant
  }
</script>

<script type="text/plain" id="reporting-class-weeklyreportaggregate">
classDiagram
  class WeeklyReportAggregate {
    <<aggregate>>
    +id: String
    +version: long
    +apply(input: Object): void
    +raiseEvent(): Object
  }
</script>

<script type="text/plain" id="reporting-class-consumerref">
classDiagram
  class ConsumerRef {
    <<class>>
    +value: String
    +validate(): void
  }
</script>

<script type="text/plain" id="reporting-class-kafkatopicpartition">
classDiagram
  class KafkaTopicPartition {
    <<class>>
    +value: String
    +validate(): void
  }
</script>

<script type="text/plain" id="reporting-class-offsetposition">
classDiagram
  class OffsetPosition {
    <<class>>
    +value: String
    +validate(): void
  }
</script>

<script type="text/plain" id="reporting-class-consumercheckpoint">
classDiagram
  class ConsumerCheckpoint {
    <<class>>
    +value: String
    +validate(): void
  }
</script>

<script type="text/plain" id="reporting-class-projectionpolicy">
classDiagram
  class ProjectionPolicy {
    <<policy>>
    +evaluate(input: Object): Decision
    +assertAllowed(input: Object): void
  }
</script>

<script type="text/plain" id="reporting-class-factdeduppolicy">
classDiagram
  class FactDedupPolicy {
    <<policy>>
    +evaluate(input: Object): Decision
    +assertAllowed(input: Object): void
  }
</script>

<script type="text/plain" id="reporting-class-weeklycutoffpolicy">
classDiagram
  class WeeklyCutoffPolicy {
    <<policy>>
    +evaluate(input: Object): Decision
    +assertAllowed(input: Object): void
  }
</script>

<script type="text/plain" id="reporting-class-tenantisolationpolicy">
classDiagram
  class TenantIsolationPolicy {
    <<policy>>
    +evaluate(input: Object): Decision
    +assertAllowed(input: Object): void
  }
</script>

<script type="text/plain" id="reporting-class-dataclassificationpolicy">
classDiagram
  class DataClassificationPolicy {
    <<policy>>
    +evaluate(input: Object): Decision
    +assertAllowed(input: Object): void
  }
</script>

<script type="text/plain" id="reporting-class-domainexception">
classDiagram
  class DomainException {
    <<exception>>
    +code: String
    +message: String
    +retryable: boolean
  }
</script>

<script type="text/plain" id="reporting-class-domainruleviolationexception">
classDiagram
  class DomainRuleViolationException {
    <<exception>>
    +code: String
    +message: String
    +retryable: boolean
  }
</script>

<script type="text/plain" id="reporting-class-conflictexception">
classDiagram
  class ConflictException {
    <<exception>>
    +code: String
    +message: String
    +retryable: boolean
  }
</script>

<script type="text/plain" id="reporting-class-analyticfactrejectedexception">
classDiagram
  class AnalyticFactRejectedException {
    <<exception>>
    +code: String
    +message: String
    +retryable: boolean
  }
</script>

<script type="text/plain" id="reporting-class-projectionwindowclosedexception">
classDiagram
  class ProjectionWindowClosedException {
    <<exception>>
    +code: String
    +message: String
    +retryable: boolean
  }
</script>

<script type="text/plain" id="reporting-class-weeklyreportgenerationconflictexception">
classDiagram
  class WeeklyReportGenerationConflictException {
    <<exception>>
    +code: String
    +message: String
    +retryable: boolean
  }
</script>

<script type="text/plain" id="reporting-class-registeranalyticfactport">
classDiagram
  class RegisterAnalyticFactPort {
    <<port>>
    +execute(input: Object): Mono~Object~
    +supports(context: Object): boolean
  }
</script>

<script type="text/plain" id="reporting-class-generateweeklyreportport">
classDiagram
  class GenerateWeeklyReportPort {
    <<port>>
    +execute(input: Object): Mono~Object~
    +supports(context: Object): boolean
  }
</script>

<script type="text/plain" id="reporting-class-rebuildprojectionport">
classDiagram
  class RebuildProjectionPort {
    <<port>>
    +execute(input: Object): Mono~Object~
    +supports(context: Object): boolean
  }
</script>

<script type="text/plain" id="reporting-class-reprocessreportingdlqport">
classDiagram
  class ReprocessReportingDlqPort {
    <<port>>
    +execute(input: Object): Mono~Object~
    +supports(context: Object): boolean
  }
</script>

<script type="text/plain" id="reporting-class-reportingqueryport">
classDiagram
  class ReportingQueryPort {
    <<port>>
    +execute(input: Object): Mono~Object~
    +supports(context: Object): boolean
  }
</script>

<script type="text/plain" id="reporting-class-analyticfactrepositoryport">
classDiagram
  class AnalyticFactRepositoryPort {
    <<port>>
    +execute(input: Object): Mono~Object~
    +supports(context: Object): boolean
  }
</script>

<script type="text/plain" id="reporting-class-salesprojectionrepositoryport">
classDiagram
  class SalesProjectionRepositoryPort {
    <<port>>
    +execute(input: Object): Mono~Object~
    +supports(context: Object): boolean
  }
</script>

<script type="text/plain" id="reporting-class-replenishmentprojectionrepositoryport">
classDiagram
  class ReplenishmentProjectionRepositoryPort {
    <<port>>
    +execute(input: Object): Mono~Object~
    +supports(context: Object): boolean
  }
</script>

<script type="text/plain" id="reporting-class-operationkpirepositoryport">
classDiagram
  class OperationKpiRepositoryPort {
    <<port>>
    +execute(input: Object): Mono~Object~
    +supports(context: Object): boolean
  }
</script>

<script type="text/plain" id="reporting-class-checkpointrepositoryport">
classDiagram
  class CheckpointRepositoryPort {
    <<port>>
    +execute(input: Object): Mono~Object~
    +supports(context: Object): boolean
  }
</script>

<script type="text/plain" id="reporting-class-weeklyreportexecutionrepositoryport">
classDiagram
  class WeeklyReportExecutionRepositoryPort {
    <<port>>
    +execute(input: Object): Mono~Object~
    +supports(context: Object): boolean
  }
</script>

<script type="text/plain" id="reporting-class-reportartifactrepositoryport">
classDiagram
  class ReportArtifactRepositoryPort {
    <<port>>
    +execute(input: Object): Mono~Object~
    +supports(context: Object): boolean
  }
</script>

<script type="text/plain" id="reporting-class-reportexporterport">
classDiagram
  class ReportExporterPort {
    <<port>>
    +execute(input: Object): Mono~Object~
    +supports(context: Object): boolean
  }
</script>

<script type="text/plain" id="reporting-class-directoryoperationalcountrypolicyport">
classDiagram
  class DirectoryOperationalCountryPolicyPort {
    <<port>>
    +execute(input: Object): Mono~Object~
    +supports(context: Object): boolean
  }
</script>

<script type="text/plain" id="reporting-class-clockport">
classDiagram
  class ClockPort {
    <<port>>
    +execute(input: Object): Mono~Object~
    +supports(context: Object): boolean
  }
</script>

<script type="text/plain" id="reporting-class-outboxport">
classDiagram
  class OutboxPort {
    <<port>>
    +execute(input: Object): Mono~Object~
    +supports(context: Object): boolean
  }
</script>

<script type="text/plain" id="reporting-class-processedeventport">
classDiagram
  class ProcessedEventPort {
    <<port>>
    +execute(input: Object): Mono~Object~
    +supports(context: Object): boolean
  }
</script>

<script type="text/plain" id="reporting-class-domaineventpublisherport">
classDiagram
  class DomainEventPublisherPort {
    <<port>>
    +execute(input: Object): Mono~Object~
    +supports(context: Object): boolean
  }
</script>

<script type="text/plain" id="reporting-class-reportingauditport">
classDiagram
  class ReportingAuditPort {
    <<port>>
    +execute(input: Object): Mono~Object~
    +supports(context: Object): boolean
  }
</script>

<script type="text/plain" id="reporting-class-reportingcacheport">
classDiagram
  class ReportingCachePort {
    <<port>>
    +execute(input: Object): Mono~Object~
    +supports(context: Object): boolean
  }
</script>

<script type="text/plain" id="reporting-class-principalcontextport">
classDiagram
  class PrincipalContextPort {
    <<port>>
    +execute(input: Object): Mono~Object~
    +supports(context: Object): boolean
  }
</script>

<script type="text/plain" id="reporting-class-principalcontext">
classDiagram
  class PrincipalContext {
    <<context>>
    +tenantId: String
    +principalRef: String
    +permissions: Set~String~
  }
</script>

<script type="text/plain" id="reporting-class-permissionevaluatorport">
classDiagram
  class PermissionEvaluatorPort {
    <<port>>
    +execute(input: Object): Mono~Object~
    +supports(context: Object): boolean
  }
</script>

<script type="text/plain" id="reporting-class-registeranalyticfactusecase">
classDiagram
  class RegisterAnalyticFactUseCase {
    <<usecase>>
    +handle(input: Object): Mono~Object~
    +validateContext(context: Object): void
  }
</script>

<script type="text/plain" id="reporting-class-refreshsalesprojectionusecase">
classDiagram
  class RefreshSalesProjectionUseCase {
    <<usecase>>
    +handle(input: Object): Mono~Object~
    +validateContext(context: Object): void
  }
</script>

<script type="text/plain" id="reporting-class-refreshreplenishmentprojectionusecase">
classDiagram
  class RefreshReplenishmentProjectionUseCase {
    <<usecase>>
    +handle(input: Object): Mono~Object~
    +validateContext(context: Object): void
  }
</script>

<script type="text/plain" id="reporting-class-refreshoperationkpiusecase">
classDiagram
  class RefreshOperationKpiUseCase {
    <<usecase>>
    +handle(input: Object): Mono~Object~
    +validateContext(context: Object): void
  }
</script>

<script type="text/plain" id="reporting-class-generateweeklyreportusecase">
classDiagram
  class GenerateWeeklyReportUseCase {
    <<usecase>>
    +handle(input: Object): Mono~Object~
    +validateContext(context: Object): void
  }
</script>

<script type="text/plain" id="reporting-class-rebuildprojectionusecase">
classDiagram
  class RebuildProjectionUseCase {
    <<usecase>>
    +handle(input: Object): Mono~Object~
    +validateContext(context: Object): void
  }
</script>

<script type="text/plain" id="reporting-class-reprocessreportingdlqusecase">
classDiagram
  class ReprocessReportingDlqUseCase {
    <<usecase>>
    +handle(input: Object): Mono~Object~
    +validateContext(context: Object): void
  }
</script>

<script type="text/plain" id="reporting-class-getweeklysalesreportusecase">
classDiagram
  class GetWeeklySalesReportUseCase {
    <<usecase>>
    +handle(input: Object): Mono~Object~
    +validateContext(context: Object): void
  }
</script>

<script type="text/plain" id="reporting-class-getweeklyreplenishmentreportusecase">
classDiagram
  class GetWeeklyReplenishmentReportUseCase {
    <<usecase>>
    +handle(input: Object): Mono~Object~
    +validateContext(context: Object): void
  }
</script>

<script type="text/plain" id="reporting-class-getoperationskpiusecase">
classDiagram
  class GetOperationsKpiUseCase {
    <<usecase>>
    +handle(input: Object): Mono~Object~
    +validateContext(context: Object): void
  }
</script>

<script type="text/plain" id="reporting-class-getreportartifactusecase">
classDiagram
  class GetReportArtifactUseCase {
    <<usecase>>
    +handle(input: Object): Mono~Object~
    +validateContext(context: Object): void
  }
</script>

<script type="text/plain" id="reporting-class-registeranalyticfactcommand">
classDiagram
  class RegisterAnalyticFactCommand {
    <<command>>
    +tenantId: String
    +traceId: String
    +idempotencyKey: String
  }
</script>

<script type="text/plain" id="reporting-class-generateweeklyreportcommand">
classDiagram
  class GenerateWeeklyReportCommand {
    <<command>>
    +tenantId: String
    +traceId: String
    +idempotencyKey: String
  }
</script>

<script type="text/plain" id="reporting-class-rebuildprojectioncommand">
classDiagram
  class RebuildProjectionCommand {
    <<command>>
    +tenantId: String
    +traceId: String
    +idempotencyKey: String
  }
</script>

<script type="text/plain" id="reporting-class-reprocessreportingdlqcommand">
classDiagram
  class ReprocessReportingDlqCommand {
    <<command>>
    +tenantId: String
    +traceId: String
    +idempotencyKey: String
  }
</script>

<script type="text/plain" id="reporting-class-weeklysalesreportquery">
classDiagram
  class WeeklySalesReportQuery {
    <<query>>
    +tenantId: String
    +countryCode: String
    +periodRef: String
  }
</script>

<script type="text/plain" id="reporting-class-weeklyreplenishmentreportquery">
classDiagram
  class WeeklyReplenishmentReportQuery {
    <<query>>
    +tenantId: String
    +countryCode: String
    +periodRef: String
  }
</script>

<script type="text/plain" id="reporting-class-operationskpiquery">
classDiagram
  class OperationsKpiQuery {
    <<query>>
    +tenantId: String
    +countryCode: String
    +periodRef: String
  }
</script>

<script type="text/plain" id="reporting-class-reportartifactquery">
classDiagram
  class ReportArtifactQuery {
    <<query>>
    +tenantId: String
    +countryCode: String
    +periodRef: String
  }
</script>

<script type="text/plain" id="reporting-class-salesreportresult">
classDiagram
  class SalesReportResult {
    <<result>>
    +status: String
    +traceId: String
    +generatedAt: Instant
  }
</script>

<script type="text/plain" id="reporting-class-replenishmentreportresult">
classDiagram
  class ReplenishmentReportResult {
    <<result>>
    +status: String
    +traceId: String
    +generatedAt: Instant
  }
</script>

<script type="text/plain" id="reporting-class-operationskpiresult">
classDiagram
  class OperationsKpiResult {
    <<result>>
    +status: String
    +traceId: String
    +generatedAt: Instant
  }
</script>

<script type="text/plain" id="reporting-class-reportartifactresult">
classDiagram
  class ReportArtifactResult {
    <<result>>
    +status: String
    +traceId: String
    +generatedAt: Instant
  }
</script>

<script type="text/plain" id="reporting-class-generateweeklyreportresult">
classDiagram
  class GenerateWeeklyReportResult {
    <<result>>
    +status: String
    +traceId: String
    +generatedAt: Instant
  }
</script>

<script type="text/plain" id="reporting-class-rebuildprojectionresult">
classDiagram
  class RebuildProjectionResult {
    <<result>>
    +status: String
    +traceId: String
    +generatedAt: Instant
  }
</script>

<script type="text/plain" id="reporting-class-reportingdlqreprocessresult">
classDiagram
  class ReportingDlqReprocessResult {
    <<result>>
    +status: String
    +traceId: String
    +generatedAt: Instant
  }
</script>

<script type="text/plain" id="reporting-class-reportingcommandassembler">
classDiagram
  class ReportingCommandAssembler {
    <<assembler>>
    +toCommand(input: Object): Object
    +toQuery(input: Object): Object
    +toResult(input: Object): Object
  }
</script>

<script type="text/plain" id="reporting-class-reportingqueryassembler">
classDiagram
  class ReportingQueryAssembler {
    <<assembler>>
    +toCommand(input: Object): Object
    +toQuery(input: Object): Object
    +toResult(input: Object): Object
  }
</script>

<script type="text/plain" id="reporting-class-reportingresultassembler">
classDiagram
  class ReportingResultAssembler {
    <<assembler>>
    +toCommand(input: Object): Object
    +toQuery(input: Object): Object
    +toResult(input: Object): Object
  }
</script>

<script type="text/plain" id="reporting-class-applicationexception">
classDiagram
  class ApplicationException {
    <<exception>>
    +code: String
    +message: String
    +retryable: boolean
  }
</script>

<script type="text/plain" id="reporting-class-authorizationdeniedexception">
classDiagram
  class AuthorizationDeniedException {
    <<exception>>
    +code: String
    +message: String
    +retryable: boolean
  }
</script>

<script type="text/plain" id="reporting-class-tenantisolationexception">
classDiagram
  class TenantIsolationException {
    <<exception>>
    +code: String
    +message: String
    +retryable: boolean
  }
</script>

<script type="text/plain" id="reporting-class-resourcenotfoundexception">
classDiagram
  class ResourceNotFoundException {
    <<exception>>
    +code: String
    +message: String
    +retryable: boolean
  }
</script>

<script type="text/plain" id="reporting-class-idempotencyconflictexception">
classDiagram
  class IdempotencyConflictException {
    <<exception>>
    +code: String
    +message: String
    +retryable: boolean
  }
</script>

<script type="text/plain" id="reporting-class-reportartifactnotfoundexception">
classDiagram
  class ReportArtifactNotFoundException {
    <<exception>>
    +code: String
    +message: String
    +retryable: boolean
  }
</script>

<script type="text/plain" id="reporting-class-weeklysalesreportrequest">
classDiagram
  class WeeklySalesReportRequest {
    <<request>>
    +tenantId: String
    +countryCode: String
    +operationRef: String
  }
</script>

<script type="text/plain" id="reporting-class-weeklyreplenishmentreportrequest">
classDiagram
  class WeeklyReplenishmentReportRequest {
    <<request>>
    +tenantId: String
    +countryCode: String
    +operationRef: String
  }
</script>

<script type="text/plain" id="reporting-class-operationskpirequest">
classDiagram
  class OperationsKpiRequest {
    <<request>>
    +tenantId: String
    +countryCode: String
    +operationRef: String
  }
</script>

<script type="text/plain" id="reporting-class-reportartifactrequest">
classDiagram
  class ReportArtifactRequest {
    <<request>>
    +tenantId: String
    +countryCode: String
    +operationRef: String
  }
</script>

<script type="text/plain" id="reporting-class-generateweeklyreportrequest">
classDiagram
  class GenerateWeeklyReportRequest {
    <<request>>
    +tenantId: String
    +countryCode: String
    +operationRef: String
  }
</script>

<script type="text/plain" id="reporting-class-rebuildprojectionrequest">
classDiagram
  class RebuildProjectionRequest {
    <<request>>
    +tenantId: String
    +countryCode: String
    +operationRef: String
  }
</script>

<script type="text/plain" id="reporting-class-reprocessreportingdlqrequest">
classDiagram
  class ReprocessReportingDlqRequest {
    <<request>>
    +tenantId: String
    +countryCode: String
    +operationRef: String
  }
</script>

<script type="text/plain" id="reporting-class-salesreportresponse">
classDiagram
  class SalesReportResponse {
    <<response>>
    +status: String
    +traceId: String
    +correlationId: String
  }
</script>

<script type="text/plain" id="reporting-class-replenishmentreportresponse">
classDiagram
  class ReplenishmentReportResponse {
    <<response>>
    +status: String
    +traceId: String
    +correlationId: String
  }
</script>

<script type="text/plain" id="reporting-class-operationskpiresponse">
classDiagram
  class OperationsKpiResponse {
    <<response>>
    +status: String
    +traceId: String
    +correlationId: String
  }
</script>

<script type="text/plain" id="reporting-class-reportartifactresponse">
classDiagram
  class ReportArtifactResponse {
    <<response>>
    +status: String
    +traceId: String
    +correlationId: String
  }
</script>

<script type="text/plain" id="reporting-class-generateweeklyreportresponse">
classDiagram
  class GenerateWeeklyReportResponse {
    <<response>>
    +status: String
    +traceId: String
    +correlationId: String
  }
</script>

<script type="text/plain" id="reporting-class-rebuildprojectionresponse">
classDiagram
  class RebuildProjectionResponse {
    <<response>>
    +status: String
    +traceId: String
    +correlationId: String
  }
</script>

<script type="text/plain" id="reporting-class-reportingdlqreprocessresponse">
classDiagram
  class ReportingDlqReprocessResponse {
    <<response>>
    +status: String
    +traceId: String
    +correlationId: String
  }
</script>

<script type="text/plain" id="reporting-class-reportingcommandmapper">
classDiagram
  class ReportingCommandMapper {
    <<mapper>>
    +toDomain(input: Object): Object
    +toContract(input: Object): Object
  }
</script>

<script type="text/plain" id="reporting-class-reportingquerymapper">
classDiagram
  class ReportingQueryMapper {
    <<mapper>>
    +toDomain(input: Object): Object
    +toContract(input: Object): Object
  }
</script>

<script type="text/plain" id="reporting-class-reportingresponsemapper">
classDiagram
  class ReportingResponseMapper {
    <<mapper>>
    +toDomain(input: Object): Object
    +toContract(input: Object): Object
  }
</script>

<script type="text/plain" id="reporting-class-reportingquerycontroller">
classDiagram
  class ReportingQueryController {
    <<controller>>
    +handleRequest(request: Object): Mono~Object~
    +mapError(error: Throwable): Mono~Object~
  }
</script>

<script type="text/plain" id="reporting-class-reportingadmincontroller">
classDiagram
  class ReportingAdminController {
    <<controller>>
    +handleRequest(request: Object): Mono~Object~
    +mapError(error: Throwable): Mono~Object~
  }
</script>

<script type="text/plain" id="reporting-class-coreeventslistener">
classDiagram
  class CoreEventsListener {
    <<listener>>
    +onEvent(envelope: Object): Mono~Void~
    +resolveTrigger(envelope: Object): TriggerContext
  }
</script>

<script type="text/plain" id="reporting-class-notificationeventslistener">
classDiagram
  class NotificationEventsListener {
    <<listener>>
    +onEvent(envelope: Object): Mono~Void~
    +resolveTrigger(envelope: Object): TriggerContext
  }
</script>

<script type="text/plain" id="reporting-class-weeklyreportschedulerlistener">
classDiagram
  class WeeklyReportSchedulerListener {
    <<listener>>
    +onEvent(envelope: Object): Mono~Void~
    +resolveTrigger(envelope: Object): TriggerContext
  }
</script>

<script type="text/plain" id="reporting-class-projectionrebuildschedulerlistener">
classDiagram
  class ProjectionRebuildSchedulerListener {
    <<listener>>
    +onEvent(envelope: Object): Mono~Void~
    +resolveTrigger(envelope: Object): TriggerContext
  }
</script>

<script type="text/plain" id="reporting-class-reportingdlqreprocessorlistener">
classDiagram
  class ReportingDlqReprocessorListener {
    <<listener>>
    +onEvent(envelope: Object): Mono~Void~
    +resolveTrigger(envelope: Object): TriggerContext
  }
</script>

<script type="text/plain" id="reporting-class-triggercontextresolver">
classDiagram
  class TriggerContextResolver {
    <<resolver>>
    +resolve(input: Object): TriggerContext
  }
</script>

<script type="text/plain" id="reporting-class-triggercontext">
classDiagram
  class TriggerContext {
    <<class>>
    +value: String
    +validate(): void
  }
</script>

<script type="text/plain" id="reporting-class-analyticfactentity">
classDiagram
  class AnalyticFactEntity {
    <<entity>>
    +id: String
    +tenantId: String
    +updatedAt: Instant
  }
</script>

<script type="text/plain" id="reporting-class-salesprojectionentity">
classDiagram
  class SalesProjectionEntity {
    <<entity>>
    +id: String
    +tenantId: String
    +updatedAt: Instant
  }
</script>

<script type="text/plain" id="reporting-class-replenishmentprojectionentity">
classDiagram
  class ReplenishmentProjectionEntity {
    <<entity>>
    +id: String
    +tenantId: String
    +updatedAt: Instant
  }
</script>

<script type="text/plain" id="reporting-class-operationkpiprojectionentity">
classDiagram
  class OperationKpiProjectionEntity {
    <<entity>>
    +id: String
    +tenantId: String
    +updatedAt: Instant
  }
</script>

<script type="text/plain" id="reporting-class-weeklyreportexecutionentity">
classDiagram
  class WeeklyReportExecutionEntity {
    <<entity>>
    +id: String
    +tenantId: String
    +updatedAt: Instant
  }
</script>

<script type="text/plain" id="reporting-class-reportartifactentity">
classDiagram
  class ReportArtifactEntity {
    <<entity>>
    +id: String
    +tenantId: String
    +updatedAt: Instant
  }
</script>

<script type="text/plain" id="reporting-class-consumercheckpointentity">
classDiagram
  class ConsumerCheckpointEntity {
    <<entity>>
    +id: String
    +tenantId: String
    +updatedAt: Instant
  }
</script>

<script type="text/plain" id="reporting-class-reportingauditentity">
classDiagram
  class ReportingAuditEntity {
    <<entity>>
    +id: String
    +tenantId: String
    +updatedAt: Instant
  }
</script>

<script type="text/plain" id="reporting-class-outboxevententity">
classDiagram
  class OutboxEventEntity {
    <<entity>>
    +id: String
    +tenantId: String
    +updatedAt: Instant
  }
</script>

<script type="text/plain" id="reporting-class-processedevententity">
classDiagram
  class ProcessedEventEntity {
    <<entity>>
    +id: String
    +tenantId: String
    +updatedAt: Instant
  }
</script>

<script type="text/plain" id="reporting-class-analyticfactpersistencemapper">
classDiagram
  class AnalyticFactPersistenceMapper {
    <<mapper>>
    +toDomain(input: Object): Object
    +toContract(input: Object): Object
  }
</script>

<script type="text/plain" id="reporting-class-salesprojectionpersistencemapper">
classDiagram
  class SalesProjectionPersistenceMapper {
    <<mapper>>
    +toDomain(input: Object): Object
    +toContract(input: Object): Object
  }
</script>

<script type="text/plain" id="reporting-class-replenishmentprojectionpersistencemapper">
classDiagram
  class ReplenishmentProjectionPersistenceMapper {
    <<mapper>>
    +toDomain(input: Object): Object
    +toContract(input: Object): Object
  }
</script>

<script type="text/plain" id="reporting-class-operationkpiprojectionpersistencemapper">
classDiagram
  class OperationKpiProjectionPersistenceMapper {
    <<mapper>>
    +toDomain(input: Object): Object
    +toContract(input: Object): Object
  }
</script>

<script type="text/plain" id="reporting-class-weeklyreportexecutionpersistencemapper">
classDiagram
  class WeeklyReportExecutionPersistenceMapper {
    <<mapper>>
    +toDomain(input: Object): Object
    +toContract(input: Object): Object
  }
</script>

<script type="text/plain" id="reporting-class-reportartifactpersistencemapper">
classDiagram
  class ReportArtifactPersistenceMapper {
    <<mapper>>
    +toDomain(input: Object): Object
    +toContract(input: Object): Object
  }
</script>

<script type="text/plain" id="reporting-class-consumercheckpointpersistencemapper">
classDiagram
  class ConsumerCheckpointPersistenceMapper {
    <<mapper>>
    +toDomain(input: Object): Object
    +toContract(input: Object): Object
  }
</script>

<script type="text/plain" id="reporting-class-reportingauditpersistencemapper">
classDiagram
  class ReportingAuditPersistenceMapper {
    <<mapper>>
    +toDomain(input: Object): Object
    +toContract(input: Object): Object
  }
</script>

<script type="text/plain" id="reporting-class-outboxeventpersistencemapper">
classDiagram
  class OutboxEventPersistenceMapper {
    <<mapper>>
    +toDomain(input: Object): Object
    +toContract(input: Object): Object
  }
</script>

<script type="text/plain" id="reporting-class-processedeventpersistencemapper">
classDiagram
  class ProcessedEventPersistenceMapper {
    <<mapper>>
    +toDomain(input: Object): Object
    +toContract(input: Object): Object
  }
</script>

<script type="text/plain" id="reporting-class-analyticfactr2dbcrepositoryadapter">
classDiagram
  class AnalyticFactR2dbcRepositoryAdapter {
    <<adapter>>
    +execute(input: Object): Mono~Object~
    +translateError(error: Throwable): RuntimeException
  }
</script>

<script type="text/plain" id="reporting-class-salesprojectionr2dbcrepositoryadapter">
classDiagram
  class SalesProjectionR2dbcRepositoryAdapter {
    <<adapter>>
    +execute(input: Object): Mono~Object~
    +translateError(error: Throwable): RuntimeException
  }
</script>

<script type="text/plain" id="reporting-class-replenishmentprojectionr2dbcrepositoryadapter">
classDiagram
  class ReplenishmentProjectionR2dbcRepositoryAdapter {
    <<adapter>>
    +execute(input: Object): Mono~Object~
    +translateError(error: Throwable): RuntimeException
  }
</script>

<script type="text/plain" id="reporting-class-operationkpir2dbcrepositoryadapter">
classDiagram
  class OperationKpiR2dbcRepositoryAdapter {
    <<adapter>>
    +execute(input: Object): Mono~Object~
    +translateError(error: Throwable): RuntimeException
  }
</script>

<script type="text/plain" id="reporting-class-weeklyreportexecutionr2dbcrepositoryadapter">
classDiagram
  class WeeklyReportExecutionR2dbcRepositoryAdapter {
    <<adapter>>
    +execute(input: Object): Mono~Object~
    +translateError(error: Throwable): RuntimeException
  }
</script>

<script type="text/plain" id="reporting-class-consumercheckpointr2dbcrepositoryadapter">
classDiagram
  class ConsumerCheckpointR2dbcRepositoryAdapter {
    <<adapter>>
    +execute(input: Object): Mono~Object~
    +translateError(error: Throwable): RuntimeException
  }
</script>

<script type="text/plain" id="reporting-class-reportartifactr2dbcrepositoryadapter">
classDiagram
  class ReportArtifactR2dbcRepositoryAdapter {
    <<adapter>>
    +execute(input: Object): Mono~Object~
    +translateError(error: Throwable): RuntimeException
  }
</script>

<script type="text/plain" id="reporting-class-reportingauditr2dbcrepositoryadapter">
classDiagram
  class ReportingAuditR2dbcRepositoryAdapter {
    <<adapter>>
    +execute(input: Object): Mono~Object~
    +translateError(error: Throwable): RuntimeException
  }
</script>

<script type="text/plain" id="reporting-class-processedeventr2dbcrepositoryadapter">
classDiagram
  class ProcessedEventR2dbcRepositoryAdapter {
    <<adapter>>
    +execute(input: Object): Mono~Object~
    +translateError(error: Throwable): RuntimeException
  }
</script>

<script type="text/plain" id="reporting-class-reportexporterstorageadapter">
classDiagram
  class ReportExporterStorageAdapter {
    <<adapter>>
    +execute(input: Object): Mono~Object~
    +translateError(error: Throwable): RuntimeException
  }
</script>

<script type="text/plain" id="reporting-class-directoryoperationalcountrypolicyhttpclientadapter">
classDiagram
  class DirectoryOperationalCountryPolicyHttpClientAdapter {
    <<adapter>>
    +execute(input: Object): Mono~Object~
    +translateError(error: Throwable): RuntimeException
  }
</script>

<script type="text/plain" id="reporting-class-systemclockadapter">
classDiagram
  class SystemClockAdapter {
    <<adapter>>
    +execute(input: Object): Mono~Object~
    +translateError(error: Throwable): RuntimeException
  }
</script>

<script type="text/plain" id="reporting-class-outboxpersistenceadapter">
classDiagram
  class OutboxPersistenceAdapter {
    <<adapter>>
    +execute(input: Object): Mono~Object~
    +translateError(error: Throwable): RuntimeException
  }
</script>

<script type="text/plain" id="reporting-class-kafkadomaineventpublisheradapter">
classDiagram
  class KafkaDomainEventPublisherAdapter {
    <<adapter>>
    +execute(input: Object): Mono~Object~
    +translateError(error: Throwable): RuntimeException
  }
</script>

<script type="text/plain" id="reporting-class-outboxpublisherscheduler">
classDiagram
  class OutboxPublisherScheduler {
    <<scheduler>>
    +run(): Mono~Void~
  }
</script>

<script type="text/plain" id="reporting-class-reportingcacheredisadapter">
classDiagram
  class ReportingCacheRedisAdapter {
    <<adapter>>
    +execute(input: Object): Mono~Object~
    +translateError(error: Throwable): RuntimeException
  }
</script>

<script type="text/plain" id="reporting-class-principalcontextadapter">
classDiagram
  class PrincipalContextAdapter {
    <<adapter>>
    +execute(input: Object): Mono~Object~
    +translateError(error: Throwable): RuntimeException
  }
</script>

<script type="text/plain" id="reporting-class-rbacpermissionevaluatoradapter">
classDiagram
  class RbacPermissionEvaluatorAdapter {
    <<adapter>>
    +execute(input: Object): Mono~Object~
    +translateError(error: Throwable): RuntimeException
  }
</script>

<script type="text/plain" id="reporting-class-reportingsecurityconfiguration">
classDiagram
  class ReportingSecurityConfiguration {
    <<config>>
    +configure(): void
  }
</script>

<script type="text/plain" id="reporting-class-reportingkafkaconfiguration">
classDiagram
  class ReportingKafkaConfiguration {
    <<config>>
    +configure(): void
  }
</script>

<script type="text/plain" id="reporting-class-reportingr2dbcconfiguration">
classDiagram
  class ReportingR2dbcConfiguration {
    <<config>>
    +configure(): void
  }
</script>

<script type="text/plain" id="reporting-class-reportingstorageconfiguration">
classDiagram
  class ReportingStorageConfiguration {
    <<config>>
    +configure(): void
  }
</script>

<script type="text/plain" id="reporting-class-reportingschedulerconfiguration">
classDiagram
  class ReportingSchedulerConfiguration {
    <<config>>
    +configure(): void
  }
</script>

<script type="text/plain" id="reporting-class-reportingobservabilityconfiguration">
classDiagram
  class ReportingObservabilityConfiguration {
    <<config>>
    +configure(): void
  }
</script>

<script type="text/plain" id="reporting-class-infrastructureexception">
classDiagram
  class InfrastructureException {
    <<exception>>
    +code: String
    +message: String
    +retryable: boolean
  }
</script>

<script type="text/plain" id="reporting-class-externaldependencyunavailableexception">
classDiagram
  class ExternalDependencyUnavailableException {
    <<exception>>
    +code: String
    +message: String
    +retryable: boolean
  }
</script>

<script type="text/plain" id="reporting-class-retryabledependencyexception">
classDiagram
  class RetryableDependencyException {
    <<exception>>
    +code: String
    +message: String
    +retryable: boolean
  }
</script>

<script type="text/plain" id="reporting-class-nonretryabledependencyexception">
classDiagram
  class NonRetryableDependencyException {
    <<exception>>
    +code: String
    +message: String
    +retryable: boolean
  }
</script>

<script type="text/plain" id="reporting-class-reportingdependencyunavailableexception">
classDiagram
  class ReportingDependencyUnavailableException {
    <<exception>>
    +code: String
    +message: String
    +retryable: boolean
  }
</script>

## Matriz de ownership por capa
| Capa | Objetivo principal | Artefactos representativos |
|---|---|---|
| `Domain` | Semantica analitica, invariantes y politicas de corte/aislamiento | `ReportingViewAggregate`, `WeeklyReportAggregate`, `ProjectionPolicy`, `WeeklyCutoffPolicy`, `TenantIsolationPolicy` |
| `Application` | Orquestacion de casos de uso, contratos internos y cierre de autorizacion contextual | `*UseCase`, `PrincipalContextPort`, `PermissionEvaluatorPort`, `*Command`, `*Query`, `*Result` |
| `Infrastructure` | Adaptadores de entrada/salida, persistencia, exportacion, cache, seguridad tecnica y publicacion EDA | `ReportingQueryController`, `ReportingAdminController`, `CoreEventsListener`, `NotificationEventsListener`, `*R2dbcRepositoryAdapter`, `OutboxPublisherScheduler`, `PrincipalContextAdapter`, `RbacPermissionEvaluatorAdapter`, `TriggerContextResolver` |

## Inventario por ruta canonica
| Ruta | Clases |
|---|---|
| `application/command` | `RegisterAnalyticFactCommand`, `GenerateWeeklyReportCommand`, `RebuildProjectionCommand`, `ReprocessReportingDlqCommand` |
| `application/query` | `WeeklySalesReportQuery`, `WeeklyReplenishmentReportQuery`, `OperationsKpiQuery`, `ReportArtifactQuery` |
| `application/result` | `SalesReportResult`, `ReplenishmentReportResult`, `OperationsKpiResult`, `ReportArtifactResult`, `GenerateWeeklyReportResult`, `RebuildProjectionResult`, `ReportingDlqReprocessResult` |
| `application/mapper/command` | `ReportingCommandAssembler` |
| `application/mapper/query` | `ReportingQueryAssembler` |
| `application/mapper/result` | `ReportingResultAssembler` |
| `infrastructure/adapter/in/web/request` | `WeeklySalesReportRequest`, `WeeklyReplenishmentReportRequest`, `OperationsKpiRequest`, `ReportArtifactRequest`, `GenerateWeeklyReportRequest`, `RebuildProjectionRequest`, `ReprocessReportingDlqRequest` |
| `infrastructure/adapter/in/web/response` | `SalesReportResponse`, `ReplenishmentReportResponse`, `OperationsKpiResponse`, `ReportArtifactResponse`, `GenerateWeeklyReportResponse`, `RebuildProjectionResponse`, `ReportingDlqReprocessResponse` |
| `infrastructure/adapter/in/web/mapper/command` | `ReportingCommandMapper` |
| `infrastructure/adapter/in/web/mapper/query` | `ReportingQueryMapper` |
| `infrastructure/adapter/in/web/mapper/response` | `ReportingResponseMapper` |
| `application/port/out/security` | `PrincipalContext`, `PrincipalContextPort`, `PermissionEvaluatorPort` |
| `infrastructure/adapter/out/security` | `PrincipalContextAdapter`, `RbacPermissionEvaluatorAdapter` |
| `infrastructure/adapter/in/listener` | `CoreEventsListener`, `NotificationEventsListener`, `WeeklyReportSchedulerListener`, `ProjectionRebuildSchedulerListener`, `ReportingDlqReprocessorListener`, `TriggerContextResolver`, `TriggerContext` |

## Restricciones de alineamiento
- `MUST`: los triggers HTTP se autorizan por `PrincipalContext` y politicas de dominio, no por filtros ad-hoc.
- `MUST`: los triggers async/scheduler usan `TriggerContext` y dedupe por `ProcessedEventPort` antes de mutar proyecciones.
- `MUST`: `GenerateWeeklyReportUseCase` y `RebuildProjectionUseCase` validan politica regional vigente por `countryCode` via `DirectoryOperationalCountryPolicyPort`.
- `MUST`: `AnalyticFactUpdated` y `WeeklyReportGenerated` salen por outbox transaccional (`OutboxPort` + `OutboxPublisherScheduler`).
