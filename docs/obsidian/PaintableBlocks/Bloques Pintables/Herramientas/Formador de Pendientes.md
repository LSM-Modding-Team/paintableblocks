# Formador de pendientes

Herramienta implementada en código con el nombre definitivo **Formador de pendientes**; revisión estática, sin verificación en Minecraft. Una única variante inicial, sin niveles de materiales. Sus recursos definitivos se crean después de completar el ítem y sus funciones.

## Uso

Seleccionar dos esquinas de una región de bloques pintables. Una pequeña interfaz permite elegir dirección de ascenso y lado conservado, muestra longitud/anchura/altura y relación de ascenso, y permite revisar la previsualización antes de confirmar. Máximo 16×16×16 bloques por operación.

La transformación solo retira volumen. Respeta huecos, pinturas supervivientes y bloques ajenos según [[Pendientes]]. Servidor y previsualización deben compartir el cálculo; comprobar permisos antes de modificar.

## Durabilidad

Consume **un punto por bloque realmente modificado**. Si no dispone de durabilidad suficiente para toda la operación, no empieza. Las operaciones sin cambios no desgastan la herramienta.

Decisión de implementación autorizada: nombre definitivo **Formador de pendientes**, **4096 puntos** y receta con tres lingotes de hierro en la fila superior, diamante central y palo bajo el diamante. Produce una herramienta y se desbloquea con diamante. Creativo no desgasta; el coste por bloque no se reduce aleatoriamente por Irrompibilidad.

## Creativo

Además de transformar piezas existentes, ofrece un modo para colocar piezas nuevas con dimensiones configurables. Inicialmente mantiene el máximo de 16×16×16 por operación. La pestaña de bloques ofrece una pendiente básica por subdivisión, no todas las combinaciones posibles.


## Estado y evidencia

Implementación y decisiones: [Pendientes y volúmenes](../../../../technical/slopes-and-volumes.md). No se ha compilado ni ejecutado Java/Minecraft; las reglas no equivalen a comportamiento probado.
