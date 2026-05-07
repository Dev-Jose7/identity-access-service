---
title: "Servicio de Pedidos"
linkTitle: "5. Pedidos"
weight: 5
url: "/mvp/arquitectura/servicios/servicio-pedidos/"
---

## Marco del servicio
_Responde: que bounded context aterriza este servicio y como debe leerse su dossier tecnico._
Dossier tecnico del servicio que materializa [Contexto Pedidos](/mvp/dominio/contextos-delimitados/pedidos/).

## Rol del servicio
_Responde: que verdad protege este servicio y con que alcance participa en el MVP._
- carrito, checkout, pedido, confirmacion comercial y pago manual.
- Preserva fronteras de dominio sin mezclar ownership con otros servicios.

## Cobertura de requisitos
_Responde: que FR/RNF justifican la existencia y prioridad de este servicio._
- FR-004, FR-005, FR-006, FR-007, FR-008, FR-010, FR-011 | NFR-004, NFR-005, NFR-006, NFR-011.

## Estructura del dossier
_Responde: que vistas locales componen este servicio y para que se usa cada una._
- [Arquitectura Interna](/mvp/arquitectura/servicios/servicio-pedidos/arquitectura-interna/)
- [Contratos](/mvp/arquitectura/servicios/servicio-pedidos/contratos/)
- [Datos](/mvp/arquitectura/servicios/servicio-pedidos/datos/)
- [Rendimiento](/mvp/arquitectura/servicios/servicio-pedidos/rendimiento/)
- [Seguridad](/mvp/arquitectura/servicios/servicio-pedidos/seguridad/)
