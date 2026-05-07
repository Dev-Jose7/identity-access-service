---
title: "SRS Ligero"
linkTitle: "1. SRS Ligero"
weight: 1
url: "/mvp/producto/srs-ligero/"
---

## Proposito del archivo
Documento rector del producto para ArkaB2B. Define el problema, alcance, resultados esperados y politicas de negocio del MVP backend.

## 1. Resumen ejecutivo

### Problema
- Arka atiende clientes B2B de alto volumen y hoy combina procesos manuales con validaciones de stock no sincronizadas.
- Esta operacion provoca sobreventa, retrasos de confirmacion, reprocesos en despacho y baja trazabilidad de estado para el cliente.
- La situacion limita la expansion regional y afecta conversion, recompra y cumplimiento de entrega.

### Resultado esperado (outcome)
- Habilitar un backend de autogestion B2B para compras, ajustes y seguimiento de pedido.
- Reducir sobreventa y consultas manuales de estado/stock mediante reglas de negocio consistentes.
- Proveer reportes semanales accionables para ventas y abastecimiento.

### Restricciones clave
- MVP enfocado en backend; frontend a cargo de proveedor externo.
- Debe operar para Colombia y dejar readiness para Ecuador, Peru y Chile (formalizado en `FR-011` y `NFR-011`).
- El flujo core no puede permitir sobreventa ni acceso cruzado entre organizaciones.
- La entrega tecnica del MVP debe ser portable y reproducible entre ambientes
  controlados para desarrollo, integracion y operacion.

### Consistencia de entrega tecnica (MVP)
- El backend debe publicarse mediante un empaquetado tecnico estandarizado por
  servicio que habilite ejecucion reproducible en ambientes controlados.
- La reproducibilidad del stack de desarrollo/integracion es requisito de
  salida tecnica del ciclo, sin cambiar la semantica de negocio.

## 2. Proposito y alcance

### Que cubre
- Gestion de catalogo de productos y variantes (SKU), stock y disponibilidad.
- Gestion de carrito, confirmacion de pedido y seguimiento de estado.
- Notificaciones de cambio de estado y recordatorios de carrito abandonado.
- Reportes semanales de ventas y de productos por abastecer.
- Control de acceso por organizacion y rol de negocio.
- Registro de pago manual MVP para reflejar estado de pago del pedido.

### Que NO cubre (no-alcance)
- Desarrollo de frontend (responsabilidad de proveedor externo).
- Pasarela de pago online completa en esta fase.
- Analitica predictiva avanzada de demanda.
- Fulfillment extendido operativo (`READY_TO_DISPATCH`, `DISPATCHED`, `DELIVERED`).
- Operacion multi-region endurecida.
- Compliance/regulacion exhaustiva por pais.
- Permisos hipergranulares por mercado/segmento.
- Federacion enterprise con IdP externo.

### Supuestos
- El canal manual coexistira temporalmente como contingencia.
- El MVP prioriza confiabilidad del flujo de compra sobre amplitud funcional.
- Baseline y metas iniciales se apoyan de documentos del reto.

## 2.1 Supuestos congelados del baseline `MVP`
- El alcance funcional aprobado en FR-001..FR-011 y NFR-001..NFR-011 queda congelado para este ciclo.
- No se reabre semantica core de carrito/checkout/pedido/pago manual, stock/reserva, catalogo vendible ni aislamiento por tenant/rol dentro de `MVP`.
- La regionalizacion operativa se resuelve por `countryCode` y no admite fallback global implicito en operaciones criticas.
- El mecanismo de empaquetado tecnico y ejecucion reproducible por servicio
  forma parte del baseline de entrega del sistema.
- Lo clasificado como fuera de alcance o evolucion posterior no bloquea el cierre de producto del baseline actual.

## 3. Contexto del sistema

### Actores
- Comprador B2B recurrente.
- Administrador de cuenta B2B.
- Operador Arka (comercial/operaciones).
- Analista de inventario.
- Coordinador de despacho.

### Sistemas externos / integraciones
- Frontend externo (proveedor tercero).
- Servicio de correo/notificaciones.
- Sistemas de proveedores para abastecimiento (integracion progresiva).
- Sistema de reporteria/consumo interno.

### Diagrama simple de contexto
- El detalle arquitectonico del contexto queda planificado para el pilar de arquitectura (Gate 2).
- Flujo alto nivel: Usuario B2B -> Backend ArkaB2B -> Catalogo/Inventario/Pedidos/Notificaciones/Reportes.

## 4. Usuarios y escenarios

### Tipos de usuario
- Comprador B2B: crea y ajusta carrito, confirma pedido, consulta estado.
- Admin de cuenta B2B: administra datos de su organizacion.
- Operador Arka: gestiona estado de pedido, stock y pagos manuales.
- Analista de inventario: monitorea disponibilidad y faltantes.

### Jobs-to-be-done / casos principales
- Comprar por volumen sin validaciones manuales de disponibilidad.
- Modificar pedido antes del corte permitido por regla de negocio.
- Seguir el avance del pedido con estados consistentes.
- Recuperar oportunidades por carritos abandonados.
- Generar reportes semanales de ventas y abastecimiento.

## 5. Criterios de exito

### Metricas (1-5 maximo)
- M1 Adopcion digital B2B: organizaciones con >=1 pedido digital en 14 dias >= 12 en 60 dias.
- M2 Sobreventa: pedidos con faltante post-confirmacion <= 1.0% semanal.
- M3 Tiempo de confirmacion: promedio desde checkout hasta confirmacion <= 20 minutos.
- M4 Consultas manuales de estado/stock: reduccion >= 40% frente a baseline.
- M5 Entrega a tiempo: pedidos entregados en fecha comprometida >= 95%.

### Trazabilidad de metricas de exito (M -> FR/NFR)
| Metrica | FR/NFR fuente | Como se verifica |
|---|---|---|
| M1 Adopcion digital B2B | FR-004, FR-009, NFR-007 | Telemetria de pedidos por organizacion y dashboard de adopcion (Gate 4). |
| M2 Sobreventa | FR-002, FR-004, NFR-004 | Conciliacion semanal pedido vs disponibilidad (Gate 4). |
| M3 Tiempo de confirmacion | FR-004, NFR-001, NFR-008 | Metricas de latencia checkout->creacion pedido en pruebas de carga y operacion (Gate 3/4). |
| M4 Consultas manuales de estado/stock | FR-006, FR-007, NFR-007 | Tasa de contactos manuales vs uso de canales digitales (Gate 4). |
| M5 Entrega a tiempo | FR-004, FR-006, NFR-003 | Seguimiento de hitos de estado y cumplimiento operacional (Gate 4). |

### Kill criteria / criterios de descarte
- Si en semana 8 hay < 8 organizaciones activas digitales, se pausa expansion y se redefine onboarding.
- Si la conversion catalogo->pedido se mantiene < 20% por 4 semanas consecutivas, se recorta alcance al flujo core.
- Si la sobreventa supera 3% por 4 semanas consecutivas, se congela nueva funcionalidad hasta estabilizar inventario.

## 6. Politicas del producto

### Reglas del negocio de alto nivel (no tecnicas)
- RB-001: un pedido solo se confirma con disponibilidad validada y reserva vigente.
- RB-002: ninguna operacion mutante puede afectar recursos de otra organizacion.
- RB-003: un pedido solo puede modificarse antes del estado de confirmacion.
- RB-004: una falla de notificacion no revierte el core transaccional del pedido.
- RB-005: ventas y abastecimiento deben tener reporte semanal accionable.

### Reglas de cumplimiento (si aplican)
- CP-001: trazabilidad auditable de cambios en stock, estado de pedido y pagos.
- CP-002: retencion y borrado/anonimizacion de datos segun politica vigente por pais.
- CP-003: segregacion de datos por organizacion en toda consulta y mutacion.

## 7. Referencias
- [Catalogo RF](/mvp/producto/catalogo-rf/)
- [Catalogo RNF](/mvp/producto/catalogo-rnf/)
- [Trazabilidad](/mvp/producto/trazabilidad/)
- [Glosario](/mvp/producto/glosario/)
- [Adjunto 03 Backlog Arka](/mvp/producto/adjuntos/backlog-java-backend-arka/)
- [Adjunto 02 Proyecto Arka](/mvp/producto/adjuntos/proyecto-arka/)
- [Adjunto 01 Reto Backend V2](/mvp/producto/adjuntos/proyecto-java-backend-reto-v2/)

### Changelog breve
- 2026-03-03: version inicial completa del SRS-Lite para MVP backend ArkaB2B.
- 2026-03-04: se corrigen referencias internas y se agrega trazabilidad explicita M1..M5 a FR/NFR.
- 2026-03-07: se normaliza trazabilidad de metricas a estado real del repo (producto en foco) y se eliminan referencias no materializadas.
- 2026-03-10: se actualizan rutas a esquema ciclo-de-vida-first, enlaces de adjuntos y alineacion del SRS a la reestructuracion de catalogos FR/NFR/Trazabilidad sin subcarpetas de grupo.
