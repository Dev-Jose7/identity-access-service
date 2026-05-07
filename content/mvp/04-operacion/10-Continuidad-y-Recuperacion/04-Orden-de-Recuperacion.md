---
title: "Orden de Recuperacion"
linkTitle: "4. Orden de Recuperacion"
weight: 4
url: "/mvp/operacion/continuidad-recuperacion/orden-recuperacion/"
---

## Prioridad de restauracion
1. identity-access-service y api-gateway-service
2. directory-service
3. catalog-service e inventory-service
4. order-service
5. notification-service
6. reporting-service

## Justificacion
La autenticacion y el flujo comercial core (directory+catalog+inventory+order)
son prerequisito para operacion minima del MVP.

## Orden por criticidad y dependencia
| Etapa | Servicios | Dependencia previa | Objetivo minimo |
|---|---|---|---|
| 1 | gateway + IAM | red/plataforma base | restablecer acceso autenticado |
| 2 | directory | etapa 1 | resolver contexto organizacional/regional |
| 3 | catalog + inventory | etapas 1-2 | restablecer vendibilidad y reserva de stock |
| 4 | order | etapas 1-3 | restaurar checkout y creacion de pedido |
| 5 | notification | etapa 4 | reanudar side effects no bloqueantes |
| 6 | reporting | etapas 3-4 | recuperar proyecciones y frescura |

## Escenario especial: broker/consumidores
- Si broker no esta disponible, restaurar primero publicacion de eventos core.
- Luego consumidores de `order`/`inventory` y finalmente consumidores derivados.
- Validar lag y DLQ antes de declarar etapa cerrada.

## Escenario especial: configuracion regional
- Si falla politica regional vigente, recuperar `directory` y propagacion de
  configuracion antes de reabrir checkout por `countryCode`.
- Mantener bloqueo auditable hasta confirmacion de vigencia.

## Verificacion por etapa
Cada etapa requiere:
- healthcheck verde;
- smoke funcional del servicio/flujo de la etapa;
- confirmacion de trazabilidad en logs/metricas/trazas;
- registro de evidencia en incidente activo.
