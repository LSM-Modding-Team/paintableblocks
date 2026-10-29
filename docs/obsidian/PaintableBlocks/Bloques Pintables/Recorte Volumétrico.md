# Recorte volumétrico

Reglas aceptadas, con código preparado y revisión estática; sin verificación en Minecraft. Amplía el [[Martillo]] sin sustituir su recorte individual. No se limita a superficies planas y alcanza minicubos interiores.

## Selección y confirmación

1. Ctrl + clic derecho marca la primera esquina sobre un bloque pintable al alcance normal.
2. Otro Ctrl + clic derecho marca la esquina opuesta, también al alcance normal. Se permite desplazarse entre ambas marcas.
3. Se previsualizan el volumen y los minicubos afectados.
4. Clic derecho confirma; cambiar de herramienta o pulsar Escape cancela.

La selección pertenece al jugador, no al objeto. Para confirmar debe conservar el mismo martillo y permanecer en la misma dimensión. Las esquinas se ajustan a la cuadrícula de los bloques apuntados. Se retiran celdas cuyo centro esté dentro del volumen; no se corta parcialmente una celda con el martillo.

Se admiten distintas subdivisiones en una operación. Los bloques ajenos, huecos y pintables 1×1 se ignoran y se indican en la vista previa. En pendientes se elimina también cualquier fragmento inclinado contenido en la celda seleccionada.

## Límites y resultado

Máximo **256×256×256 posiciones de bloque**, contando ambos extremos: nunca 257 posiciones por eje. Es un límite espacial, no una promesa de ejecución instantánea. No cargar zonas descargadas. Si la selección las incluye, exigir ajustarla antes de ejecutar.

Los recortes son irreversibles y no devuelven materiales ni pintura. Las superficies supervivientes conservan sus pinturas; las nuevas aparecen sin pintar. El bloque desaparece cuando no queda volumen.

Si cambia un bloque afectado antes de confirmar, invalidar la previsualización y exigir una nueva selección. La comprobación inicial de permisos rechaza toda la operación si encuentra una posición afectada sin autorización. El servidor vuelve a validar permisos y estado al procesar cada lote.

## Operaciones grandes

Una operación activa por jugador, procesada por lotes con presupuesto compartido por servidor. Recorrer bloques y calcular su intersección, sin enumerar en memoria todos los minicubos del volumen. El tamaño del lote y el presupuesto son ajustes técnicos pendientes de medición, no nuevas reglas de producto.

Cancelar, desconectarse, descargar una zona necesaria o perder permisos detiene los lotes pendientes. **Lo ya recortado permanece**; avisarlo antes de confirmar. No hay reversión ni reanudación automática al abrir el mundo.

Mostrar contorno global, tipos ignorados, disponibilidad y progreso en bloques revisados/modificados. Detallar minicubos cercanos sin intentar representar individualmente todos los del volumen. Vista previa y operación comparten el cálculo del resultado.


## Estado y evidencia

Implementación y decisiones: [Pendientes y volúmenes](../../../technical/slopes-and-volumes.md). No se ha compilado ni ejecutado Java/Minecraft; las reglas no equivalen a comportamiento probado.
