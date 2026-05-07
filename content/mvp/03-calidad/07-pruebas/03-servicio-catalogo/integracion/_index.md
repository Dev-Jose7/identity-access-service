---
title: "Integracion - servicio-catalogo"
linkTitle: "2. Integracion"
weight: 2
url: "/mvp/calidad/pruebas/servicio-catalogo/integracion/"
---

## Objetivo
Validar contratos REST/eventos, persistencia, proyecciones de consulta, idempotencia y dedupe de Catalog.

## Dependencias y entornos
- BD del modelo Catalog (producto, variante, precio, agenda de precio, taxonomia referencial, auditoria, outbox, processed_event).
- broker/event bus para publicacion/consumo.
- contexto de seguridad para usuarios admin, usuarios de consulta y `trusted_service(order-service)`.

## Datos de entrada
- requests/responses de contratos `v1`.
- headers `Authorization`, `Idempotency-Key`, `X-Request-Id` cuando aplique.
- eventos entrantes Inventory: `inventory.stock-updated.v1`, `inventory.sku-reconciled.v1`.

## Matriz detallada de casos de integracion
| ID | Escenario | Validacion principal | Evidencia | Trazabilidad |
|---|---|---|---|---|
| CAT-IT-001 | `POST /products` exitoso | contrato + persistencia producto | 201 + fila producto + `catalog_audit` + outbox `ProductCreated` | FR-001, NFR-006 |
| CAT-IT-002 | `PATCH /products/{id}` | actualizacion de producto | 200 + cambios + outbox `ProductUpdated` | FR-001 |
| CAT-IT-003 | `POST /products/{id}/activate` | activacion de producto | 200 + `status=ACTIVE` + outbox `ProductActivated` | FR-001 |
| CAT-IT-004 | `POST /products/{id}/retire` | retiro de producto | 200 + `status=RETIRED` + outbox `ProductRetired` | FR-001 |
| CAT-IT-005 | `POST /products/{id}/variants` | alta de variante | 201 + fila variante + outbox `VariantCreated` | FR-001 |
| CAT-IT-006 | `PATCH /variants/{id}` | actualizacion de variante | 200 + outbox `VariantUpdated` | FR-001 |
| CAT-IT-007 | `POST /variants/{id}/mark-sellable` | vendibilidad condicionada | 200 + `status=SELLABLE` + outbox `VariantSellabilityChanged` | FR-001 |
| CAT-IT-008 | `POST /variants/{id}/discontinue` | descontinuacion de variante | 200 + `status=DISCONTINUED` + outbox `VariantDiscontinued` | FR-001 |
| CAT-IT-009 | `PUT /variants/{id}/prices/current` | precio vigente activo | 200 + precio `ACTIVE` + outbox `PriceUpdated` | FR-001 |
| CAT-IT-010 | `POST /variants/{id}/prices/schedule` | precio programado | 201 + precio `SCHEDULED` + outbox `PriceScheduled` | FR-001 |
| CAT-IT-011 | `GET /variants/{id}/prices` | timeline consistente de precios | response usa estados `ACTIVE/SCHEDULED/EXPIRED` | dominio+contratos Catalog |
| CAT-IT-012 | `POST /prices/bulk-upsert` | carga masiva con idempotencia | resultado consistente + sin duplicar side effects | NFR-009 |
| CAT-IT-013 | `GET /catalog/search` | consulta catalogo filtrada por tenant | resultados solo del tenant | NFR-005 |
| CAT-IT-014 | `POST /variants/resolve` valido | endpoint trusted para checkout | 200 con variante resoluble y precio vigente | FR-004 |
| CAT-IT-015 | `POST /variants/resolve` no vendible | validacion para checkout | rechazo `variante_no_vendible` | FR-004 |
| CAT-IT-016 | seguridad tenant/ownership en mutaciones | actor tenant A sobre recurso tenant B | rechazo 403/409 + sin cambios DB | NFR-005, I-ACC-02, D-CROSS-01 |
| CAT-IT-017 | idempotencia mutacion admin | misma clave + mismo payload | un solo efecto persistente | NFR-009 |
| CAT-IT-018 | conflicto idempotente | misma clave + payload distinto | rechazo de conflicto | NFR-009 |
| CAT-IT-019 | consumo `StockUpdated` | recalculo `availabilityHint` | cache/index actualizados + `processed_event` | politicas Catalog |
| CAT-IT-020 | consumo `SkuReconciled` | reconciliacion de visibilidad SKU | proyeccion ajustada + `processed_event` | politicas Catalog |
| CAT-IT-021 | dedupe de evento Inventory duplicado | mismo `eventId` dos veces | segundo consumo `noop idempotente` | NFR-009 |
| CAT-IT-022 | retry y DLQ en evento no recuperable | payload incompatible/no legitimo | clasificacion no-retryable + DLQ + evidencia | NFR-006 |
| CAT-IT-023 | propagacion `traceId/correlationId` | mutacion + publicacion evento | ids presentes en response, auditoria y outbox | NFR-006 |
| CAT-IT-024 | validacion taxonomia referencial activa | marca/categoria inactivas | rechazo en alta/edicion de producto | FR-001 |
| CAT-IT-025 | consistencia de eventos publicados | topicos y payload minimos correctos | contratos producer conformes en corrida (`Product*`,`Variant*`,`Price*`) | NFR-009 |

## Criterio de exito integracion
- Escenarios `CAT-IT-001..025` disenados para verificar ausencia de breaking en contratos `v1`.
- Dedupe/outbox/idempotencia disenados para verificarse con evidencia persistida durante la corrida.
- En corrida de certificacion, no deben ocurrir mutaciones cross-tenant.
