# Decisiones pendientes

## Requisitos ya aceptados

Las reglas de esta documentación forman parte definitiva del mod. No solicitar otra aprobación para implementarlas. El mapa por dependencias de [[Plan de Fases]], los límites de [[Arquitectura]] y los criterios de [[Criterios de División]] también están aceptados.

La función de cada herramienta precede a sus recursos definitivos. Las decisiones técnicas rutinarias se resuelven dentro de esos límites; solo se consulta cuando falta una regla que afecta al comportamiento.

## Información de producto aún no especificada

| Tema | Información disponible | Qué no se debe inventar |
|---|---|---|
| Recetas de herramientas y recipientes | Familias de ingredientes y relaciones en [[Recetas]] | Cantidades y posiciones que nunca se concretaron |
| Recetas de estaciones y máquinas | Mejoras de estación anterior vacía, costes/rendimientos de operación | Patrones supuestamente procedentes de `START_HERE.md`, que no fue aportado |

La solicitud de completar documentación incorpora toda la información disponible; no convierte un patrón ausente en una receta acordada. Esto bloquea solo la receta afectada, no el desarrollo de las funciones ya definidas.

## Recursos y trabajo por ejecutar

| Trabajo | Requisito y condición |
|---|---|
| Integrar `unpainted.png` original | Localizar el archivo original y comprobar 16×16 sin recrearlo; esta documentación no acredita su integración |
| Implementar sistemas | Elegir tareas disponibles de [[Plan de Fases]]; no confundir aprobación con código terminado |
| Modelos y texturas de herramientas | Habilitados individualmente después de completar funciones; véase [[Modelos y Texturas]] |
| Verificación en Minecraft | Requiere petición expresa; no afirmar resultados sin ejecución |

Estos son trabajos pendientes, no decisiones de producto que deban aprobarse otra vez. La arquitectura y el método de dividir el desarrollo ya están definidos en sus páginas.

## Documentos de referencia anteriores

Los adjuntos técnicos sirvieron de comparación, no de sustitución automática del planteamiento. Prevalecen las decisiones actuales: cinco resoluciones hasta 16×16, 256 unidades por ítem, conversión exclusiva de estaciones, orientación conjunta con 24 giros y prensa sin combustible.

Los IDs del catálogo se toman del CSV: nieve en polvo es `paint_id` 232; 233 corresponde a prismarina. No copiar el ID erróneo de los adjuntos ni importar sus recetas ausentes, tamaños de texturas o supuestos de implementación.

## Resoluciones durante el grupo 1

El usuario autorizó decidir sin consulta durante este avance. Se documentaron las resoluciones de textura provisional, pinturas exclusivas de creativo, iluminación de recortes, datos inválidos y caché en [Grupo 1](../../../../technical/group1.md).

El original `unpainted.png` sigue pendiente de integrar; no se ha recreado. Tenazas y guantes ya tienen funciones en código y habilitan sus recursos definitivos individuales. Las recetas sin patrones concretos corresponden al futuro frente de recetas; no impiden el acceso creativo al contenido preparado.

## Estado vigente tras acordar las ampliaciones

Las decisiones de [[Recorte Volumétrico]], [[Pendientes]], [[Pintura de Pendientes]] y [[Pestañas del Creativo]] están aceptadas y no necesitan aprobación posterior. No están implementadas por esta entrega.

Quedan como decisiones de producto el **nombre definitivo, receta y durabilidad máxima del formador**. Su nombre actual es provisional; no inventar cifras ni ingredientes. Equilibrar juntos receta y durabilidad respecto al martillo y la progresión.

La colisión a 1/16, el comportamiento de uniones y el rendimiento de selecciones grandes requieren verificación posterior. Presupuesto por tick, tamaño de lote y detalle de previsualización son ajustes técnicos sujetos a medición, no funciones nuevas por aprobar.

Las notas anteriores sobre PNG original pendiente y recetas generales ausentes son históricas: el original ya está integrado, los grupos 1–3 y recursos visuales están publicados; las recetas concretadas se recogen en [recetas del grupo 3](../../../../technical/group3-recipes.md). No confundirlas con la receta todavía ausente del formador.
