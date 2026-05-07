---
title: "Acta de Certificacion Minima"
linkTitle: "6.5 Acta"
weight: 5
url: "/mvp/calidad/evidencias/acta-certificacion-minima/"
---

## Proposito
Formalizar el criterio para declarar el baseline `MVP` certificado minimamente, sin confundirlo con certificacion completa de produccion.

## Alcance de la certificacion minima
La certificacion minima cubre:
- consistencia del pilar de calidad (diseno, mapeo, gates y evidencia);
- ejecucion del paquete minimo critico definido en [Plan de Certificacion Minima](/mvp/calidad/evidencias/baseline-certification-plan/);
- evidencia verificable y trazable para los casos incluidos en la matriz minima.

No cubre:
- certificacion operacional completa multi-region;
- hardening avanzado de rendimiento/compliance;
- totalidad de escenarios no funcionales extendidos.

## Precondiciones para emitir acta
- [Matriz de Resultados Esperados](/mvp/calidad/evidencias/matriz-resultados-esperados/) actualizada para release candidato.
- [Registro de Ejecuciones](/mvp/calidad/evidencias/registro-ejecuciones/) con corridas reales del paquete minimo.
- [Desviaciones y Aceptaciones](/mvp/calidad/evidencias/desviaciones-aceptaciones/) actualizado.
- Gates aplicables sin bloqueos `P0/P1`.

## Criterio formal de emision
| Estado de acta | Condicion |
|---|---|
| `Lista para certificar` | paquete minimo definido, sin corridas adjuntas aun |
| `Certificada minimamente` | paquete minimo ejecutado con evidencia y sin `P0/P1` abiertos |
| `Certificacion condicionada` | existe evidencia parcial con desviaciones `P2/P3` aceptadas y time-boxed |
| `No certificada` | falta evidencia minima o existen bloqueos `P0/P1` |

## Estado actual del baseline `MVP`
`Lista para certificar`.

Motivo:
- el paquete minimo, matriz y reglas de evidencia estan definidos;
- la certificacion final depende de adjuntar corridas reales y evidencias del release candidato.

## Campos de firma (para uso en corrida real)
- Release evaluado:
- Fecha de evaluacion:
- Entorno(s):
- Responsable de calidad:
- Responsable tecnico:
- Decision final del acta:
- Referencias de evidencia:
