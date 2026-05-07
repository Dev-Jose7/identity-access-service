---
title: "Plan de Certificacion Minima"
linkTitle: "6.1 Plan"
weight: 1
url: "/mvp/calidad/evidencias/baseline-certification-plan/"
---

## Objetivo
Definir el nucleo minimo y defendible de pruebas para certificar el baseline `MVP` sin afirmar cobertura total de produccion.

## Precondiciones
- Producto, dominio y arquitectura congelados para `MVP`.
- Mapeo `FR/NFR` completo y taxonomia de estados vigente.
- Suites minimas implementadas o listas para ejecucion en entorno objetivo.

## Paquete minimo de certificacion

### A) Unitarias criticas por servicio
Se exige evidencia de al menos una suite critica por cada servicio:
- `identity-access-service`: sesion valida/revocada, bloqueo de usuario, aislamiento de tenant.
- `directory-service`: ownership organizacional, direccion valida, politica regional vigente.
- `catalog-service`: vendibilidad, precio vigente, transiciones de variante.
- `inventory-service`: stock no negativo, reserva/expiracion/confirmacion idempotente.
- `order-service`: transiciones de pedido, checkout con reservas, pago manual idempotente.
- `notification-service`: solicitud no bloqueante, retry, descarte controlado.
- `reporting-service`: proyeccion read-only, dedupe, corte semanal por `countryCode`.

### B) Integraciones criticas del flujo core
- `order <-> inventory`: reserva/confirmacion/liberacion sin sobreventa.
- `order <-> catalog`: variante vendible y precio vigente para checkout.
- `order <-> directory`: direccion valida y politica regional vigente.
- `order -> notification`: side effect no bloqueante por `OrderConfirmed`.
- `order/inventory/catalog/directory/notification -> reporting`: consumo derivado idempotente.

### C) E2E minimos del MVP
1. Compra feliz B2B (`checkout -> PENDING_APPROVAL -> CONFIRMED`).
2. Bloqueo por falta de politica regional (`configuracion_pais_no_disponible`).
3. Rechazo por falta de disponibilidad/reserva (`stock_insuficiente` o `reserva_expirada`).

### D) No funcional minimo defendible
- aislamiento tenant (NFR-005);
- idempotencia basica en mutaciones core y consumo async (NFR-004/NFR-009);
- contratos API/eventos minimos sin breaking no versionado (NFR-009);
- build de imagen Docker por servicio y arranque con configuracion externa;
- stack reproducible de integracion con `docker compose` para smoke multi-servicio;
- error canonico estable en flujos invalidos core;
- medicion basal de rendimiento de flujo critico (NFR-001);
- verificacion basica de seguridad operacional (`traceId`, `correlationId`, auditoria) (NFR-006/NFR-007).

## Fuera del alcance de certificacion minima
- certificacion completa de todos los escenarios no funcionales avanzados;
- hardening operativo profundo multi-region/compliance exhaustivo;
- pruebas enterprise de externals fuera del baseline (PSP/ERP/IdP externo).

## Criterio de suficiencia
El paquete minimo es suficiente para declarar el baseline listo para cierre integral cuando:
- no existen hallazgos `P0/P1` abiertos en las suites minimas;
- existe evidencia verificable por suite minima;
- las desviaciones `P2/P3` estan registradas y time-boxed.
