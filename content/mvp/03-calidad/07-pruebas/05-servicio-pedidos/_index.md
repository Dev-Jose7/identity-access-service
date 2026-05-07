---
title: "Servicio Pedidos"
linkTitle: "5. Pedidos"
weight: 5
url: "/mvp/calidad/pruebas/servicio-pedidos/"
---

## Objetivo
Asegurar el flujo comercial completo de carrito, checkout, pedido y pago manual, con transiciones validas, aislamiento tenant y trazabilidad operacional.

## Alcance de calidad del servicio
- Flujos HTTP de carrito, validacion/confirmacion de checkout, ciclo de vida del pedido, pagos y consultas.
- Flujos async: publicacion de eventos Order por outbox y consumo idempotente de eventos de Inventory, Catalog, Directory e IAM.
- Reglas de seguridad: tenant/ownership, RBAC por accion y controles de idempotencia en mutaciones.

## Fuentes de verdad usadas
- Producto: `FR-004`, `FR-005`, `FR-006`, `FR-008`, `FR-010`, `FR-011`, `NFR-004`, `NFR-005`, `NFR-006`, `NFR-007`, `NFR-009`, `NFR-011`.
- Dominio: `RN-ORD-01`, `RN-ORD-02`, `RN-ORD-03`, `RN-PAY-01`, `RN-PAY-02`, `I-ORD-01`, `I-ORD-02`, `I-PAY-01`, `I-ACC-02`, `I-LOC-01`, `D-ORD-01`, `D-ORD-02`, `D-PAY-01`, `D-CROSS-01`.
- Arquitectura Order: contratos API/eventos, seguridad, datos y runtime.

## Datos de entrada comunes
- `tenant` principal: `org-co-001`.
- `tenant` alterno: `org-ec-001`.
- actores base:
  - `tenant_user`.
  - `arka_operator`.
  - `arka_admin`.
  - `system_scheduler`.
- trazabilidad tecnica obligatoria en mutaciones: `traceId`, `correlationId`.
- idempotencia en mutaciones por `Idempotency-Key`.

## Criterio de exito global
- Carpetas `unitarias`, `integracion` y `e2e` cubren escenarios `P1` y `P2` de Order.
- Cada escenario referencia al menos un `FR/NFR` y una regla/invariante de dominio.
- Errores canonicos de Order cubiertos en pruebas:
  - `conflicto_checkout`
  - `transicion_estado_invalida`
  - `reserva_expirada`
  - `pago_duplicado`
  - `monto_pago_invalido`
  - `acceso_cruzado_detectado`
  - `configuracion_pais_no_disponible`
  - `conflicto_idempotencia`
- Evidencia tecnica por flujo: `order_audit`, `outbox_event`, `processed_event`, snapshots y logs con `traceId/correlationId`.

## Cobertura minima obligatoria (Order)
| Bloque | Cobertura minima |
|---|---|
| Carrito | agregar/actualizar/remover/limpiar con reservas |
| Checkout | validar y confirmar con Directory + Inventory + Catalog |
| Pedido | crear en `PENDING_APPROVAL`, confirmar a `CONFIRMED`, cancelar segun politica |
| Pago manual | registrar pago y recalcular `paymentStatus` sin duplicidad |
| Regionalizacion | bloqueo por falta de politica pais (`no fallback global`) |
| Integracion EDA | `Order*`, `Cart*` + consumo `inventory.*`, `catalog.*`, `directory.*`, `iam.*` |
| Baseline de estados | `PENDING_APPROVAL`, `CONFIRMED`, `CANCELLED` activos; `READY_TO_DISPATCH`, `DISPATCHED`, `DELIVERED` reservados |
