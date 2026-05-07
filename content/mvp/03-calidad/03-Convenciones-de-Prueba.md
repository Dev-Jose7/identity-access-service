---
title: "Convenciones de Prueba"
linkTitle: "3. Convenciones de Prueba"
weight: 3
url: "/mvp/calidad/convenciones-prueba/"
---

## Proposito
Estandarizar la definicion, implementacion y ejecucion de pruebas de `MVP` para que todos los servicios evalen de forma consistente Producto, Dominio y Arquitectura.

## 1) Taxonomia de estados (uso obligatorio)
- `Definido`: requisito identificado para cobertura.
- `Disenado`: escenario documentado con Given/When/Then.
- `Listo para implementar`: escenario disenado con owner y artefacto objetivo.
- `Implementado`: prueba automatizada disponible.
- `Ejecutado`: corrida registrada con resultado y metadatos.
- `Validado con evidencia`: ejecucion evaluada y aprobada en gate con evidencia verificable.

Regla: no usar `Validado` sin el sufijo `con evidencia` ni sin referencia de evidencia.

## 2) Convencion unica de naming

### 2.1 Regla general
Formato canonico para nombre de caso:
`<nivel>_<servicio>_<modulo>_<comportamiento>_<resultadoEsperado>`

Ejemplos:
- `unit_order_checkout_confirmarPedido_creaPendingApproval`
- `integration_inventory_reserva_crearReserva_rechazaStockInsuficiente`
- `e2e_order_notification_orderConfirmed_disparaNotificacionNoBloqueante`

### 2.2 Sufijos por tipo de validacion
- `..._ok` para flujo valido.
- `..._rechaza<ErrorCanonico>` para flujo invalido esperado.
- `..._idempotente` para reintentos/duplicados.
- `..._sinSideEffects` para rechazo sin mutacion.

## 3) Estructura Given/When/Then

### 3.1 Plantilla obligatoria
Toda prueba debe documentar y materializar:
- `Given`: contexto inicial, datos y precondiciones de dominio.
- `When`: accion/trigger exacto (API, comando, evento o scheduler).
- `Then`: resultado observable (respuesta, estado persistido, evento, auditoria, metricas).

### 3.2 Reglas de redaccion
- Un solo `When` por prueba.
- `Then` debe incluir al menos una evidencia de negocio y una tecnica.
- No mezclar varios errores esperados en un mismo caso.

## 4) Fixtures y datos de prueba

### 4.1 Convencion de fixtures
Carpeta recomendada por servicio:
- `tests/<servicio>/<nivel>/fixtures/`

Naming de fixture:
`fixture_<contexto>_<estado>.json` o `fixture_<contexto>_<estado>.yaml`

Ejemplos:
- `fixture_order_pending_approval.json`
- `fixture_inventory_sku_sellable_with_stock.json`

### 4.2 Politica de datos
- Usar datos deterministas y repetibles.
- Separar datos `validos` y `invalidos`.
- Evitar datos reales de PII; usar anonimizados/sinteticos.
- Cada fixture debe indicar `tenantId` y, cuando aplique, `countryCode`.

### 4.3 Semillas minimas comunes
- Tenant base: `org-co-001`.
- Tenant alterno para aislamiento: `org-ec-001`.
- Usuario habilitado: `usr-b2b-active`.
- Usuario bloqueado: `usr-b2b-blocked`.
- SKU vendible: `SKU-SELLABLE-001`.
- SKU no disponible: `SKU-OOS-001`.

## 5) Manejo de idempotencia en pruebas

### 5.1 Regla general
Todo comando mutante probado en integracion/e2e debe incluir verificacion de idempotencia.

### 5.2 Casos obligatorios
- Misma clave + mismo payload -> mismo resultado funcional (`idempotente`).
- Misma clave + payload distinto -> conflicto de idempotencia o rechazo semantico.
- Reintento tras timeout tecnico -> no duplica side effects.

### 5.3 Evidencia minima
- respuesta API consistente entre reintentos;
- una sola mutacion efectiva en DB;
- una sola publicacion de evento de negocio (o dedupe verificado en consumo).

## 6) Reglas para tests async/eventos

### 6.1 Dedupe (`processed_event`)
- Consumidor debe tratar duplicado como `noop idempotente`.
- Prueba minima:
  - consumir el mismo `eventId` 2 veces;
  - verificar un solo efecto de negocio;
  - verificar registro de dedupe/processed_event.

### 6.2 Outbox
- Eventos de core se verifican en 2 pasos:
  - persistencia en outbox al materializar decision de dominio;
  - publicacion posterior sin reabrir transaccion de negocio.
- Fallo de publicacion no invalida decision ya materializada.

### 6.3 DLQ
- Mensaje no recuperable debe enrutarse a DLQ.
- Prueba minima:
  - evento malformado/no compatible;
  - consumidor clasifica como no reintentable;
  - evidencia en DLQ + auditoria operacional.

### 6.4 Retries
- Retry solo para fallos transitorios.
- Prueba minima:
  - falla transitoria inicial;
  - reintento dentro de politica definida;
  - convergencia sin duplicar side effects.

## 7) Estandar de errores canonicos

### 7.1 Regla de validacion
Cuando un flujo falla por negocio, la prueba debe verificar:
- codigo/identificador semantico esperado (ej. `stock_insuficiente`);
- categoria HTTP/mensaje acorde;
- ausencia de mutaciones no permitidas.

### 7.2 Errores minimos obligatorios en suite
- `acceso_cruzado_detectado`
- `stock_insuficiente`
- `reserva_expirada`
- `conflicto_checkout`
- `transicion_estado_invalida`
- `pago_duplicado`
- `token_expirado_o_revocado`
- `configuracion_pais_no_disponible`

## 8) Verificacion de `traceId` y `correlationId`

### 8.1 Regla transversal
Toda prueba de integracion/e2e sobre mutaciones o consumo de eventos debe verificar propagacion de trazabilidad.

### 8.2 Puntos de control
- Request/command de entrada incluye `traceId` y `correlationId`.
- Respuesta/error conserva trazabilidad.
- Evento publicado conserva los mismos IDs (o relacion documentada de correlacion).
- Auditoria/log estructurado contiene ambos campos.

### 8.3 Criterio de aceptacion de trazabilidad
Se considera `Ejecutado` cuando:
- existe corrida registrada con verificacion de la cadena `entrada -> decision -> persistencia -> evento/log`.

Se considera `Validado con evidencia` cuando:
- la corrida ejecutada referencia evidencia verificable,
- la evidencia queda aprobada en el gate aplicable.

## 9) Relacion caso-corrida-evidencia
- Caso de prueba: especifica comportamiento esperado y trazabilidad (`FR/NFR/RN-I-D`).
- Corrida: instancia de ejecucion de uno o mas casos en un entorno y fecha.
- Evidencia: artefacto verificable producido por la corrida.

Referencias:
- [Puertas de Calidad](/mvp/calidad/puertas-calidad/)
- [Modelo de Evidencia](/mvp/calidad/modelo-evidencia/)
- [Registro de Ejecuciones](/mvp/calidad/evidencias/registro-ejecuciones/)

## 10) Criterios de calidad de una prueba
- Determinista: mismo resultado en ejecuciones repetidas.
- Aislada: no depende de orden global de suite.
- Observable: evidencia tecnica y de negocio verificable.
- Trazable: referencia explicita a `FR/NFR/RN-I-D` en el caso.

## Historial
- 2026-03-28: se establece la convencion unica de ejecucion para `03-calidad` (naming, GWT, fixtures, idempotencia, async/eventos, errores canonicos y trazabilidad tecnica).
- 2026-03-28: se normaliza taxonomia de estados y criterios de `Ejecutado`/`Validado con evidencia`.
