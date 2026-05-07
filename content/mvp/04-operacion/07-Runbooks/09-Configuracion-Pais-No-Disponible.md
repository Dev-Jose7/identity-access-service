---
title: "Configuracion Pais No Disponible"
linkTitle: "9. Configuracion Pais No Disponible"
weight: 9
url: "/mvp/operacion/runbooks/configuracion-pais-no-disponible/"
---

## Proposito
Gestionar bloqueos operativos por ausencia de configuracion regional vigente.

## Senal de entrada
- Error canonico configuracion_pais_no_disponible.
- Bloqueo en checkout u operacion critica regional.

## Senales para delimitar alcance
| Senal | Interpretacion |
|---|---|
| error concentrado en un `countryCode` | politica ausente o vencida en ese pais |
| error en multiples paises tras cambio | posible falla de propagacion/configuracion global |
| error solo en un tenant | configuracion organizacional incompleta |

## Impacto esperado
No se procesa operacion critica para countryCode afectado.

## Diagnostico inicial
- Validar existencia y vigencia de politica regional.
- Confirmar countryCode, tenant y operacion bloqueada.

## Regla de interpretacion HTTP del incidente
- `404 configuracion_pais_no_disponible`: ausencia del recurso tecnico al
  consultar politica regional en `directory`.
- `409 configuracion_pais_no_disponible`: bloqueo de operacion de negocio en
  `order`/`reporting` por precondicion regional no satisfecha.
- Ambos casos representan el mismo error estable y deben auditarse.

## Decision operativa (si X entonces Y)
| Si | Entonces |
|---|---|
| politica no existe | crear/publicar version valida y auditable |
| politica existe pero no propaga | validar cache/sincronizacion y forzar recarga controlada |
| politica vigente pero error persiste | revisar mapeo `countryCode` y validacion en servicio consumidor |

## Contencion
- Mantener bloqueo (no hay fallback global implicito).
- Comunicar impacto por pais/tenant.

## Criterio de contencion cumplida
- bloqueo se mantiene solo para alcance afectado;
- no se habilitan bypasses fuera de baseline;
- stakeholders informados con alcance y ETA.

## Recuperacion
- Publicar configuracion regional valida.
- Verificar propagacion de configuracion en servicios.

## Verificacion posterior
- Cesa emision del error canonico para pais corregido.
- Operacion critica vuelve a estado normal.

## Checklist de cierre
| Item | Criterio |
|---|---|
| vigencia politica | configuracion regional activa y auditada |
| comportamiento funcional | operacion critica ejecuta para pais afectado |
| trazabilidad | log canonico deja de aparecer en ventana de verificacion |
| evidencia | version aplicada + auditoria + prueba funcional registradas |

## Escalamiento
Sev 2 o Sev 1 segun cobertura de pais afectado.

## Evidencia a registrar
- version de configuracion aplicada
- auditoria de cambio
- validacion funcional posterior

## Artefactos relacionados
- directory-service/contracts/01-APIs
- 03-Gestion-de-Configuracion-y-Secretos
