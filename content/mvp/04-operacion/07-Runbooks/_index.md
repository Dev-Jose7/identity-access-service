---
title: "7. Runbooks"
weight: 7
url: "/mvp/operacion/runbooks/"
---

Runbooks operativos de `MVP` para incidentes y degradaciones del flujo
core. Cada runbook define entrada, contencion, recuperacion, verificacion,
escalamiento y evidencia obligatoria.

Regla transversal para incidentes de runtime:
- verificar salud de servicio, version de imagen desplegada y configuracion
  inyectada por entorno;
- cuando el alcance sea local/dev/qa de integracion, validar reproduccion del
  incidente sobre stack `docker compose` antes de cerrar.
