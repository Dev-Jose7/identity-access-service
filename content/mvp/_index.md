---
title: "MVP"
weight: 10
params:
  ciclo: "mvp"
  baselineVersion: "0.1.1"
  sidebarmenus:
    - type: page
      identifier: ciclo
      pageRef: /mvp/
      main: true
      disableTitle: true
cascade:
  - params:
      sidebarmenus:
        - type: page
          identifier: ciclo
          pageRef: /mvp/
          main: true
          disableTitle: true
url: "/mvp/"
---

## Objetivo del ciclo de vida

`MVP` define el baseline funcional y no funcional del sistema ArkaB2B para salir a operacion inicial con alcance controlado.

Este ciclo concentra el trabajo de definicion, validacion y trazabilidad de requerimientos que habilitan la primera entrega util del producto.

## Convencion y versionado

- Ciclo vigente: `mvp`.
- Version documental del baseline: `0.1.1`.

## Alcance actual

- Catalogo funcional (FR) y no funcional (NFR) del ciclo.
- Trazabilidad de producto entre SRS, FR/NFR y estado.
- Lenguaje comun mediante glosario.
- Baseline de dominio por bounded context, reglas, eventos y contratos.
- Baseline de arquitectura con vistas globales, servicios y cierre de trazabilidad tecnica.

## Estado rapido del ciclo

| Pilar | Estado | Comentario |
|---|---|---|
| 00-producto | Cerrado | Alcance, FR/NFR, glosario y trazabilidad funcional congelados para `mvp`. |
| 01-dominio | Cerrado | Semantica, reglas, eventos y contratos de integracion alineados al baseline vigente. |
| 02-arquitectura | Cerrado | Vistas globales y dossiers por servicio coherentes con decisiones tecnicas congeladas. |
| 03-calidad | Cerrado | Sistema de verificacion y certificacion minima definido de forma auditable. |
| 04-operacion | Cerrado | Baseline operativo listo para despliegue, observabilidad e incidentes en `v0.1.1`. |

## Artefactos disponibles en MVP

- [Producto](/mvp/producto/)
- [SRS Ligero](/mvp/producto/srs-ligero/)
- [Catalogo RF](/mvp/producto/catalogo-rf/)
- [Catalogo RNF](/mvp/producto/catalogo-rnf/)
- [Trazabilidad](/mvp/producto/trazabilidad/)
- [Glosario](/mvp/producto/glosario/)
- [Dominio](/mvp/dominio/)
- [Mapa de Contexto](/mvp/dominio/mapa-contexto/)
- [Lenguaje Ubicuo](/mvp/dominio/lenguaje-ubicuo/)
- [Eventos de Dominio](/mvp/dominio/eventos-dominio/)
- [Reglas e Invariantes](/mvp/dominio/reglas-invariantes/)
- [Contratos de Integracion](/mvp/dominio/contratos-integracion/)
- [Contextos Delimitados](/mvp/dominio/contextos-delimitados/)
- [Arquitectura](/mvp/arquitectura/)
- [arc42](/mvp/arquitectura/arc42/)
- [Servicios](/mvp/arquitectura/servicios/)
- [Trazabilidad de Arquitectura](/mvp/arquitectura/trazabilidad/)
- [Evolucion de Arquitectura](/mvp/arquitectura/evolucion/)
- [Calidad](/mvp/calidad/)
- [Estrategia de Pruebas](/mvp/calidad/estrategia-pruebas/)
- [Operacion](/mvp/operacion/)
- [Runbooks Operativos](/mvp/operacion/runbooks/)

## Flujo recomendado

1. Revisar `SRS Ligero` para entender outcomes del ciclo.
2. Validar FR/NFR del grupo `MVP`.
3. Confirmar prioridades y estado en trazabilidad.
4. Consumir dominio como baseline semantico ejecutable.
5. Revisar arquitectura para definir servicios, contratos, seguridad, runtime, datos y despliegue del ciclo.
6. Usar calidad y operacion como guia de verificacion y ejecucion sobre el baseline cerrado.

## Regla de evolucion

En este repositorio, cada ciclo de vida agrega nuevos requerimientos o deltas sobre IDs existentes.
No se reescribe informacion sin cambio: se referencia por ID y se documenta la diferencia.
