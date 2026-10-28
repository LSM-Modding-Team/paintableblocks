# Catálogo de pinturas

El catálogo definitivo contiene **337 pinturas**, recogidas en [[vanilla-paints-26.2-id.csv]]. Cada entrada incluye `paint_id`, `block_id`, `name_es` y `name_en`.

Las 337 pinturas utilizan un único tipo de ítem con identidad guardada en sus datos. Cada identidad conserva nombre y apariencia propios. Todas son únicamente decorativas: reproducen la apariencia del bloque de origen sin transmitir propiedades como emisión de luz, señales de redstone o funciones de interacción.

En los bloques con distintas texturas por cara, se aplica la textura correspondiente a la orientación. Las reglas para pinturas con eje longitudinal están en [[Texturas y Orientación]].

## Identidad y acceso

Las pinturas con igual identidad pueden apilarse; identidades distintas no se mezclan. El bote 3D muestra la pintura y cada variante está disponible en el inventario creativo. Pinceles, cubos y máquinas manejan esas identidades con reglas comunes.

Las pinturas cuyos bloques no se pueden conseguir en supervivencia son exclusivas de creativo, sin recetas alternativas para obtenerlas. No se destruyen diseños existentes al cambiar de modo: la restricción afecta a obtener y aplicar esas pinturas. Véase [[Pintura en Creativo]].
