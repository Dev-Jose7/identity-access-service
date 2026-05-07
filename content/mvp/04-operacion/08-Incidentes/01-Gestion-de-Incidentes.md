---
title: "Gestion de Incidentes"
linkTitle: "1. Gestion de Incidentes"
weight: 1
url: "/mvp/operacion/incidentes/gestion-incidentes/"
---

## Objetivo
Establecer un proceso unico para detectar, contener, resolver y aprender de
incidentes operativos en MVP.

## Definicion de incidente
Evento no planeado que degrada disponibilidad, integridad operativa,
rendimiento o seguridad del baseline.

## Ciclo operativo
1. Deteccion por alerta, reporte o monitoreo.
2. Triage inicial y asignacion de severidad.
3. Contencion para limitar impacto.
4. Recuperacion del servicio.
5. Cierre tecnico y registro de evidencia.
6. Postmortem cuando corresponda.

## Roles minimos
- Incident Commander: coordina respuesta.
- Owner de servicio: ejecuta diagnostico/remediacion.
- Plataforma/SRE: soporte de infraestructura y observabilidad.
- Comunicacion: actualiza stakeholders internos.

## Reglas
- Todo incidente en prod abre registro operativo.
- Todo Sev 1 exige timeline y postmortem.
- No se promueven releases durante incidente Sev 1/Sev 2 abierto.
