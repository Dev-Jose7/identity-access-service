---
title: "ADR-005 Multitenancy y Auditoria"
linkTitle: "ADR-005"
weight: 5
url: "/mvp/arquitectura/arc42/adr/adr-005-multitenancy-y-auditoria/"
---

## Estado
Accepted

## Contexto
ArkaB2B requiere segregacion estricta por organizacion y trazabilidad de mutaciones de stock/pedido/pago.

## Decision
Aplicar validacion de tenant/rol en borde y dominio, y exigir metadata de auditoria (`actorId`, `tenantId`, `traceId`, `correlationId`) en mutaciones criticas.

## Consecuencias
- Positivas: reduce riesgo de acceso cruzado y mejora investigacion de incidentes.
- Negativas: incremento de validaciones y volumen de logs/auditoria.

## FR/NFR impactados
- FR-009, FR-010
- NFR-005, NFR-006, NFR-007

## Verificacion
- Security architecture por servicio y escenarios de calidad de seguridad/observabilidad.
