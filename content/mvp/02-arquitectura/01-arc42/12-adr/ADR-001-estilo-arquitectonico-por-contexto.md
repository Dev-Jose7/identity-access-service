---
title: "ADR-001 Estilo Arquitectonico por Contexto"
linkTitle: "ADR-001"
weight: 1
url: "/mvp/arquitectura/arc42/adr/adr-001-estilo-arquitectonico-por-contexto/"
---

## Estado
Accepted

## Contexto
El dominio ArkaB2B esta definido por bounded contexts con reglas de negocio fuertes y necesidades de evolucion independiente.

## Decision
Adoptar microservicios por BC con arquitectura hexagonal interna (`adapter-in`, `application`, `domain`, `adapter-out`).

## Consecuencias
- Positivas: separacion de responsabilidades, menor acoplamiento semantico, escalado selectivo.
- Negativas: mayor complejidad de despliegue e integracion.

## FR/NFR impactados
- FR-001..FR-010
- NFR-008, NFR-009

## Verificacion
- Consistencia de packs por servicio y contratos explicitos.
