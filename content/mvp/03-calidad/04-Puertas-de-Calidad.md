---
title: "Puertas de Calidad"
linkTitle: "4. Puertas de Calidad"
weight: 4
url: "/mvp/calidad/puertas-calidad/"
---

## Proposito
Definir las compuertas de calidad de `MVP` para controlar promocion de cambios desde PR hasta evaluacion no funcional, sin sobreactuar certificacion cuando no exista evidencia.

## Alcance
Aplica a los 7 servicios del sistema:
- `identity-access-service`
- `directory-service`
- `catalog-service`
- `inventory-service`
- `order-service`
- `notification-service`
- `reporting-service`

## Regla transversal de certificacion
- Cumplir un gate requiere corrida ejecutada + evidencia verificable.
- Si no hay evidencia, el estado maximo permitido es `Implementado`.
- Solo se usa `Validado con evidencia` cuando hay aprobacion explicita del gate.
- El cierre del baseline integral se habilita con certificacion minima aprobada (no con certificacion total de produccion).

Referencia: [Modelo de Evidencia](/mvp/calidad/modelo-evidencia/)

## Gate 0: Certificacion minima del baseline

### Criterios obligatorios
- Paquete minimo definido en [Plan de Certificacion Minima](/mvp/calidad/evidencias/baseline-certification-plan/) ejecutado para el release candidato.
- Resultados trazados en [Matriz de Resultados Esperados](/mvp/calidad/evidencias/matriz-resultados-esperados/).
- Corridas reales registradas en [Registro de Ejecuciones](/mvp/calidad/evidencias/registro-ejecuciones/).
- Desviaciones y aceptaciones documentadas en [Desviaciones y Aceptaciones](/mvp/calidad/evidencias/desviaciones-aceptaciones/).
- Emision de [Acta de Certificacion Minima](/mvp/calidad/evidencias/acta-certificacion-minima/) con estado final.

### Regla de salida
- `Aprobado`: paquete minimo ejecutado con evidencia suficiente y sin defectos `P0/P1` abiertos.
- `Condicionado`: existe evidencia parcial o desviaciones `P2/P3` aceptadas time-boxed.
- `No aprobado`: falta evidencia minima o existen bloqueos `P0/P1`.

## Gate 1: PR (pre-merge)

### Criterios obligatorios
- Lint/estaticos conformes.
- Suite `unitarias` conforme en el alcance impactado.
- Contract checks minimos conformes:
  - APIs cambiadas sin breaking no versionado.
  - Eventos cambiados compatibles con consumidores `v1`.
- Build del artefacto de empaquetado del/los servicio(s) impactado(s) conforme.
- Sin secretos embebidos en artefacto ni configuracion critica hardcodeada.
- Sin fallos de severidad `P0/P1` abiertos introducidos por el cambio.

### Evidencia minima
- reporte de lint;
- reporte de unitarias;
- reporte de contract checks;
- evidencia de build de artefacto y arranque tecnico basico;
- trazabilidad a `FR/NFR/RN-I-D` impactados.

## Gate 2: Integracion

### Criterios obligatorios
- APIs y eventos del cambio evaluados en entorno de integracion.
- Persistencia/DB evaluada (writes/reads, constraints, idempotencia).
- Seguridad evaluada:
  - autenticacion en borde;
  - autorizacion contextual en servicio;
  - errores canonicos esperados.
- Multi-tenant evaluado:
  - no hay acceso cruzado;
  - `tenantId` presente en mutaciones y evidencia.
- Stack reproducible de integracion levantado con el mecanismo multi-contenedor
  definido en arquitectura cuando
  aplique al alcance del cambio.
- Arranque de contenedores con configuracion externa por entorno.

### Evidencia minima
- resultados de pruebas de integracion por servicio;
- evidencia de DB/outbox/processed_event cuando aplique;
- evidencia de stack de integracion (servicios/dependencias) y smoke;
- evidencia de trazabilidad (`traceId`, `correlationId`).

## Gate 3: E2E

### Criterios obligatorios
Flujos criticos end-to-end exigidos segun el alcance documentado por servicio en el baseline vigente:
- IAM: login/refresh/logout, bloqueo/revocacion.
- Directory: validacion de direccion y politica regional.
- Catalog: resolucion comercial de variante/precio.
- Inventory: reserva/confirmacion/expiracion sin sobreventa.
- Order: checkout -> `PENDING_APPROVAL` -> `CONFIRMED`, pago manual.
- Notification: solicitud y resultado no bloqueante.
- Reporting: consolidacion y reporte semanal read-only.

### Regla de salida
- 100% de escenarios E2E criticos del alcance del release con corrida registrada.
- Sin incidentes `P0/P1` abiertos en los flujos criticos.

## Gate 4: No funcional

### Matriz operativa no funcional
| NFR | Flujo medido | Entorno objetivo | Metrica/umbral | Ventana | Evidencia esperada |
|---|---|---|---|---|---|
| NFR-001 | lecturas de catalogo + mutaciones checkout/pedido | `qa`/`staging` | p95 <= 800 ms (lecturas), p95 <= 1500 ms (mutaciones) | corrida controlada por release | reporte de performance con percentiles por endpoint |
| NFR-002 | generacion de reporte semanal | `qa`/`staging` | <= 15 min por ejecucion semanal | por corrida semanal | tiempos de inicio/fin por `tenant+week+type` |
| NFR-003 | disponibilidad backend core | `prod` (o simulacion operativa previa) | >= 99.5% mensual | 06:00-22:00 hora local | reporte SLI/SLO por servicio y consolidado |
| NFR-004 | checkout + reserva/confirmacion de stock | `qa`/`staging` | sobreventa <= 1.0% semanal | semanal | metrica de sobreventa y detalle de incidentes |
| NFR-005 | mutaciones tenantizadas | `qa`/`staging` | 0 incidentes criticos cross-tenant/mes | por release + mensual | consultas de auditoria/seguridad por `tenantId` |
| NFR-006 | mutaciones criticas | `qa`/`staging` | >= 99% con `actorId/tenantId/fechaOperacion` | por release | evidencia de auditoria y logs estructurados |
| NFR-007 | mutaciones y consumo async | `qa`/`staging` | 100% respuestas mutantes con `traceId`; alertas de error-rate/latencia activas | por release | reporte de observabilidad + alertas + trazas |
| NFR-008 | picos de demanda en checkout/pedido | `qa`/`staging` | 3x baseline con degradacion <= 30% de p95 (NFR-001) | corrida de estres por release | comparativo baseline vs carga pico |
| NFR-009 | APIs/eventos `v1` | `qa`/`staging` | sin breaking no versionado | por PR/release | resultados de contract tests producer/consumer |
| NFR-010 | retencion, anonimizado y borrado | `qa`/`staging` + controles operativos | solicitudes <= 30 dias; politica aplicada por tipo de dato | por release + mensual | evidencia de jobs/reglas + auditoria |
| NFR-011 | operaciones con `countryCode` | `qa`/`staging` | 100% operaciones con politica regional vigente; sin fallback global implicito | por release | resultados regionales + bloqueos `configuracion_pais_no_disponible` auditados |

### Baseline operativo transversal obligatorio
- artefacto de empaquetado versionado por microservicio como baseline tecnico.
- stack reproducible para local/dev/qa de integracion.
- Broker/event bus operativo con outbox en productores y dedupe en consumidores.
- Logs estructurados con `traceId/correlationId`, metricas y trazas distribuidas activas.
- Secretos/config fuera de repositorio con gestor administrado.
- DLQ y reproceso auditable para consumidores async.

### Plataforma y entornos
- `AWS` como plataforma objetivo de despliegue real (`dev/qa/staging/prod`).
- `Railway` solo como fallback tactico/excepcional manteniendo topologia logica y controles.
- `LocalStack` restringido a emulacion local/dev (no staging/prod).

## Criterio explicito de bloqueo/desbloqueo por severidad

### Clasificacion
- `P0` Critico: vulneracion de seguridad/tenant, corrupcion de datos core, caida de flujo comercial critico.
- `P1` Alto: ruptura funcional de FR core o NFR clave sin workaround aceptable.
- `P2` Medio: degradacion relevante con workaround temporal.
- `P3` Bajo: defecto menor sin impacto operativo significativo.

### Reglas de bloqueo
- `P0`: bloquea inmediatamente cualquier gate.
- `P1`: bloquea PR, Integracion, E2E y salida no funcional hasta correccion/verificacion.
- `P2`: no bloquea PR si hay mitigacion documentada; bloquea salida de release si afecta flujo critico.
- `P3`: no bloquea; se registra en backlog priorizado.

### Reglas de desbloqueo
Un hallazgo se desbloquea solo cuando:
- correccion implementada;
- prueba automatizada asociada agregada/actualizada;
- re-ejecucion del gate afectado conforme;
- evidencia anexada en trazabilidad de calidad.

## Politica de excepcion (time-boxed)
- Solo para `P2/P3`, con aprobacion explicita.
- Debe incluir:
  - riesgo aceptado,
  - mitigacion,
  - fecha limite de cierre.
- No se aceptan excepciones para `P0/P1` en salida de release.

## Historial
- 2026-03-28: se establecen compuertas formales de calidad para PR, integracion, E2E y no funcional con criterio de bloqueo/desbloqueo por severidad.
- 2026-03-28: se agrega matriz operativa no funcional y regla estricta de certificacion con evidencia.
