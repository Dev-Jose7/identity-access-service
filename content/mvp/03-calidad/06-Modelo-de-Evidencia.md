---
title: "Modelo de Evidencia"
linkTitle: "7. Modelo de Evidencia"
weight: 7
url: "/mvp/calidad/modelo-evidencia/"
---

## Proposito
Definir un modelo unico y auditable para registrar evidencia de pruebas y habilitar trazabilidad entre caso, corrida, gate y estado final.

## 1) Ubicacion de evidencia
Estructura recomendada para almacenar evidencia por ciclo/release:
`content/mvp/03-calidad/06-Evidencias/artefactos/<release>/<gate>/<servicio>/`

Donde:
- `<release>`: identificador de release (ej. `mvp-r1`).
- `<gate>`: `gate-1-pr`, `gate-2-integracion`, `gate-3-e2e`, `gate-4-no-funcional`.
- `<servicio>`: `identity-access-service`, `directory-service`, `catalog-service`, `inventory-service`, `order-service`, `notification-service`, `reporting-service`.

Referencias del paquete de certificacion minima:
- [Evidencias y Certificacion Minima](/mvp/calidad/evidencias/)
- [Registro de Ejecuciones](/mvp/calidad/evidencias/registro-ejecuciones/)
- [Acta de Certificacion Minima](/mvp/calidad/evidencias/acta-certificacion-minima/)

## 2) Convencion de nombres
Nombre canonico de evidencia:
`<fechaUTC>_<release>_<gate>_<servicio>_<suite>_<resultado>.md`

Ejemplo:
`2026-03-28T19-40Z_mvp-r1_gate-2-integracion_order-service_integracion_ok.md`

Adjuntos opcionales en la misma carpeta:
- `*.json` para resultados de test runner,
- `*.log` para logs estructurados,
- `*.csv` para metricas no funcionales,
- capturas/tablas SQL en `*.md` o `*.txt`.

## 3) Metadatos minimos por evidencia
Toda evidencia debe incluir:
- `release`;
- `gate`;
- `servicio`;
- `entorno` (`dev/qa/staging/prod`);
- `suite` y escenarios incluidos;
- `timestampInicio` y `timestampFin`;
- `resultado` (`ok`/`fail`);
- `commitSha` o version evaluada;
- `responsable`;
- `referencias` a `FR/NFR/RN-I-D`;
- `observaciones` y hallazgos (si aplica).

## 4) Criterios de estado

### 4.1 Para marcar `Ejecutado`
Un escenario puede pasar a `Ejecutado` si existe:
- corrida registrada con fecha, entorno y resultado,
- referencia al artefacto de corrida,
- identificacion de escenario(s) ejecutados.

### 4.2 Para marcar `Validado con evidencia`
Un escenario puede pasar a `Validado con evidencia` solo si:
- ya esta en estado `Ejecutado`,
- la evidencia cumple metadatos minimos,
- el gate aplicable queda aprobado,
- existe referencia cruzada desde [Mapeo de Pruebas](/mvp/calidad/mapeo-pruebas/).

## 5) Relacion entre caso, corrida y evidencia
- Caso de prueba: define comportamiento esperado y trazabilidad.
- Corrida: ejecucion real de uno o mas casos.
- Evidencia: artefacto verificable que respalda el resultado de la corrida.

Regla de auditoria:
- toda afirmacion de certificacion debe enlazar al menos una evidencia concreta.
- sin evidencia enlazada, el estado maximo permitido es `Implementado`.

## 6) Integracion con puertas de calidad
- Gate 1: evidencia de lint, unitarias y contract checks.
- Gate 2: evidencia de integracion, DB, seguridad, tenant y trazabilidad.
- Gate 3: evidencia E2E por flujo critico.
- Gate 4: evidencia no funcional (performance, disponibilidad, resiliencia, regionalizacion, seguridad operacional).

Referencia:
- [Puertas de Calidad](/mvp/calidad/puertas-calidad/)

## Historial
- 2026-03-28: se define modelo formal de evidencia para separar diseno, ejecucion y certificacion en `03-calidad`.
