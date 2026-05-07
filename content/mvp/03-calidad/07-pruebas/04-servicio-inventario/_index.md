---
title: "Servicio Inventario"
linkTitle: "4. Inventario"
weight: 4
url: "/mvp/calidad/pruebas/servicio-inventario/"
---

## Objetivo
Asegurar disponibilidad real, reservas con TTL, anti-sobreventa e idempotencia fuerte en mutaciones HTTP, manteniendo coherencia con checkout y consumidores EDA.

## Alcance de calidad del servicio
- Flujos HTTP de stock, reservas, consultas y validacion interna de checkout.
- Flujos async: publicacion de eventos Inventory por outbox y consumo idempotente de eventos de Catalog.
- Reglas de seguridad: aislamiento tenant/rol, ownership de reserva/stock, auditoria operativa y trazabilidad tecnica.

## Fuentes de verdad usadas
- Producto: `FR-002`, `FR-003`, `FR-004`, `NFR-004`, `NFR-005`, `NFR-006`, `NFR-009`.
- Dominio: `RN-INV-01`, `RN-RES-01`, `RN-RES-02`, `I-INV-01`, `I-INV-02`, `D-INV-01`, `D-INV-02`, `I-ACC-02`, `D-CROSS-01`.
- Arquitectura Inventory: contratos API/eventos, seguridad, datos y runtime.

## Datos de entrada comunes
- `tenant` principal: `org-co-001`.
- `tenant` alterno: `org-ec-001`.
- actores base:
  - `tenant_user`.
  - `arka_operator`.
  - `order_service` (trusted service).
  - `system_scheduler`.
- trazabilidad tecnica obligatoria en mutaciones: `traceId`, `correlationId`.
- idempotencia en mutaciones HTTP via `Idempotency-Key` + `idempotency_records`.

## Criterio de exito global
- Carpetas `unitarias`, `integracion` y `e2e` cubren escenarios `P1` y `P2` de Inventory.
- Cada escenario referencia al menos un `FR/NFR` y una regla/invariante de dominio.
- Errores canonicos de Inventory cubiertos en pruebas:
  - `stock_insuficiente`
  - `stock_negativo_invalido`
  - `reserva_expirada`
  - `reserva_no_encontrada`
  - `conflicto_reserva`
  - `acceso_cruzado_detectado`
  - `conflicto_idempotencia`
- Evidencia tecnica por flujo: `inventory_audit`, `idempotency_records`, `outbox_events`, `processed_events`, logs con `traceId/correlationId`.

## Cobertura minima obligatoria (Inventory)
| Bloque | Cobertura minima |
|---|---|
| Stock | inicializacion, ajustes, incrementos/decrementos, bulk adjustments |
| Reservas | crear, extender, confirmar, liberar, expirar por scheduler |
| Checkout sync | `POST /api/v1/internal/inventory/checkout/validate-reservations` |
| Idempotencia | dedupe write-side en mutaciones HTTP con `idempotency_records` |
| Integracion EDA | `Stock*`, `StockReservation*`, `SkuReconciled`, `LowStockDetected` + consumo `catalog.*` |
| Seguridad y aislamiento | tenant/ownership, permisos por accion, trigger context en listeners/scheduler |
