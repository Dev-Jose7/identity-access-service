---
title: "ADR-004 Versionado de Contratos"
linkTitle: "ADR-004"
weight: 4
url: "/mvp/arquitectura/arc42/adr/adr-004-versionado-de-contratos/"
---

## Estado
Accepted

## Contexto
El frontend externo y consumidores de eventos requieren estabilidad de integracion y control de cambios breaking.

## Decision
Versionar APIs por path major (`/api/v1`) y eventos por `eventVersion` + topic versionado. Cambios breaking obligan nueva major y ventana de convivencia.

## Consecuencias
- Positivas: menor riesgo de ruptura no controlada.
- Negativas: costo de coexistencia temporal de versiones.

## FR/NFR impactados
- FR-006, FR-007
- NFR-009

## Verificacion
- Reglas en contracts por servicio y escenarios de calidad NFR.
