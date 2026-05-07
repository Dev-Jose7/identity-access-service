---
title: "Resumen"
linkTitle: "1. Resumen"
weight: 1
url: "/mvp/dominio/contextos-delimitados/reporteria/resumen/"
---

## Marco del contexto
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Resumen ejecutivo del bounded context `reporting`.

## Mision del contexto
_Responde: que verdad local gobierna este bounded context y para que existe dentro del dominio._
- Construir vistas derivadas de ventas y abastecimiento para decision operativa/comercial.
- Consolidar hechos de BC core sin mutar estado transaccional.

## Naturaleza del contexto
_Responde: por que reporting se modela como contexto de proyeccion y no como core transaccional._
- `reporting` es un contexto de proyeccion/analytics, no un contexto transaccional de negocio.
- Los comandos de `reporting` son internos de pipeline de proyeccion; no son comandos de negocio expuestos hacia core.
- `reporting` no tiene autoridad sobre verdad operacional ni comercial de `order`, `inventory`, `catalog`, `directory` o `notification`.

## Procesos y casos de uso cubiertos
_Responde: que procesos del MVP cubre este contexto y con que casos de uso contribuye._
| Proceso | Casos de uso | Resultado |
|---|---|---|
| P5 Abastecimiento semanal | refresco de vista de stock/faltantes | backlog semanal de reposicion |
| P8 Reporte de ventas semanal | refresco de vista de ventas/pago y generacion de reporte | reporte consolidado para negocio |
| Telemetria operacional | consolidacion de eventos de notificacion y checkout | indicadores accionables |

## Responsabilidades
_Responde: que responsabilidades locales asume este contexto._
- Consumir eventos de `order`, `inventory`, `catalog`, `directory`, `notification`.
- Mantener proyecciones de ventas y abastecimiento.
- Generar reporte semanal y emitir evento de disponibilidad.
- Aplicar corte semanal, moneda de presentacion y retencion de datos segun parametros operativos por pais.
- Validar politica regional activa por `countryCode` antes de consultas semanales, generacion y rebuild; bloquear con `configuracion_pais_no_disponible` si no existe.
- Recalcular vistas ante inconsistencia detectada.

## Limites (que NO hace)
_Responde: que queda explicitamente fuera del contexto para proteger sus fronteras._
- No crea/modifica pedidos, pagos, stock, catalogo o directorio.
- No autoriza operaciones transaccionales.

## Dependencias externas
_Responde: de que otros contextos o contratos depende este contexto._
- productores de eventos core (`order`, `inventory`, `catalog`, `directory`, `notification`).
- `notification` como consumidor de `WeeklyReportGenerated`.

## FR/NFR relacionados
_Responde: que requisitos del producto aterrizan principalmente en este contexto._
- FR-003, FR-007, FR-010, FR-011.
- NFR-002, NFR-007, NFR-011.

## Riesgos del contexto
_Responde: que riesgos locales existen y como se mitigan._
- Riesgo: usar vista derivada como verdad transaccional.
  - Mitigacion: regla fuerte `RN-REP-01`.
- Riesgo: operar consultas/reportes sin politica regional vigente.
  - Mitigacion: resolucion obligatoria en `directory` y bloqueo con `configuracion_pais_no_disponible`.
- Riesgo: backlog de consumo genera reporte desactualizado.
  - Mitigacion: monitoreo de lag + recalculo por lote.
