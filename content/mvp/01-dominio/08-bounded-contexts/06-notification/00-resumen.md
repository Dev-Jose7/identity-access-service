---
title: "Resumen"
linkTitle: "1. Resumen"
weight: 1
url: "/mvp/dominio/contextos-delimitados/notificaciones/resumen/"
---

## Marco del contexto
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Resumen ejecutivo del bounded context `notification`.

## Mision del contexto
_Responde: que verdad local gobierna este bounded context y para que existe dentro del dominio._
- Gestionar solicitudes e intentos de notificacion derivados de hechos de negocio.
- Mantener notificacion como capacidad no bloqueante del core transaccional.

## Naturaleza del contexto
_Responde: por que notification se modela como soporte operacional y no como orquestador del negocio._
- `notification` es un contexto supporting con verdad operacional de entrega (solicitud, intento, resultado), no verdad comercial.
- `notification` no decide workflow principal de compra/pago; solo reacciona a hechos del core.
- Su modelo existe para resiliencia, trazabilidad de entrega y desacoplamiento.

## Procesos y casos de uso cubiertos
_Responde: que procesos del MVP cubre este contexto y con que casos de uso contribuye._
| Proceso | Casos de uso | Resultado |
|---|---|---|
| P4 Ciclo de pedido | notificar confirmacion/cambio de estado/pago | cliente informado sin afectar transaccion |
| P6 Recuperacion de carrito | notificar carrito abandonado | habilita recuperacion comercial |
| P5/P8 operacion y reportes | notificar bajo stock/reporte semanal | soporte operativo interno |

## Responsabilidades
_Responde: que responsabilidades locales asume este contexto._
- Registrar solicitud de notificacion por evento de dominio.
- Ejecutar envio por canal configurado.
- Reintentar de forma controlada ante falla recuperable.
- Registrar resultado de envio y emitir eventos de trazabilidad.

## Limites (que NO hace)
_Responde: que queda explicitamente fuera del contexto para proteger sus fronteras._
- No muta estado de pedido, pago, stock ni catalogo.
- No define contenido comercial de negocio fuera de plantillas.
- No es fuente transaccional de ventas o inventario.

## Dependencias externas
_Responde: de que otros contextos o contratos depende este contexto._
- `order`, `inventory`, `reporting`, `identity-access` y `directory` como productores de hechos.
- proveedor de canal (correo/plataforma) para entrega.
- `reporting` como consumidor de eventos de notificacion.

## FR/NFR relacionados
_Responde: que requisitos del producto aterrizan principalmente en este contexto._
- FR-006, FR-008, FR-010.
- NFR-007, NFR-006.

## Riesgos del contexto
_Responde: que riesgos locales existen y como se mitigan._
- Riesgo: interpretar fallo de envio como fallo de negocio core.
  - Mitigacion: regla `RN-NOTI-01` y eventos no bloqueantes.
- Riesgo: duplicidad de envios por reintento.
  - Mitigacion: idempotencia por `notificationId` + `idempotencyKey`.
