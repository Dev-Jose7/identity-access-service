---
title: "Trazabilidad"
linkTitle: "4. Trazabilidad"
weight: 4
url: "/mvp/producto/trazabilidad/"
---

## Proposito del archivo
Tablero de trazabilidad del pilar Producto para conectar:
- `01-SRS-Ligero.md` (outcomes de negocio)
- `02-Catalogo-RF/` (que funcional)
- `03-Catalogo-RNF/` (que no funcional)

Este archivo no define el detalle interno de otros pilares; solo gestiona relaciones y estado de producto.

## Alcance de la trazabilidad
- Incluye: cobertura de outcomes, estado de requisitos, dependencias funcionales, evidencia documental de producto.
- No incluye: decisiones internas de dominio, diseno tecnico, implementacion tecnica o runbooks operativos.

## Convenciones
- IDs validos: `FR-001...` y `NFR-001...`.
- Estado de aprobacion permitido: `Draft | Approved | Rejected`.
- Estado de implementacion permitido: `Pendiente | En progreso | Hecho | Deprecado`.
- Ciclo de vida actual: `MVP`.
- Outcomes SRS vigentes: `M1..M5` (definidos en [SRS Ligero](/mvp/producto/srs-ligero/)).
- `N/A` en `Outcome` significa que el requisito es habilitador y no mide directamente un outcome del SRS.

## Definicion de trazabilidad (Producto)
Cadena oficial de trazabilidad:
`Outcome (SRS) -> FR/NFR -> Evidencia de producto -> Estado`

Reglas minimas:
1. Todo FR/NFR debe tener ID, prioridad, estado de aprobacion, estado de implementacion.
2. Todo FR/NFR debe mapear a `0..n` outcomes (`N/A` permitido cuando sea habilitador).
3. Todo FR debe tener criterios de aceptacion en su archivo.
4. Todo NFR debe tener metrica, umbral y metodo de validacion en su archivo.
5. Nada pasa a `Hecho` sin evidencia de aceptacion de producto vinculada.

## Matriz principal de requisitos

| ID | Tipo | Prioridad | Aprobacion | Implementacion | Outcome(s) | Dependencias | Evidencia actual |
|---|---|---|---|---|---|---|---|
| FR-001 | FR | Must | Approved | Pendiente | N/A | FR-009 | [FR-001](/mvp/producto/catalogo-rf/fr-001/) |
| FR-002 | FR | Must | Approved | Pendiente | M2 | FR-001, NFR-004 | [FR-002](/mvp/producto/catalogo-rf/fr-002/) |
| FR-003 | FR | Should | Approved | Pendiente | N/A | FR-002, NFR-002 | [FR-003](/mvp/producto/catalogo-rf/fr-003/) |
| FR-004 | FR | Must | Approved | Pendiente | M1, M2, M3, M5 | FR-001, FR-002, NFR-005 | [FR-004](/mvp/producto/catalogo-rf/fr-004/) |
| FR-005 | FR | Must | Approved | Pendiente | N/A | FR-004, FR-002 | [FR-005](/mvp/producto/catalogo-rf/fr-005/) |
| FR-006 | FR | Should | Approved | Pendiente | M4, M5 | FR-004, NFR-007 | [FR-006](/mvp/producto/catalogo-rf/fr-006/) |
| FR-007 | FR | Should | Approved | Pendiente | M4 | FR-004, NFR-002 | [FR-007](/mvp/producto/catalogo-rf/fr-007/) |
| FR-008 | FR | Could | Approved | Pendiente | N/A | FR-004, FR-006, NFR-007 | [FR-008](/mvp/producto/catalogo-rf/fr-008/) |
| FR-009 | FR | Must | Approved | Pendiente | M1 | NFR-005, NFR-006 | [FR-009](/mvp/producto/catalogo-rf/fr-009/) |
| FR-010 | FR | Should | Approved | Pendiente | N/A | FR-004, NFR-006 | [FR-010](/mvp/producto/catalogo-rf/fr-010/) |
| FR-011 | FR | Must | Approved | Pendiente | N/A | FR-003, FR-007, FR-009, NFR-010 | [FR-011](/mvp/producto/catalogo-rf/fr-011/) |
| NFR-001 | NFR | Must | Approved | Pendiente | M3 | - | [NFR-001](/mvp/producto/catalogo-rnf/nfr-001/) |
| NFR-002 | NFR | Should | Approved | Pendiente | N/A | - | [NFR-002](/mvp/producto/catalogo-rnf/nfr-002/) |
| NFR-003 | NFR | Must | Approved | Pendiente | M5 | - | [NFR-003](/mvp/producto/catalogo-rnf/nfr-003/) |
| NFR-004 | NFR | Must | Approved | Pendiente | M2 | - | [NFR-004](/mvp/producto/catalogo-rnf/nfr-004/) |
| NFR-005 | NFR | Must | Approved | Pendiente | N/A | - | [NFR-005](/mvp/producto/catalogo-rnf/nfr-005/) |
| NFR-006 | NFR | Must | Approved | Pendiente | N/A | - | [NFR-006](/mvp/producto/catalogo-rnf/nfr-006/) |
| NFR-007 | NFR | Must | Approved | Pendiente | M1, M4 | - | [NFR-007](/mvp/producto/catalogo-rnf/nfr-007/) |
| NFR-008 | NFR | Should | Approved | Pendiente | M3 | - | [NFR-008](/mvp/producto/catalogo-rnf/nfr-008/) |
| NFR-009 | NFR | Should | Approved | Pendiente | N/A | - | [NFR-009](/mvp/producto/catalogo-rnf/nfr-009/) |
| NFR-010 | NFR | Must | Approved | Pendiente | N/A | - | [NFR-010](/mvp/producto/catalogo-rnf/nfr-010/) |
| NFR-011 | NFR | Must | Approved | Pendiente | N/A | - | [NFR-011](/mvp/producto/catalogo-rnf/nfr-011/) |

## Matriz de outcome (SRS) -> cobertura de requisitos

| Outcome | Definicion resumida | FR asociados | NFR asociados | Cobertura FR | Cobertura NFR | Estado |
|---|---|---|---|---|---|---|
| M1 | Adopcion digital B2B | FR-004, FR-009 | NFR-007 | Si | Si | Completa |
| M2 | Control de sobreventa | FR-002, FR-004 | NFR-004 | Si | Si | Completa |
| M3 | Tiempo de confirmacion | FR-004 | NFR-001, NFR-008 | Si | Si | Completa |
| M4 | Reduccion de consultas manuales | FR-006, FR-007 | NFR-007 | Si | Si | Completa |
| M5 | Entrega a tiempo | FR-004, FR-006 | NFR-003 | Si | Si | Completa |

## Matriz de dependencias funcionales

| ID | Dependencias declaradas | Riesgo de bloqueo |
|---|---|---|
| FR-001 | FR-009 | Medio |
| FR-002 | FR-001, NFR-004 | Alto |
| FR-003 | FR-002, NFR-002 | Medio |
| FR-004 | FR-001, FR-002, NFR-005 | Alto |
| FR-005 | FR-004, FR-002 | Alto |
| FR-006 | FR-004, NFR-007 | Medio |
| FR-007 | FR-004, NFR-002 | Medio |
| FR-008 | FR-004, FR-006, NFR-007 | Medio |
| FR-009 | NFR-005, NFR-006 | Alto |
| FR-010 | FR-004, NFR-006 | Medio |
| FR-011 | FR-003, FR-007, FR-009, NFR-010 | Alto |

## Vista inter-pilares (alto nivel, sin detalle interno)

| Pilar | Estado de avance | Relacion desde Producto |
|---|---|---|
| 01-dominio | Alineado baseline `MVP` | Consume FR/NFR aprobados sin reinterpretar alcance funcional. |
| 02-arquitectura | Alineado baseline `MVP` | Materializa restricciones tecnicas y de despliegue, incluyendo empaquetado/ejecucion reproducible del backend. |
| 03-calidad | Alineado baseline `MVP` | Traza pruebas contra FR/NFR aprobados y valida baseline tecnico reproducible sin inventar ejecuciones. |
| 04-operacion | Alineado baseline `MVP` | Operacionaliza el baseline congelado (ambientes, releases, runbooks, seguridad y continuidad) sin reabrir alcance de producto. |

## Indicadores de salud del pilar Producto

| Indicador | Formula | Valor actual |
|---|---|---|
| Cobertura de outcomes | outcomes con FR+NFR / outcomes totales | 5/5 (100%) |
| Requisitos con outcome explicito | FR+NFR con outcome != N/A / total FR+NFR | 10/22 (45.5%) |
| Requisitos aprobados | FR+NFR en `Approved` / total FR+NFR | 22/22 (100%) |
| Requisitos en `Hecho` con evidencia de aceptacion | FR+NFR en `Hecho` con evidencia / FR+NFR en `Hecho` | 0/0 (N/A) |

## Checklist de mantenimiento (cada actualizacion)
1. Verificar que nuevos FR/NFR tengan ID, prioridad y estados validos.
2. Actualizar mapeo de outcomes si cambia el SRS o entra un requisito nuevo.
3. Revisar dependencias y riesgos de bloqueo.
4. Actualizar indicadores de salud.
5. Registrar cambios en `Decisiones de cambio`.

## Decisiones de cambio

| Fecha | ID(s) | Que cambio | Por que | Impacto |
|---|---|---|---|---|
| 2026-03-03 | FR-001..FR-010, NFR-001..NFR-010 | Se crea matriz inicial del pilar Producto. | Establecer baseline trazable por IDs. | Habilita trabajo por fases. |
| 2026-03-04 | FR-011, NFR-011 | Se agrega readiness regional por pais. | Formalizar expansion CO/EC/PE/CL en producto. | Amplia alcance funcional y no funcional del MVP. |
| 2026-03-08 | FR-001..FR-011, NFR-001..NFR-011 | Se redefine trazabilidad para que sea exclusivamente de producto y se separen claramente coverage, dependencias y salud. | Aumentar utilidad operativa del archivo sin invadir otros pilares. | Trazabilidad accionable para gestion de backlog, aprobacion y seguimiento del MVP. |
| 2026-03-10 | FR-001..FR-011, NFR-001..NFR-011 | Se migra el detalle de FR/NFR/Trazabilidad a estructura plana por seccion (sin subcarpetas `mvp` internas) y se normalizan enlaces ciclo-de-vida-first. | Mantener coherencia con el modelo ciclo-de-vida-first y simplificar navegacion/mantenimiento. | Reduce complejidad estructural y elimina ambiguedad de rutas en el pilar producto. |
| 2026-03-10 | FR-001..FR-011, NFR-001..NFR-011 | Se elimina el campo de responsable del pilar Producto y se alinea el estado agregado de catalogos a `Pendiente` (0/11). | Aplicar politica actual sin responsables y dejar consistencia entre resumenes e items. | Cierre documental limpio y sin contradicciones en estado ni metadatos. |
