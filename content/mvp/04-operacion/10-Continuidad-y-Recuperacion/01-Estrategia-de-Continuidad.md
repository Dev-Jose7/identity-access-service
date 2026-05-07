---
title: "Estrategia de Continuidad"
linkTitle: "1. Estrategia de Continuidad"
weight: 1
url: "/mvp/operacion/continuidad-recuperacion/estrategia-continuidad/"
---

## Objetivo
Mantener continuidad del MVP ante fallas parciales de servicios,
dependencias o datos, priorizando restauracion del flujo core.

## Principios
- Recuperar primero capacidades criticas de negocio.
- Aplicar degradacion controlada en lugar de indisponibilidad total.
- Mantener trazabilidad de decisiones operativas.
- Evitar cambios riesgosos durante ventana de recuperacion.

## Dependencias criticas de continuidad
- `identity-access-service` y `api-gateway-service` para acceso al sistema.
- `directory-service` para configuracion organizacional/regional.
- `catalog-service`, `inventory-service`, `order-service` para flujo comercial.
- Broker de eventos para sincronizacion y procesos derivados.
- Bases de datos por servicio como fuente transaccional primaria.

## Escenarios base
- caida de servicio individual
- degradacion de base de datos
- atraso severo de consumidores/eventos
- falla en configuracion regional critica
- caida parcial de broker o degradacion de publish/consume
- perdida parcial de configuracion operativa/secretos

## Escenarios de perdida
| Tipo | Ejemplo | Objetivo operativo |
|---|---|---|
| Parcial | un servicio o consumidor degradado | restaurar flujo critico con degradacion controlada |
| Extendida | multiples servicios del flujo core | recuperar acceso + checkout/pedido minimo |
| Configuracion | ausencia de politica regional o parametro critico | bloquear de forma auditable y restaurar configuracion valida |

## Restauracion minima aceptable por flujo
- Autenticacion: login/refresh funcional para usuarios habilitados.
- Checkout/pedido: crear pedido con validacion de stock y politica regional.
- Eventos: publicacion/consumo reanudado sin crecimiento sostenido de DLQ.
- Soporte: notificaciones y reporting pueden degradarse temporalmente sin
  bloquear core transaccional.

## Evidencia operativa exigida
- incidente con timeline y decisiones.
- metricas pre y post recuperacion.
- evidencia de smoke de los flujos restaurados.
