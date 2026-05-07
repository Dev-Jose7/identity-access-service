+++
title = "ArkaB2B"
type = "home"
url = "/"
+++

## Propósito

Este sitio es la fuente de verdad documental de ArkaB2B.
Su objetivo es organizar los pilares de ingeniería de software y sus artefactos por ciclo de vida, manteniendo trazabilidad clara entre producto, dominio, arquitectura, calidad y operación.

## Estado actual

| Ciclo de vida | Version documental baseline | Producto | Dominio | Arquitectura | Calidad | Operacion | Estado |
|---|---|---|---|---|---|---|---|
| MVP | 0.1.1 | Cerrado | Cerrado | Cerrado | Cerrado | Cerrado | Baseline listo para implementacion |

## Cómo navegar (ciclo-de-vida-first)

1. Usa el combo **Ciclo de vida** en la parte superior izquierda.
2. Selecciona el ciclo que quieres revisar (por ejemplo, `MVP`).
3. Al seleccionar el ciclo se habilita su árbol de navegación en la barra lateral.
4. Dentro del ciclo, navega por pilares (`00-producto`, `01-dominio`, `02-arquitectura`, `03-calidad`, `04-operacion`).
5. Si no hay ciclo de vida seleccionado, este home funciona como punto de entrada global.

## Flujo recomendado de lectura

1. Entra al ciclo de vida objetivo.
2. Empieza por `00-producto` para entender alcance y requerimientos.
3. Revisa `SRS`, catálogo RF/RNF y trazabilidad de producto.
4. Continúa en `01-dominio` para validar mapa de contexto, lenguaje ubicuo, reglas y contratos.
5. Revisa la trazabilidad de dominio para cobertura RF/RNF por componente.
6. Usa el glosario y la trazabilidad de dominio para validar semántica antes de pasar a arquitectura.
7. Revisa `02-arquitectura` para aterrizar servicios, contratos tecnicos, vistas globales y decisiones de despliegue del ciclo.
8. Usa `03-calidad` para implementar y verificar el sistema con mapeo FR/NFR, compuertas y evidencia.
9. Usa `04-operacion` para ejecutar despliegue, observabilidad, runbooks e incidentes en el baseline vigente.
