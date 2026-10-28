# Molino de pintura

Produce **4 ítems de pintura (1024 unidades)** por bloque del catálogo en **cuatro segundos**, sin combustible. Cada ítem contiene 256 unidades. No cambia el rendimiento según el nivel de rasqueta: es una ruta de producción independiente.

## Procesamiento y contenido

Antes de procesar debe caber el lote completo en las salidas. Si no cabe, espera sin consumir materiales y no entrega fracciones. El contenido no se pierde por inventarios llenos ni se tira al suelo.

Al recoger la máquina, un único ítem conserva su contenido y lo restaura al colocar, sin soltarlo otra vez por separado. Los pistones no pueden moverla. Si sobrevive como drop de una explosión, conserva contenido en ese ítem.

## Progresión

Supera a la rasqueta manual y tiene menor rendimiento que la prensa.

La prensa se obtiene mejorando un molino vacío. Los patrones exactos de fabricación no se han definido; no se importan de `START_HERE.md`. La materia prima respeta el [[Catálogo de Pinturas]] y sus restricciones de creativo.

El ítem de la máquina se representa en 3D y las texturas propias son 16×16. Véanse [[Obtención]], [[Recetas]] y [[Modelos y Texturas]].
