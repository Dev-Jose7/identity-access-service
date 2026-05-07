---
title: "Severidades y Escalamiento"
linkTitle: "2. Severidades y Escalamiento"
weight: 2
url: "/mvp/operacion/incidentes/severidades-escalamiento/"
---

## Niveles de severidad
| Nivel | Definicion | Tiempo objetivo de respuesta | Escalamiento |
|---|---|---|---|
| Sev 1 | caida o degradacion critica de flujo core en prod | <= 10 min | inmediato a owners + plataforma + liderazgo tecnico |
| Sev 2 | degradacion relevante con workaround parcial | <= 20 min | owner de servicio y plataforma |
| Sev 3 | impacto acotado, sin interrupcion total | <= 60 min | owner de servicio |
| Sev 4 | incidencia menor o consulta operativa | <= 1 dia habil | backlog operativo |

## Criterios de escalamiento
- Aumentar severidad si impacto se expande a checkout/pedido.
- Aumentar severidad si hay riesgo de seguridad o datos.
- Mantener severidad hasta comprobar recuperacion estable.

## Reglas de guardia
- Sev 1 y Sev 2 requieren guardia activa hasta cierre tecnico.
- Toda transferencia de guardia deja handoff con estado y proximos pasos.
