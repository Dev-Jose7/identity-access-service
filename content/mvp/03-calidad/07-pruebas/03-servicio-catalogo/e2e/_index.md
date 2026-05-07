---
title: "E2E - servicio-catalogo"
linkTitle: "3. E2E"
weight: 3
url: "/mvp/calidad/pruebas/servicio-catalogo/e2e/"
---

## Objetivo
Validar journeys comerciales de Catalog de punta a punta, incluyendo integracion con order/inventory/reporting/notificacion y controles de seguridad.

## Alcance E2E
- ciclo completo producto-variante-precio para disponibilidad comercial.
- resolucion de variante para checkout.
- propagacion de eventos a consumidores reales.
- seguridad tenant/rol y trazabilidad operacional.

## Escenarios E2E priorizados
| ID | Escenario | Flujo | Resultado esperado | Trazabilidad |
|---|---|---|---|---|
| CAT-E2E-001 | publicacion comercial completa | crear producto -> activar -> crear variante -> mark-sellable -> precio vigente | SKU vendible y resoluble para canales de consulta | FR-001 |
| CAT-E2E-002 | checkout resuelve variante valida | order invoca `variants/resolve` sobre SKU vendible | resolve exitoso con precio activo | FR-004 |
| CAT-E2E-003 | checkout rechaza variante no vendible | variante `DISCONTINUED` o sin precio activo | rechazo semantico estable para order | FR-004 |
| CAT-E2E-004 | cambio de precio vigente impacta consumidores | actualizar precio -> publicar `PriceUpdated` | order/reporting consumen cambio sin ruptura | FR-001, NFR-009 |
| CAT-E2E-005 | precio programado transiciona por tiempo | programar precio futuro | ciclo `SCHEDULED -> ACTIVE -> EXPIRED` coherente | dominio Catalog |
| CAT-E2E-006 | retiro de producto dispara efectos downstream | retirar producto -> `ProductRetired` | inventory/notification reaccionan segun contrato | FR-001 |
| CAT-E2E-007 | descontinuacion de variante impacta order/inventory | descontinuar variante -> `VariantDiscontinued` | consumidores aplican restriccion operacional | FR-001, FR-004 |
| CAT-E2E-008 | ingest de eventos inventory ajusta proyeccion | inventory publica `StockUpdated/SkuReconciled` | Catalog actualiza hint/index con dedupe | politicas Catalog |
| CAT-E2E-009 | aislamiento tenant en admin y consultas | actor tenant A sobre datos tenant B | rechazo `acceso_cruzado_detectado` | NFR-005 |
| CAT-E2E-010 | taxonomia referencial invalida bloquea alta | crear producto con brand/category inactiva | rechazo semantico y auditoria | FR-001 |
| CAT-E2E-011 | resiliencia outbox en falla de broker | mutacion exitosa con fallo de publicacion | decision persiste y outbox reintenta | NFR-006 |
| CAT-E2E-012 | trazabilidad tecnica completa | mutacion admin + consumo/emit de evento | cadena `request -> db -> audit -> outbox -> evento` con correlacion | NFR-006, NFR-009 |

## Criterio de exito E2E
- Escenarios `CAT-E2E-001..012` disenados para ejecucion reproducible; el estado final requiere corrida y evidencia.
- En corrida de certificacion, los flujos criticos (`001`, `002`, `004`, `007`, `009`) deben ejecutarse sin desviaciones semanticas.
- Vocabulario de precio y eventos consistente entre dominio/contratos/runtime.

## Evidencia minima por corrida
- Reporte por escenario con estado (`Implementado`/`Ejecutado`/`Validado con evidencia`).
- Extractos de `catalog_audit`, `outbox_event` y `processed_event` correlacionados.
- Confirmacion de contratos API/eventos `v1` sin ruptura para consumidores.
