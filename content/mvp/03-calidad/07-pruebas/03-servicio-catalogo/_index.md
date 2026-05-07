---
title: "Servicio Catalogo"
linkTitle: "3. Catalogo"
weight: 3
url: "/mvp/calidad/pruebas/servicio-catalogo/"
---

## Objetivo
Asegurar la coherencia comercial de producto, variante (SKU) y precio, manteniendo contratos estables para checkout, inventario, notificacion y reporteria.

## Alcance de calidad del servicio
- Flujos HTTP de administracion y consulta de catalogo (`products`, `variants`, `prices`, `search`, `resolve`).
- Flujos async: publicacion de eventos de Catalog por outbox y consumo idempotente de eventos de Inventory (`StockUpdated`, `SkuReconciled`).
- Reglas de seguridad: aislamiento tenant/rol, integridad comercial de SKU/precio, auditoria y trazabilidad tecnica.

## Fuentes de verdad usadas
- Producto: `FR-001`, `FR-004`, `NFR-005`, `NFR-006`, `NFR-009`.
- Dominio: `I-CAT-01`, `D-CAT-01`, `I-ACC-02`, `D-CROSS-01`.
- Arquitectura Catalog: contratos API/eventos, seguridad, datos y runtime.

## Datos de entrada comunes
- `tenant` principal: `org-co-001`.
- `tenant` alterno: `org-ec-001`.
- actores base:
  - `tenant_user` (consultas).
  - `arka_admin` (mutaciones comerciales).
  - `trusted_service(order-service)` para `variants/resolve`.
- trazabilidad tecnica obligatoria en mutaciones: `traceId`, `correlationId`.
- idempotencia en mutaciones administrativas via `Idempotency-Key`.

## Criterio de exito global
- Carpetas `unitarias`, `integracion` y `e2e` cubren escenarios `P1` y `P2` de Catalog.
- Cada escenario referencia al menos un `FR/NFR` y una regla/invariante de dominio.
- Errores canonicos de Catalog cubiertos en pruebas:
  - `sku_no_unico`
  - `required_attributes_missing`
  - `brand_o_categoria_invalida`
  - `producto_no_activo`
  - `variante_no_vendible`
  - `precio_invalido`
  - `periodo_precio_solapado`
  - `acceso_cruzado_detectado`
- Evidencia tecnica por flujo: `catalog_audit`, `outbox_event`, `processed_event`, logs estructurados con `traceId/correlationId`.

## Cobertura minima obligatoria (Catalog)
| Bloque | Cobertura minima |
|---|---|
| Producto | alta, actualizacion, activacion, retiro |
| Variante | alta, actualizacion, `mark-sellable`, descontinuacion |
| Precio | vigente, programado y expirado (`ACTIVE`, `SCHEDULED`, `EXPIRED`) sin vocabulario alterno |
| Taxonomia referencial | validacion de marca/categoria activas (sin CRUD independiente en MVP) |
| Seguridad y aislamiento | tenant/ownership, RBAC por accion, trusted-service |
| Integracion EDA | eventos `Product*`, `Variant*`, `Price*` + consumo Inventory con dedupe |
