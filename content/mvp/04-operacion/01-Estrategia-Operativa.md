---
title: "Estrategia Operativa"
linkTitle: "1. Estrategia Operativa"
weight: 1
url: "/mvp/operacion/estrategia-operativa/"
---

## Proposito
Definir como se opera ArkaB2B en `MVP` con un baseline estable,
trazable y ejecutable, sin reinterpretar decisiones de producto, dominio,
arquitectura ni calidad.

## Alcance operativo del MVP
Incluye:
- operacion de servicios core y de soporte en ambientes reales;
- despliegues y promocion entre ambientes;
- monitoreo de salud, trazabilidad y resiliencia;
- gestion de incidentes, respuesta y recuperacion;
- operacion del broker, DLQ y reprocesos;
- gestion segura de configuracion y secretos.

No incluye:
- hardening enterprise completo;
- operacion multi-region activa/activa;
- compliance exhaustivo por pais;
- automatizacion avanzada de incidentes y capacity planning maduro.

## Principios operativos
- Una sola verdad: operar sobre baseline congelado de `MVP`.
- Seguridad primero: aislamiento tenant, permisos minimos y auditoria.
- Recuperacion sobre perfeccion: contener rapido y restaurar core.
- Trazabilidad obligatoria: `traceId`, `correlationId`, `tenantId`, `actorId`.
- Idempotencia operacional: reintentos y reprocesos sin duplicar efectos.
- Regionalizacion estricta: sin politica vigente por `countryCode` se bloquea
  con `configuracion_pais_no_disponible` y auditoria.

## Componentes criticos a operar
- Edge y seguridad: `api-gateway-service`, `identity-access-service`.
- Core comercial: `directory-service`, `catalog-service`,
  `inventory-service`, `order-service`.
- Soporte y proyeccion: `notification-service`, `reporting-service`.
- Plataforma operacional: broker compatible con `Kafka`, `Redis`,
  PostgreSQL por servicio, stack de observabilidad, gestor de secretos.

## Supuestos congelados del baseline
- Plataforma objetivo: `AWS`; `Railway` solo fallback tactico/excepcional.
- `LocalStack` solo local/dev.
- `JWKS` es contrato oficial para validacion JWT en componentes autorizados.
- Estados `READY_TO_DISPATCH`, `DISPATCHED`, `DELIVERED` fuera del baseline.
- `MFA` administrativo diferido a hardening futuro no bloqueante.

## Criterio operativo de exito en MVP
Se considera operable cuando:
- ambientes y promocion estan definidos y aplicados;
- alertas y runbooks cubren flujos criticos;
- incidentes se gestionan con severidad, escalamiento y evidencia;
- calidad minima de release se refleja en gates de promocion;
- riesgos operacionales y evoluciones estan explicitamente clasificados.

## Relacion con otros pilares
- Entrada de arquitectura: [Trazabilidad de Arquitectura](/mvp/arquitectura/trazabilidad/), [Conceptos Transversales](/mvp/arquitectura/arc42/conceptos-transversales/).
- Entrada de calidad: [Puertas de Calidad](/mvp/calidad/puertas-calidad/), [Evidencias](/mvp/calidad/evidencias/).
- Salida operativa: runbooks, criterios de promocion, trazabilidad operativa
  y gobierno de evolucion operacional.
