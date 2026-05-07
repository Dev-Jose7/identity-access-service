---
title: "Comunicacion y Postmortem"
linkTitle: "3. Comunicacion y Postmortem"
weight: 3
url: "/mvp/operacion/incidentes/comunicacion-postmortem/"
---

## Comunicacion durante incidente
- Abrir canal unico de incidente con timestamp de hitos.
- Emitir actualizacion periodica segun severidad:
  - Sev 1: cada 15 min
  - Sev 2: cada 30 min
  - Sev 3: por hitos relevantes
- Comunicar impacto, estado, mitigacion y ETA tentativa.

## Postmortem
Se requiere para:
- todo Sev 1;
- Sev 2 con causa sistemica o recurrencia.

## Contenido minimo de postmortem
- resumen ejecutivo
- linea de tiempo
- causa raiz y factores contribuyentes
- acciones correctivas/preventivas
- owner y fecha compromiso por accion
- referencia a evidencias (logs, metricas, trazas)

## Principio
Postmortem sin culpa personal; enfocado en aprendizaje sistemico.
