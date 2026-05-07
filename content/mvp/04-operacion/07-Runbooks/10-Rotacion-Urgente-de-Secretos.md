---
title: "Rotacion Urgente de Secretos"
linkTitle: "10. Rotacion Urgente de Secretos"
weight: 10
url: "/mvp/operacion/runbooks/rotacion-urgente-secretos/"
---

## Proposito
Rotar secretos comprometidos minimizando interrupcion del servicio.

## Senal de entrada
- Indicador de exposicion de secreto.
- Alerta de uso no autorizado.

## Impacto esperado
Riesgo de acceso indebido, fuga o manipulacion.

## Diagnostico inicial
- Identificar secretos afectados y alcance.
- Confirmar servicios/dependencias impactadas.

## Contencion
- Revocar secreto comprometido.
- Restringir accesos temporales asociados.

## Recuperacion
- Emitir nuevo secreto por entorno.
- Distribuir por canal oficial de secretos.
- Reiniciar componentes que cargan secreto en arranque.
- Verificar que la inyeccion de secreto quede efectiva en runtime
  (contenedor/proceso) del servicio afectado.

## Verificacion posterior
- Autenticacion a dependencia funciona con secreto nuevo.
- No hay nuevos intentos con secreto revocado.

## Escalamiento
Sev 1 de seguridad en prod.

## Evidencia a registrar
- identificador de secreto rotado
- fecha/hora de revocacion
- validacion de servicio restaurado

## Artefactos relacionados
- 11-Seguridad-Operacional/02-Secretos-y-Rotacion
- 08-Incidentes/03-Comunicacion-y-Postmortem
