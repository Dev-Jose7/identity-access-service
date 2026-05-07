---
title: "Politicas"
linkTitle: "6. Politicas"
weight: 6
url: "/mvp/dominio/contextos-delimitados/notificaciones/politicas/"
---

## Marco de politicas
_Responde: que define este artefacto dentro del bounded context y como debe leerse su alcance._
Politicas de reaccion de `notification`.

## Politicas (if event -> then command)
_Responde: que reacciones automatizadas ejecuta el contexto frente a eventos relevantes._
| Trigger (evento) | Condicion | Accion (comando) | Observaciones |
|---|---|---|---|
| `OrderConfirmed` | canal habilitado | `solicitar_notificacion` | confirmacion comercial de pedido |
| `OrderStatusChanged` | estado visible al cliente | `solicitar_notificacion` | avance operativo |
| `OrderPaymentRegistered` | politica de cobro activa | `solicitar_notificacion` | FR-010 aviso de pago manual |
| `CartAbandonedDetected` | ventana comercial valida | `solicitar_notificacion` | FR-008 |
| `StockReservationExpired` | reserva vinculada a carrito activo | `solicitar_notificacion` | aviso preventivo |
| `LowStockDetected` | umbral critico superado | `solicitar_notificacion` | alerta operativa |
| `WeeklyReportGenerated` | destinatarios operativos definidos | `solicitar_notificacion` | distribucion de reporte |
| `NotificationFailed` | intentos < maximo | `reintentar_notificacion` | retry con backoff |

## Retries / compensacion
_Responde: como maneja este contexto reintentos y compensaciones sin romper su modelo._
- Retry: hasta 3 intentos con backoff exponencial.
- Compensacion: marcar `DISCARDED` tras maximo de intentos y escalar operacion.

## Timeouts
_Responde: que limites temporales gobiernan las interacciones de este contexto._
- envio por intento: 2 s objetivo.
- resolucion de destinatario: 300 ms.
