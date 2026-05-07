---
title: "Glosario"
linkTitle: "5. Glosario"
weight: 5
url: "/mvp/producto/glosario/"
---

## Proposito del archivo
Definir lenguaje canonico de producto para eliminar ambiguedad en documentos, codigo y agentes IA.

## Terminos

| Termino | Definicion | Sinonimos prohibidos | Ejemplo |
|---|---|---|---|
| Organizacion | Cliente B2B con identidad comercial propia dentro del sistema. | cliente global, cuenta global | "El pedido pertenece a la organizacion ORG-001." |
| Usuario B2B | Persona autenticada asociada a una organizacion. | usuario global, usuario libre | "El usuario B2B confirma el pedido." |
| Rol de negocio | Perfil de autorizacion para ejecutar acciones de negocio. | admin total, permiso libre | "El rol de negocio operador puede registrar pago manual." |
| Producto | Agrupador comercial de variantes vendibles. | item maestro | "El producto SSD NVMe tiene varias variantes." |
| Variante (SKU) | Unidad vendible concreta con atributos definidos. | item suelto, referencia ambigua | "SKU SSD-1TB-NVME-980PRO" |
| Stock fisico | Unidades realmente existentes en inventario. | stock comercial | "El stock fisico del SKU es 120." |
| Disponibilidad | Unidades vendibles calculadas como stock fisico menos reservas activas. | stock disponible comercial | "Disponibilidad actual: 35 unidades." |
| Reserva | Apartado temporal de unidades para un carrito o checkout. | bloqueo indefinido | "La reserva vence en 20 minutos." |
| Carrito | Intencion de compra editable previa a confirmar pedido. | pedido borrador final | "El carrito contiene 5 lineas." |
| Carrito abandonado | Carrito sin conversion a pedido dentro de la ventana definida. | carrito perdido | "Se detecto carrito abandonado para recordatorio." |
| Pedido | Entidad de compra creada desde carrito que se materializa en `PENDING_APPROVAL` y evoluciona por estados contractuales del MVP (`PENDING_APPROVAL`, `CONFIRMED`, `CANCELLED`); `CREATED` es interno y los estados de fulfillment quedan reservados. | orden finalizada | "Pedido ARKA-CO-2026-000184 en estado PENDING_APPROVAL." |
| Estado de pedido | Etapa oficial del ciclo de vida de un pedido. | estado final | "Estado de pedido: CONFIRMED." |
| Estado de pago | Situacion agregada de pago del pedido. | estado financiero libre | "Estado de pago: PENDING." |
| Sobreventa | Confirmacion de pedido sin disponibilidad real suficiente. | falta normal de stock | "La meta es mantener sobreventa <= 1.0%." |
| Reporte de abastecimiento | Reporte de SKU con riesgo de faltante para reponer inventario. | reporte de compras suelto | "Se genero el reporte de abastecimiento semanal." |

## Acronimos

| Acronimo | Significado |
|---|---|
| API | Application Programming Interface |
| BC | Bounded Context |
| MVP | Minimum Viable Product |
| SKU | Stock Keeping Unit |
| SLA | Service Level Agreement |
| KPI | Key Performance Indicator |
| NFR | Non-Functional Requirement |
| FR | Functional Requirement |

## Reglas de lenguaje
- Un concepto tiene un solo nombre canonico.
- Si se crea un termino nuevo, tambien actualizar el Ubiquitous Language del pilar de dominio cuando ese pilar sea materializado.
- Si en este ciclo de vida se materializan artefactos de contratos, deben usar nombres canonicos.
- Eventos de contrato usan naming en ingles `UpperCamelCase` y envelope comun con `eventType` + `eventVersion`.
- Las tablas de FR/NFR y trazabilidad deben usar los mismos terminos definidos aqui.

## Changelog breve
- 2026-03-03: glosario inicial completo para el pilar Producto (MVP backend ArkaB2B).
- 2026-03-04: se corrige referencia absoluta al Ubiquitous Language del pilar Dominio.
- 2026-03-07: se alinea definicion de `Pedido` con los estados contractuales de FR-004/FR-005/FR-006.
