---
title: "Comandos"
linkTitle: "4. Comandos"
weight: 4
url: "/mvp/dominio/contextos-delimitados/reporteria/comandos/"
---

## Marco de comandos
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Catalogo de comandos internos de proyeccion de `reporting`.

## Lista de comandos
_Responde: que comandos admite el contexto y con que efecto semantico._
Nota semantica:
- estos comandos son mecanismos internos de materializacion/recalculo analitico.
- no son comandos de negocio expuestos para mutar estado de BCs core.

### registrar_hecho_analitico
_Esta subseccion detalla registrar_hecho_analitico dentro del contexto del documento._
- Input esperado:
  - `sourceEventId`, `tenantId`, `eventType`, `eventVersion`, `occurredAt`, `payload`.
- Precondiciones:
  - evento fuente valido y no duplicado.
- Postcondiciones:
  - hecho normalizado disponible para agregacion.

### refrescar_vista_ventas
_Esta subseccion detalla refrescar_vista_ventas dentro del contexto del documento._
- Input esperado:
  - `tenantId`, `period`, `sourceEventId`, `idempotencyKey`.
- Precondiciones:
  - evento de ventas/pago valido (`OrderConfirmed`, `OrderPaymentRegistered`, `OrderPaymentStatusUpdated`).
- Postcondiciones:
  - vista de ventas actualizada.

### refrescar_vista_abastecimiento
_Esta subseccion detalla refrescar_vista_abastecimiento dentro del contexto del documento._
- Input esperado:
  - `tenantId`, `period`, `sourceEventId`, `idempotencyKey`.
- Precondiciones:
  - evento de stock/reserva valido.
- Postcondiciones:
  - vista de abastecimiento actualizada.

### generar_reporte_semanal
_Esta subseccion detalla generar_reporte_semanal dentro del contexto del documento._
- Input esperado:
  - `tenantId`, `weekId`, `reportType`, `operationRef`, `idempotencyKey`.
- Precondiciones:
  - ventana semanal cerrada.
- Postcondiciones:
  - reporte generado en ubicacion de salida.

### recalcular_vista_completa
_Esta subseccion detalla recalcular_vista_completa dentro del contexto del documento._
- Input esperado:
  - `tenantId`, `period`, `reasonCode`, `operationRef`.
- Postcondiciones:
  - vista reconstruida desde hechos fuente.
