---
title: "Notas de Cobertura"
linkTitle: "5. Notas de Cobertura"
weight: 5
url: "/mvp/calidad/notas-cobertura/"
---

## Proposito
Definir el modelo de cobertura de `MVP` priorizando evidencia funcional y semantica (dominio/contratos/flujos), no solo porcentaje de lineas.

## 1) Modelo de cobertura por tipo

### 1.1 Cobertura de logica de dominio
Valida:
- reglas `RN-*`,
- invariantes `I-*`,
- invariantes de datos `D-*`,
- transiciones validas/invalidas,
- errores canonicos de negocio.

Evidencia esperada:
- pruebas unitarias e integracion de agregados/politicas/validadores.

### 1.2 Cobertura de contratos
Valida:
- APIs (request/response, codigos, errores, versionado),
- eventos (envelope, version, compatibilidad producer/consumer, idempotencia de consumo).

Evidencia esperada:
- contract tests de API/eventos por servicio.

### 1.3 Cobertura tecnica de empaquetado y ejecucion reproducible
Valida:
- build de artefacto de empaquetado por microservicio;
- arranque de runtime con configuracion externa;
- ausencia de secretos embebidos en el artefacto;
- stack de integracion reproducible con el mecanismo multi-contenedor definido.

Evidencia esperada:
- evidencia de build/arranque por servicio;
- evidencia de smoke de stack de integracion.

### 1.4 Cobertura de flujos criticos
Valida:
- secuencias end-to-end de negocio entre servicios.

Flujos minimos:
- authn/authz por tenant/rol,
- checkout y creacion de pedido,
- confirmacion comercial de pedido,
- reservas/stock sin sobreventa,
- pago manual,
- notificacion no bloqueante,
- consolidacion y reporte semanal,
- regionalizacion por `countryCode` sin fallback global.

Evidencia esperada:
- pruebas E2E + trazabilidad tecnica (`traceId`, `correlationId`).

## 2) Metricas de cobertura (complementarias)
- `Cobertura semantica`: porcentaje de `FR/NFR/RN-I-D` con al menos un caso disenado.
- `Cobertura implementada`: porcentaje de escenarios en estado `Implementado` o superior.
- `Cobertura certificada`: porcentaje de escenarios en estado `Validado con evidencia`.
- `Cobertura de contratos`: porcentaje de endpoints/eventos versionados con contract tests.
- `Cobertura de empaquetado`: porcentaje de servicios con build/arranque de artefacto validado.
- `Cobertura de stack reproducible`: porcentaje de flujos de integracion ejecutables sobre stack estandar de integracion.
- `Cobertura de flujos criticos`: porcentaje de journeys criticos con corrida registrada.
- `Cobertura de lineas` (auxiliar): indicador secundario, nunca criterio unico de cierre.

Regla:
- no se acepta declarar cobertura completa solo con porcentaje de lineas.

## 3) Minimos de cobertura por nivel
| Nivel | Minimo de diseno (documental) | Minimo de certificacion (operacional) |
|---|---|---|
| Unitarias | >= 85% de `RN/I/D` criticos con casos disenados | por release: porcentaje objetivo definido por equipo ejecutor |
| Integracion | >= 80% de contratos API/eventos activos con casos disenados | por release: cobertura de contratos impactados en gate |
| E2E | 100% de flujos criticos definidos en calidad | por release: 100% de flujos criticos impactados con corrida registrada |

Nota: los minimos de certificacion dependen de ejecuciones reales y evidencia, no se infieren por diseno.

## 4) Minimos por servicio
| Servicio | Unitarias (dominio) | Integracion (contratos/DB/seguridad) | E2E (flujos criticos) |
|---|---|---|---|
| `identity-access-service` | >= 85% reglas IAM criticas | >= 80% APIs/eventos IAM | login/refresh/logout + bloqueo/revocacion |
| `directory-service` | >= 85% reglas organizacion/direccion/pais | >= 80% APIs/eventos directory | validacion direccion + politica regional |
| `catalog-service` | >= 85% reglas de producto/variante/precio | >= 80% APIs/eventos catalog | resolucion comercial para checkout |
| `inventory-service` | >= 90% reglas de stock/reserva | >= 85% APIs/eventos inventory | reserva/confirmacion/expiracion sin sobreventa |
| `order-service` | >= 90% reglas de pedido/pago | >= 85% APIs/eventos order | checkout -> `PENDING_APPROVAL` -> `CONFIRMED` + pago manual |
| `notification-service` | >= 80% politicas de solicitud/intento | >= 80% APIs/eventos notification | envio no bloqueante + fallos controlados |
| `reporting-service` | >= 80% reglas de proyeccion/read-only | >= 80% APIs/eventos reporting | consolidacion y reporte semanal |

## 5) Excepciones y exclusiones justificadas
Toda exclusion debe registrarse con:
- alcance excluido,
- motivo,
- riesgo,
- mitigacion,
- fecha de revision/cierre.

### Registro vigente de exclusiones del baseline
| ID | Exclusion | Justificacion | Riesgo | Mitigacion aplicada | Estado |
|---|---|---|---|---|---|
| EXC-001 | pruebas E2E de canales externos reales de notificacion | proveedor externo fuera del baseline tecnico local | bajo para `MVP` | dobles de canal + contract tests + smoke controlado | Aceptada en baseline |
| EXC-002 | cobertura exhaustiva de regulacion por pais en retencion | regulacion detallada por pais queda fuera del baseline del ciclo | bajo para `MVP` | baseline `NFR-010` + trazabilidad a evolucion regulatoria | Aceptada en baseline |

## 6) Deuda de pruebas
Registrar deuda de forma explicita y trazable.

### Registro vigente de deuda
No hay deuda documental de diseno abierta en este pilar.

Nota: la deuda de ejecucion/certificacion se registra por release cuando existan corridas reales y hallazgos asociados.

## 6.1 Estado de cobertura del baseline
| Dimension | Estado actual | Fuente de control |
|---|---|---|
| Cobertura definida | Cerrada para `FR-001..FR-011` y `NFR-001..NFR-011` | [Mapeo de Pruebas](/mvp/calidad/mapeo-pruebas/) |
| Cobertura implementada | Variable por servicio/release; no inferida documentalmente | suites por servicio + [Registro de Ejecuciones](/mvp/calidad/evidencias/registro-ejecuciones/) |
| Cobertura ejecutada | Pendiente de corridas reales por release candidato | [Registro de Ejecuciones](/mvp/calidad/evidencias/registro-ejecuciones/) |
| Cobertura validada con evidencia | Pendiente de aprobacion de gates por release | [Acta de Certificacion Minima](/mvp/calidad/evidencias/acta-certificacion-minima/) |
| Cobertura incluida en certificacion minima | Definida como paquete critico representativo | [Plan de Certificacion Minima](/mvp/calidad/evidencias/baseline-certification-plan/) |
| Cobertura diferida a releases/hardening | No funcional profundo y operacion avanzada | [Desviaciones y Aceptaciones](/mvp/calidad/evidencias/desviaciones-aceptaciones/) |

## 7) Criterio de aceptacion de cobertura
Se considera cobertura documental `cerrada` cuando:
- existe mapeo completo `FR/NFR` a escenarios y artefactos,
- minimos de diseno por nivel/servicio se cumplen,
- exclusiones y deuda estan explicitadas.

Se considera cobertura operacional `certificada` cuando:
- existen corridas registradas para el alcance del release,
- la evidencia cumple el modelo del pilar,
- los gates aplicables quedan aprobados.

Referencias:
- [Mapeo de Pruebas](/mvp/calidad/mapeo-pruebas/)
- [Puertas de Calidad](/mvp/calidad/puertas-calidad/)
- [Modelo de Evidencia](/mvp/calidad/modelo-evidencia/)

## Historial
- 2026-03-28: se establece el modelo de cobertura por tipo, minimos por nivel/servicio y registro formal de exclusiones/deuda de pruebas.
- 2026-03-28: se separa cobertura documental de certificacion operacional con evidencia.
