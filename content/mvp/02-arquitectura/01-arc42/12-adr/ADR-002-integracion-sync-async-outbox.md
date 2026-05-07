---
title: "ADR-002 Integracion Sync-Async con Outbox"
linkTitle: "ADR-002"
weight: 2
url: "/mvp/arquitectura/arc42/adr/adr-002-integracion-sync-async-outbox/"
---

## Estado
Accepted

## Contexto
El checkout requiere validaciones criticas inmediatas, pero notificaciones y reportes deben ser desacoplados para resiliencia.

## Decision
Usar integracion mixta: sync para validaciones criticas (`order` con `inventory`/`directory`/`catalog`) y async por eventos para propagacion, con outbox en productores e idempotencia en consumidores.

## Consecuencias
- Positivas: protege invariantes y reduce acoplamiento temporal.
- Negativas: aumenta complejidad de consistencia eventual y operacion de broker.

## FR/NFR impactados
- FR-004, FR-006, FR-007, FR-008
- NFR-004, NFR-006, NFR-007

## Verificacion
- Flujos runtime en `arc42/06` y contratos de eventos por servicio.
