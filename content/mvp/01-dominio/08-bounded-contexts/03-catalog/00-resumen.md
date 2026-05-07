---
title: "Resumen"
linkTitle: "1. Resumen"
weight: 1
url: "/mvp/dominio/contextos-delimitados/catalogo/resumen/"
---

## Marco del contexto
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Resumen ejecutivo del bounded context `catalog`.

## Mision del contexto
_Responde: que verdad local gobierna este bounded context y para que existe dentro del dominio._
- Gestionar productos, variantes SKU, taxonomia comercial referencial activa, estado vendible y precio vigente/programado.
- Entregar informacion comercial consistente para carrito y checkout.

## Procesos y casos de uso cubiertos
_Responde: que procesos del MVP cubre este contexto y con que casos de uso contribuye._
| Proceso | Casos de uso | Resultado |
|---|---|---|
| P2 Compra recurrente | UC-CAT-12..UC-CAT-16 | variante resoluble y precio vigente para pedido |
| Gestion de catalogo | UC-CAT-01..UC-CAT-11 | ciclo de vida producto/variante/precio auditable |
| Soporte de reporting | eventos de producto/variante/precio | hechos comerciales para analitica |

## Responsabilidades
_Responde: que responsabilidades locales asume este contexto._
- Alta/actualizacion/activacion/retiro de producto.
- Alta/actualizacion/publicacion/descontinuacion de variante.
- Upsert de precio vigente y programacion de precio futuro.
- Validacion de marca/categoria activas como taxonomia referencial local del catalogo.
- Busqueda y detalle de catalogo para frontend y servicios internos.

## Limites (que NO hace)
_Responde: que queda explicitamente fuera del contexto para proteger sus fronteras._
- No calcula disponibilidad reservable (eso es `inventory`).
- No confirma pedidos ni reservas.
- No autentica usuarios.
- No expone CRUD independiente de marca/categoria por API en `MVP`; esa taxonomia se administra por carga controlada dentro del propio catalogo.

## Dependencias externas
_Responde: de que otros contextos o contratos depende este contexto._
- `order` como consumidor de resolucion de variante/precio.
- `inventory` para reconciliar estado operativo de SKU.
- `reporting` para consumo de cambios comerciales.

## FR/NFR relacionados
_Responde: que requisitos del producto aterrizan principalmente en este contexto._
- FR-001, FR-004, FR-007.
- NFR-001, NFR-006.

## Riesgos del contexto
_Responde: que riesgos locales existen y como se mitigan._
- Riesgo: usar `vendible` como si fuera disponibilidad.
  - Mitigacion: ACL explicita `catalog=vendible`, `inventory=disponibilidad`.
- Riesgo: picos de cambios de precio generan drift en checkout.
  - Mitigacion: snapshot de precio en `order` + idempotencia de eventos.
