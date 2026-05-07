---
title: "Resumen"
linkTitle: "1. Resumen"
weight: 1
url: "/mvp/dominio/contextos-delimitados/inventario/resumen/"
---

## Marco del contexto
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Resumen ejecutivo del bounded context `inventory`.

## Mision del contexto
_Responde: que verdad local gobierna este bounded context y para que existe dentro del dominio._
- Gestionar stock fisico, disponibilidad reservable, reservas TTL y movimientos auditables.
- Evitar sobreventa preservando invariantes de stock/reserva.

## Procesos y casos de uso cubiertos
_Responde: que procesos del MVP cubre este contexto y con que casos de uso contribuye._
| Proceso | Casos de uso | Resultado |
|---|---|---|
| P3 Actualizacion de stock | UC-INV-01..UC-INV-04, UC-INV-11 | stock consistente y auditado |
| P2 Compra recurrente | UC-INV-05..UC-INV-08, UC-INV-16 | reservas validas para checkout |
| Operacion continua | UC-INV-09, UC-INV-10, UC-INV-12..UC-INV-15 | expiracion/reconciliacion/consultas operativas |

## Responsabilidades
_Responde: que responsabilidades locales asume este contexto._
- Inicializar, ajustar, incrementar y decrementar stock.
- Crear/extender/confirmar/liberar/expirar reservas.
- Validar reservas para checkout.
- Reconciliar estado de SKU contra cambios de catalogo.
- Emitir eventos de stock y reserva.

## Limites (que NO hace)
_Responde: que queda explicitamente fuera del contexto para proteger sus fronteras._
- No confirma pedidos ni muta estado de pago.
- No define vendible de producto/SKU.
- No gestiona identidad de usuario.

## Dependencias externas
_Responde: de que otros contextos o contratos depende este contexto._
- `catalog` para reconciliacion de SKU.
- `order` como consumidor y emisor de solicitudes de reserva/checkout.
- `reporting` y `notification` como consumidores de eventos de stock.

## FR/NFR relacionados
_Responde: que requisitos del producto aterrizan principalmente en este contexto._
- FR-002, FR-003, FR-004, FR-005.
- NFR-004, NFR-001.

## Riesgos del contexto
_Responde: que riesgos locales existen y como se mitigan._
- Riesgo: carreras entre expiracion de reserva y confirmacion checkout.
  - Mitigacion: validacion final + confirmacion idempotente por correlacion.
- Riesgo: alta contencion en SKU caliente.
  - Mitigacion: locking por `tenant+warehouse+sku` y loteo de expiraciones.
