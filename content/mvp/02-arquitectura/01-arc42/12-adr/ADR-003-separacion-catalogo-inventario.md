---
title: "ADR-003 Separacion Catalogo e Inventario"
linkTitle: "ADR-003"
weight: 3
url: "/mvp/arquitectura/arc42/adr/adr-003-separacion-catalogo-inventario/"
---

## Estado
Accepted

## Contexto
En enfoques legacy se fusionaba producto/precio con stock, aumentando ambiguedad semantica y riesgo de sobreventa.

## Decision
Separar `catalog-service` (vendible/precio) de `inventory-service` (stock/reservas/disponibilidad reservable) con contratos explicitos.

## Consecuencias
- Positivas: reduce fuga semantica y clarifica ownership de datos.
- Negativas: requiere mas integraciones y governance de contratos.

## FR/NFR impactados
- FR-001, FR-002, FR-004
- NFR-004

## Verificacion
- Context map, contratos y modelos de datos separados por servicio.
