---
title: "Unitarias - servicio-catalogo"
linkTitle: "1. Unitarias"
weight: 1
url: "/mvp/calidad/pruebas/servicio-catalogo/unitarias/"
---

## Objetivo
Validar reglas de dominio de Catalog para lifecycle comercial, unicidad SKU y consistencia de precio.

## Cobertura objetivo
- `I-CAT-01`, `D-CAT-01`.
- `I-ACC-02`, `D-CROSS-01` en validacion contextual.
- Estados canonicos de precio: `ACTIVE`, `SCHEDULED`, `EXPIRED`.

## Fixtures base sugeridos
- `fixture_catalog_product_draft_valid_refs.yaml`
- `fixture_catalog_product_active_with_sellable_variant.yaml`
- `fixture_catalog_variant_draft_with_required_attributes.yaml`
- `fixture_catalog_variant_discontinued.yaml`
- `fixture_catalog_price_active_window.yaml`
- `fixture_catalog_price_scheduled_window.yaml`

## Matriz detallada de casos unitarios
| ID | Escenario | Given | When | Then | Trazabilidad |
|---|---|---|---|---|---|
| CAT-UT-001 | crear producto con taxonomia referencial valida | `brand/category` activas | registrar producto | producto en `DRAFT` | FR-001 |
| CAT-UT-002 | rechazar producto con marca o categoria invalida | referencia inexistente/inactiva | registrar producto | error `brand_o_categoria_invalida` | FR-001 |
| CAT-UT-003 | SKU unico por tenant para variante vendible | SKU ya usado en variante vendible | crear nueva variante | error `sku_no_unico` | FR-001, I-CAT-01, D-CAT-01 |
| CAT-UT-004 | variante requiere atributos obligatorios | payload sin atributos requeridos | crear/marcar vendible | error `required_attributes_missing` | FR-001 |
| CAT-UT-005 | no marcar vendible si producto no esta activo | producto en `DRAFT/RETIRED` | `mark-sellable` | error `producto_no_activo` | FR-001 |
| CAT-UT-006 | descontinuar variante anula vendibilidad | variante `SELLABLE` | descontinuar | estado `DISCONTINUED`, `sellable=false` | FR-001 |
| CAT-UT-007 | precio invalido por monto no positivo | `amount <= 0` | upsert precio | error `precio_invalido` | FR-001 |
| CAT-UT-008 | precio invalido por ventana solapada | periodo nuevo cruza periodo vigente/programado | programar precio | error `periodo_precio_solapado` | FR-001 |
| CAT-UT-009 | lifecycle de precio canaInico | precio programado con fecha futura | avanzar tiempo laIgico | `SCHEDULED -> ACTIVE -> EXPIRED` | dominio Catalog (modelo) |
| CAT-UT-010 | no usar estados tecnicos como lifecycle de precio | entrada con `PENDING/CANCELLED` en agregado precio | validar comando | rechazo semantico | consistencia dominio/contratos |
| CAT-UT-011 | policy de tenant obliga tenantId en mutaciones | comando sin tenant | validar policy | rechazo por aislamiento | NFR-005, D-CROSS-01 |
| CAT-UT-012 | acceso cruzado en recurso comercial | actor tenant A sobre producto tenant B | evaluar ownership | error `acceso_cruzado_detectado` | NFR-005, I-ACC-02 |
| CAT-UT-013 | resolver variante para checkout requiere vendible + precio vigente | variante no vendible o sin precio activo | resolver variante | error `variante_no_vendible` o rechazo equivalente | FR-004 |
| CAT-UT-014 | retiro de producto bloquea nuevas operaciones comerciales | producto `RETIRED` | crear variante/precio | rechazo semantico | FR-001 |
| CAT-UT-015 | dedupe semantico en comando idempotente | misma clave + mismo payload | reintentar mutacion | resultado equivalente sin doble side effect | NFR-009 |

## Criterio de exito unitario
- Escenarios `CAT-UT-001..015` en estado `Disenado` o superior, segun corrida y evidencia.
- Se valida expresamente el vocabulario unico de precio (`ACTIVE/SCHEDULED/EXPIRED`).
- Rechazos semanticos sin mutaciones indebidas.
