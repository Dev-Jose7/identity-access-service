---
title: "Servicio Notificaciones"
linkTitle: "6. Notificaciones"
weight: 6
url: "/mvp/calidad/pruebas/servicio-notificaciones/"
---

## Objetivo
Asegurar entrega no bloqueante de notificaciones, control de fallos por canal y trazabilidad completa de solicitudes/intentos/resultados.

## Alcance de calidad del servicio
- Flujos HTTP internos: request, dispatch, retry, discard, callbacks de proveedor, consultas y reproceso DLQ.
- Flujos async: consumo de eventos de Order/Inventory/Directory/Reporting/IAM y emision de eventos de Notification.
- Reglas de seguridad: scopes m2m, aislamiento tenant, validacion de callback provider, dedupe e integridad de payload.

## Fuentes de verdad usadas
- Producto: `FR-006`, `FR-008`, `FR-010`, `NFR-005`, `NFR-006`, `NFR-007`, `NFR-009`.
- Dominio: `RN-NOTI-01`, `I-NOTI-01`, `I-ACC-02`, `D-CROSS-01`.
- Arquitectura Notification: contratos API/eventos, seguridad, datos y runtime.

## Datos de entrada comunes
- `tenant` principal: `org-co-001`.
- `tenant` alterno: `org-ec-001`.
- actores/identidades base:
  - caller interno con scope `notification.write`.
  - caller interno con scope `notification.dispatch`.
  - caller interno con scope `notification.read`.
  - caller interno con scope `notification.ops`.
  - callback provider firmado/tokenizado.
- trazabilidad tecnica obligatoria: `traceId`, `correlationId`.
- idempotencia en mutaciones por `Idempotency-Key`.

## Criterio de exito global
- Carpetas `unitarias`, `integracion` y `e2e` cubren escenarios `P1` y `P2` de Notification.
- Cada escenario referencia al menos un `FR/NFR` y una regla/invariante de dominio.
- Errores canonicos de Notification cubiertos en pruebas:
  - `destinatario_invalido`
  - `canal_no_disponible`
  - `provider_timeout`
  - `maximo_reintentos_excedido`
  - `estado_invalido`
  - `conflicto_idempotencia`
  - `acceso_cruzado_detectado`
- Evidencia tecnica por flujo: `notification_audit`, `outbox_event`, `processed_event`, `notification_attempt` y logs con `traceId/correlationId`.

## Cobertura minima obligatoria (Notification)
| Bloque | Cobertura minima |
|---|---|
| Solicitud y envio | request -> dispatch -> sent/failed |
| Retry/discard | politicas de reintento y cierre terminal |
| Callback provider | reconciliacion segura y dedupe natural |
| Consumo upstream | `Order*`, `CartAbandonedDetected`, `Stock*`, `Directory*`, `WeeklyReportGenerated`, `UserBlocked` |
| Desacople no bloqueante | falla de notificacion no revierte core transaccional |
| Resiliencia | retries, DLQ, reprocess-dlq |
| Seguridad | scopes m2m, tenant isolation, validacion callback |
