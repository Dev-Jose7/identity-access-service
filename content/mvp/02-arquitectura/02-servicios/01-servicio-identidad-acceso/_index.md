---
title: "Servicio de Identidad y Acceso"
linkTitle: "1. Identidad y Acceso"
weight: 1
url: "/mvp/arquitectura/servicios/servicio-identidad-acceso/"
---

## Marco del servicio
_Responde: que bounded context aterriza este servicio y como debe leerse su dossier tecnico._
Dossier tecnico del servicio que materializa [Contexto Identidad y Acceso](/mvp/dominio/contextos-delimitados/identidad-acceso/).

## Rol del servicio
_Responde: que verdad protege este servicio y con que alcance participa en el MVP._
- autenticacion, sesion, rol y permisos por tenant.
- Preserva fronteras de dominio sin mezclar ownership con otros servicios.

## Cobertura de requisitos
_Responde: que FR/RNF justifican la existencia y prioridad de este servicio._
- FR-009 | NFR-005, NFR-006.

## Estructura del dossier
_Responde: que vistas locales componen este servicio y para que se usa cada una._
- [Arquitectura Interna](/mvp/arquitectura/servicios/servicio-identidad-acceso/arquitectura-interna/)
- [Contratos](/mvp/arquitectura/servicios/servicio-identidad-acceso/contratos/)
- [Datos](/mvp/arquitectura/servicios/servicio-identidad-acceso/datos/)
- [Rendimiento](/mvp/arquitectura/servicios/servicio-identidad-acceso/rendimiento/)
- [Seguridad](/mvp/arquitectura/servicios/servicio-identidad-acceso/seguridad/)
