---
title: "Operacion del Broker"
linkTitle: "9.1 Broker"
weight: 1
url: "/mvp/operacion/eventos-dlq/operacion-broker/"
---

## Objetivo
Operar el broker compatible `Kafka` como canal oficial de eventos de dominio,
con confiabilidad suficiente para core y proyecciones.

## Controles operativos minimos
- Monitorear disponibilidad de cluster, throughput y latencia de publish.
- Monitorear lag por consumer group y particion.
- Monitorear ratio de retries y entradas a DLQ.
- Auditar cambios de topicos, particiones y ACL.

## Politica de topicos
- Naming versionado (`<bc>.<event-name>.v<major>`).
- Cambios breaking requieren nueva version y convivencia.
- Prohibido reutilizar topico para semantica diferente.

## Operacion de productores
- Outbox obligatorio para hechos de negocio materializados.
- Relay asincrono con reintentos controlados.
- Si falla publicacion, se conserva estado de negocio y se atiende por
  retry/DLQ sin rollback transaccional indebido.

## Operacion de consumidores
- Dedupe por `eventId` + referencia de consumidor.
- Retry solo en fallos transitorios.
- En fallo no recuperable: enrutar a DLQ y registrar evidencia.
