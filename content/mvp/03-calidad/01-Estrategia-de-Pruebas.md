---
title: "Estrategia de Pruebas"
linkTitle: "1. Estrategia de Pruebas"
weight: 1
url: "/mvp/calidad/estrategia-pruebas/"
---

## Proposito
Definir el baseline de calidad de `MVP` y la estrategia global de pruebas para asegurar coherencia verificable entre Producto, Dominio y Arquitectura.

## 1) Preparacion y baseline (Dia 1)

### 1.1 Fuentes de verdad congeladas
Para `03-calidad`, las fuentes de verdad vigentes y congeladas son:
- [Producto](/mvp/producto/)
- [Dominio](/mvp/dominio/)
- [Arquitectura](/mvp/arquitectura/)

Reglas de uso:
- No reinterpretar semantica fuera de estos pilares.
- Toda prueba debe mapearse a IDs o reglas existentes.
- Cualquier contradiccion detectada se corrige primero en el pilar origen y luego se actualiza calidad.

### 1.2 Lista maestra de requisitos
Requisitos funcionales del ciclo:
- `FR-001` Catalogo base de productos/variantes/precios.
- `FR-002` Gestion de stock y disponibilidad.
- `FR-003` Reporte de abastecimiento.
- `FR-004` Checkout y creacion de pedido.
- `FR-005` Modificar pedido antes de confirmacion.
- `FR-006` Notificar cambio de estado de pedido.
- `FR-007` Reporte semanal de ventas.
- `FR-008` Recordatorio de carrito abandonado.
- `FR-009` Acceso por organizacion y rol.
- `FR-010` Registro de pago manual.
- `FR-011` Parametros operativos por pais.

Requisitos no funcionales del ciclo:
- `NFR-001` Rendimiento de operaciones criticas.
- `NFR-002` Calidad de reportes.
- `NFR-003` Disponibilidad operativa.
- `NFR-004` Control de sobreventa e integridad de stock.
- `NFR-005` Aislamiento por tenant.
- `NFR-006` Trazabilidad y auditoria.
- `NFR-007` Resiliencia y observabilidad operacional minima.
- `NFR-008` Presupuestos de tiempo operacional.
- `NFR-009` Versionado y compatibilidad de contratos.
- `NFR-010` Retencion y borrado/anonimizacion de datos.
- `NFR-011` Operacion regional por `countryCode` sin fallback global.

Fuentes:
- [Catalogo RF](/mvp/producto/catalogo-rf/)
- [Catalogo RNF](/mvp/producto/catalogo-rnf/)
- [Trazabilidad de Producto](/mvp/producto/trazabilidad/)

### 1.3 Lista maestra de invariantes y reglas
Baseline normativo de dominio que calidad debe verificar:
- Reglas de negocio `RN-*`.
- Invariantes de dominio `I-*`.
- Invariantes de datos `D-*`.

Agrupacion minima obligatoria para pruebas:
- Acceso/tenant: `RN-ACC-*`, `I-ACC-*`, `D-ACC-*`, `D-CROSS-01`.
- Inventario/reservas: `RN-INV-*`, `RN-RES-*`, `I-INV-*`, `D-INV-*`.
- Pedido/pago: `RN-ORD-*`, `RN-PAY-*`, `I-ORD-*`, `I-PAY-*`, `D-ORD-*`, `D-PAY-*`.
- Notificacion/reporting: `RN-NOTI-*`, `RN-REP-*`, `I-NOTI-*`, `I-REP-*`.
- Regionalizacion: `RN-LOC-01`, `I-LOC-01`, `D-DIR-02`.

Fuente:
- [Reglas e Invariantes](/mvp/dominio/reglas-invariantes/)

### 1.4 Lista maestra de errores canonicos
Se adopta como baseline el diccionario canonico del dominio. Errores criticos que deben tener pruebas dedicadas:
- `acceso_cruzado_detectado`
- `stock_insuficiente`
- `reserva_expirada`
- `conflicto_checkout`
- `transicion_estado_invalida`
- `pago_duplicado`
- `monto_pago_invalido`
- `token_expirado_o_revocado`
- `usuario_no_habilitado`
- `configuracion_pais_no_disponible`

Fuente:
- [Lenguaje Ubicuo - Diccionario de errores](/mvp/dominio/lenguaje-ubicuo/)

### 1.5 Lista maestra de contratos (API y eventos)
Contratos API por servicio (fuente de verdad para pruebas de contrato):
- [APIs IAM](/mvp/arquitectura/servicios/servicio-identidad-acceso/contratos/apis/)
- [APIs Directory](/mvp/arquitectura/servicios/servicio-directorio/contratos/apis/)
- [APIs Catalog](/mvp/arquitectura/servicios/servicio-catalogo/contratos/apis/)
- [APIs Inventory](/mvp/arquitectura/servicios/servicio-inventario/contratos/apis/)
- [APIs Order](/mvp/arquitectura/servicios/servicio-pedidos/contratos/apis/)
- [APIs Notification](/mvp/arquitectura/servicios/servicio-notificaciones/contratos/apis/)
- [APIs Reporting](/mvp/arquitectura/servicios/servicio-reporteria/contratos/apis/)

Contratos de eventos (fuente de verdad para consumer/producer contract tests):
- [Eventos de Dominio](/mvp/dominio/eventos-dominio/)
- [Contratos de Integracion](/mvp/dominio/contratos-integracion/)
- [Eventos IAM](/mvp/arquitectura/servicios/servicio-identidad-acceso/contratos/eventos/)
- [Eventos Directory](/mvp/arquitectura/servicios/servicio-directorio/contratos/eventos/)
- [Eventos Catalog](/mvp/arquitectura/servicios/servicio-catalogo/contratos/eventos/)
- [Eventos Inventory](/mvp/arquitectura/servicios/servicio-inventario/contratos/eventos/)
- [Eventos Order](/mvp/arquitectura/servicios/servicio-pedidos/contratos/eventos/)
- [Eventos Notification](/mvp/arquitectura/servicios/servicio-notificaciones/contratos/eventos/)
- [Eventos Reporting](/mvp/arquitectura/servicios/servicio-reporteria/contratos/eventos/)

### 1.6 Lista maestra de runtime criticos
Escenarios runtime minimos a cubrir desde calidad:
- Login y validacion de sesion.
- Checkout resiliente y creacion de pedido en `PENDING_APPROVAL`.
- Confirmacion comercial `PENDING_APPROVAL -> CONFIRMED`.
- Registro de pago manual.
- Side effects async (notification/reporting).
- Consumo idempotente de eventos (`processed_event` / dedupe).
- Regionalizacion obligatoria por `countryCode` sin fallback global.

Fuentes:
- [Vista de Ejecucion global](/mvp/arquitectura/arc42/vista-ejecucion/)
- Casos de uso en ejecucion de cada servicio (`/arquitectura-interna/casos-uso-ejecucion/`).

### 1.7 Catalogo de servicios a cubrir
Servicios obligatorios en el alcance de calidad `MVP`:
- `identity-access-service` (IAM)
- `directory-service`
- `catalog-service`
- `inventory-service`
- `order-service`
- `notification-service`
- `reporting-service`

## 2) Estrategia global de pruebas

### 2.1 Objetivo por nivel de prueba
| Nivel | Objetivo primario | Alcance esperado |
|---|---|---|
| Unitarias | validar reglas, invariantes y decisiones locales de dominio/aplicacion | componentes puros, politicas, agregados, validadores, mapeadores puros |
| Integracion | validar contratos internos/externos y persistencia/mensajeria | adapters, DB, outbox, broker, seguridad de borde+contexto |
| E2E | validar flujos de negocio completos y no regresion funcional | escenarios cross-servicio alineados a FR/NFR criticos |

### 2.2 Que valida cada nivel contra cada pilar
| Pilar | Unitarias | Integracion | E2E |
|---|---|---|---|
| Producto (FR/NFR) | reglas puntuales por caso de uso | endpoints/eventos y semantica de error por contrato | journeys completos contra FR/NFR priorizados |
| Dominio (invariantes/reglas/eventos) | `RN-*`, `I-*`, `D-*` y transiciones validas/invalidas | propagacion de eventos, idempotencia y dedupe en infraestructura | consistencia de comportamiento end-to-end y compensaciones |
| Arquitectura (contratos/resiliencia/seguridad/rendimiento) | validaciones de politicas locales | contratos API/eventos, retry, DLQ, aislamiento tenant, auditoria, datos | cumplimiento de runtime critico, presupuesto de latencia y resiliencia operacional |

### 2.3 Politica de priorizacion por riesgo y criticidad
Orden de prioridad para construir y ejecutar pruebas:
1. Riesgo `P1`: seguridad/tenant, sobreventa, creacion-confirmacion de pedido, pagos, regionalizacion.
2. Riesgo `P2`: notificaciones, reporting, sincronizaciones de soporte.
3. Riesgo `P3`: escenarios de conveniencia o baja criticidad operacional.

Criterios de criticidad del flujo:
- `Critico`: bloquea compra/cobro/seguridad/compliance basico.
- `Alto`: afecta continuidad operativa o trazabilidad obligatoria.
- `Medio`: degrada UX/operacion sin romper core.
- `Bajo`: impacto acotado y recuperable.

Regla de implementacion:
- Primero cubrir `P1 + Critico` en los 7 servicios.
- Luego `P2 + Alto` para robustez y convergencia async.
- Finalmente `P3` como cierre incremental sin bloquear gates.

### 2.4 Principios operativos de la estrategia
- Una sola verdad: toda prueba referencia IDs (`FR`, `NFR`, `RN/I/D`) y contrato fuente.
- No ambiguedad semantica: usar errores canonicos del dominio.
- Idempotencia por defecto en mutaciones y consumidores async.
- Seguridad transversal obligatoria: JWT en borde + autorizacion contextual en servicio.
- Regionalizacion obligatoria: sin politica vigente por `countryCode` se bloquea con `configuracion_pais_no_disponible`.

### 2.5 Verificacion de empaquetado y stack reproducible (baseline tecnico)
Como parte del baseline tecnico de `MVP` se verifica que:
- cada microservicio pueda construirse como artefacto versionado de
  empaquetado;
- el servicio arranque con configuracion externa por entorno;
- no existan secretos embebidos en el artefacto;
- el stack de integracion pueda levantarse de forma reproducible con el
  mecanismo multi-contenedor definido en arquitectura.

Alcance de verificacion:
- `local/dev/qa` de integracion y smoke multi-servicio.
- no implica declarar que el mecanismo de integracion sea la estrategia final de
  produccion.

## 3) Separacion explicita: diseno vs ejecucion vs certificacion

### 3.1 Baseline de pruebas (documental)
El pilar `03-calidad` queda cerrado como baseline documental cuando:
- existe mapeo completo de `FR/NFR` a escenarios y artefactos,
- existen convenciones y puertas de calidad aplicables,
- existe modelo formal de evidencia trazable.

### 3.2 Baseline de certificacion (operacional)
La certificacion del ciclo no se declara por diseno. Solo se declara cuando:
- el escenario fue ejecutado,
- hay evidencia verificable asociada,
- la evidencia fue evaluada y aprobada en el gate aplicable.

Referencias:
- [Mapeo de Pruebas](/mvp/calidad/mapeo-pruebas/)
- [Puertas de Calidad](/mvp/calidad/puertas-calidad/)
- [Modelo de Evidencia](/mvp/calidad/modelo-evidencia/)

## 4) Certificacion minima del baseline `MVP`

### 4.1 Nivel A: certificacion de consistencia del pilar
Para declarar `03-calidad` cerrado como pilar documental:
- trazabilidad completa `FR/NFR` -> escenarios -> artefactos;
- taxonomia de estados unificada y sin sobredeclaracion de evidencia;
- puertas de calidad y criterios de bloqueo/desbloqueo definidos;
- modelo de evidencia auditable y registro de corridas preparado.

### 4.2 Nivel B: certificacion minima del baseline
Para declarar el baseline listo para cierre integral antes de `04-operacion`, se exige un paquete minimo de suites criticas y evidencia asociada, sin implicar certificacion total de produccion.

El paquete minimo se define en:
- [Evidencias y Certificacion Minima](/mvp/calidad/evidencias/)
- [Plan de Certificacion Minima](/mvp/calidad/evidencias/baseline-certification-plan/)
- [Matriz de Resultados Esperados](/mvp/calidad/evidencias/matriz-resultados-esperados/)
- [Acta de Certificacion Minima](/mvp/calidad/evidencias/acta-certificacion-minima/)

## Historial
- 2026-03-28: baseline inicial de calidad con fuentes congeladas, lista maestra y estrategia global de pruebas por niveles.
- 2026-03-28: se explicita separacion entre baseline documental de pruebas y baseline de certificacion con evidencia.
