---
title: "Accesos Operativos"
linkTitle: "1. Accesos Operativos"
weight: 1
url: "/mvp/operacion/seguridad-operacional/accesos-operativos/"
---

## Politica base
Acceso operacional por minimo privilegio y cuentas nominales.

## Reglas
- separar accesos de lectura y administracion.
- registrar todo acceso privilegiado.
- prohibido compartir credenciales entre personas/equipos.
- limitar duracion de accesos elevados a ventana de trabajo.
- revocar accesos temporales al cierre de incidente/cambio.

## Roles operativos minimos
- operador Arka
- administrador/soporte Arka
- owner tecnico de servicio

## Acceso por entorno
| Entorno | Acceso humano | Acceso maquina |
|---|---|---|
| `dev` | equipos tecnicos autorizados | cuentas de servicio de desarrollo |
| `qa` | equipos tecnicos + QA autorizado | cuentas de servicio dedicadas |
| `prod` | guardia/operaciones y owners autorizados | cuentas de servicio de produccion aisladas |

## Separacion humano vs maquina
- Personas operan con cuentas nominales auditables.
- Servicios operan con cuentas de servicio y secretos dedicados.
- Ninguna cuenta de servicio debe usarse para acceso humano.
- Contenedores/imagenes no portan credenciales embebidas; consumen identidades
  tecnicas inyectadas por entorno.

## Auditoria operativa minima
- alta/baja/cambio de permisos;
- accesos a recursos sensibles;
- ejecucion de acciones administrativas criticas.

## Nota de baseline
MFA administrativo queda diferido como hardening futuro no bloqueante.
No es requisito de cierre del baseline MVP.
