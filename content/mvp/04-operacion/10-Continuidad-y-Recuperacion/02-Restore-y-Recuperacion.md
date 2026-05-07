---
title: "Restore y Recuperacion"
linkTitle: "2. Restore y Recuperacion"
weight: 2
url: "/mvp/operacion/continuidad-recuperacion/restore-recuperacion/"
---

## Alcance
Restauracion de datos y servicios minimos para reanudar operacion segura.

## Reglas
- Restaurar desde backup validado y trazable.
- Ejecutar verificacion de integridad post-restore.
- Reconciliar entidades core cuando exista brecha temporal.
- Priorizar restauracion de datos core antes de proyecciones derivadas.
- Toda restauracion en `prod` debe abrir incidente y registro de evidencia.

## Secuencia general
1. Confirmar punto de restauracion.
2. Restaurar dependencia/servicio objetivo.
3. Correr verificaciones de salud y consistencia.
4. Habilitar trafico progresivo.
5. Registrar evidencia de recuperacion.

## Restauracion por tipo de falla
| Falla | Accion minima | Verificacion minima |
|---|---|---|
| Base de datos de servicio core | restore al punto validado + reconexion de servicio | health + smoke funcional del caso critico |
| Broker degradado | restaurar conectividad de productores/consumidores + controlar offsets | lag decreciente + DLQ estable |
| Configuracion critica perdida | reponer version aprobada de configuracion/secreto | cese de errores asociados y auditoria de cambio |

## Reconciliacion posterior
- `order` vs `inventory`: validar consistencia de reservas/pedidos.
- `reporting`: ejecutar backfill controlado por rango afectado.
- Eventos reprocesados deben mantener idempotencia y trazabilidad.

## Criterio de salida de recuperacion
- flujo core estable en ventana operativa definida;
- sin alertas criticas activas relacionadas al incidente;
- evidencia completa adjunta al registro del incidente.
