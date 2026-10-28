# Recetas

## Concreción durante el grupo 3

Por autorización del usuario para decidir reglas faltantes durante la implementación, los patrones y cantidades pendientes se concretaron en [recetas del grupo 3](../../technical/group3-recipes.md). Esa tabla sustituye las referencias históricas a patrones aún ausentes en esta página. Se mantiene el balance de lotes y las restricciones de contenido.

La selección creativa vigente es clic derecho de inventario con pintura sobre el pincel, como indica [[Pintura en Creativo]]; no se publica una receta creativa global.

Se recogen los ingredientes y relaciones aceptados. Los patrones y cantidades todavía no especificados no se deducen de los documentos de referencia.

## Bloques pintables

Una piedra central rodeada de ocho exteriores iguales produce bloques nuevos de 1×1. Las seis piedras centrales, materiales exteriores y rendimientos están en [[Fabricación]]. Las estaciones fabrican cualquiera de las cinco subdivisiones; la conversión es exclusiva de ellas, 1:1, completa y sin pintura.

## Familias de herramientas

| Herramienta | Ingredientes acordados |
|---|---|
| Martillo, cincel, raspador y tenazas | Hierro y palos |
| Mazo | Tablones y palos |
| Esponja de limpieza | Esponja seca y hierro |
| Guantes | Cuero y hierro |
| Llave de giro | Hierro y cobre |
| Pinceles | Palo, lana y material del nivel |
| Cubos de pintura | Recipiente propio fabricado con el material correspondiente |

Las rasquetas extraen una pintura por bloque; ocho bloques iguales alrededor de una rasqueta producen ocho ítems. La duración y la mejora a netherite están en [[Rasquetas]].

## Mejoras

- Banco → cortadora → ensambladora: se consume la estación anterior vacía.
- Molino → prensa: se consume el molino vacío.
- Pinceles y cubos: se conservan pintura y componentes si el destino tiene capacidad suficiente.
- Las mejoras a netherite de rasquetas, pinceles y cubos usan herrería con plantilla.

Una mejora de cubo de oro a diamante debe respetar la menor capacidad por pintura del diamante. Las recetas de estaciones y máquinas no tienen patrones exactos definidos: la referencia a `START_HERE.md` de los adjuntos no establece un patrón en esta documentación.

## Creativo

Una receta sin forma con pincel e ítem de pintura permite seleccionar esa identidad exclusivamente en creativo, sin crear unidades utilizables en supervivencia. Véase [[Pintura en Creativo]].

## Estado antes de programar

Los ingredientes y relaciones descritos son decisiones aceptadas, no borradores que deban aprobarse otra vez. La tabla no define patrones ni cantidades adicionales donde no se especificaron. Los patrones ausentes son información de producto pendiente; no bloquean implementar ítems y funciones independientes.

El balance operativo se toma de [[Fabricación]], [[Obtención]], [[Rasquetas]], [[Pinceles]] y [[Cubos de Pintura]]. Las recetas de mejora deben respetar conservación y capacidad antes de consumir entradas. En particular, mejorar una rasqueta conserva la proporción restante, no la repara, y mejorar un cubo no elimina el exceso de una pintura.

La creación de recetas y revisión de sus datos se puede elegir como frente independiente cuando estén disponibles sus entradas. Los recursos definitivos de herramientas dependen de sus funciones, no de completar todos los patrones de supervivencia. Véanse [[Plan de Fases]] y [[Decisiones Pendientes]].
