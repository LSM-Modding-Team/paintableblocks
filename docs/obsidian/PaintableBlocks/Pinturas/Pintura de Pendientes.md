# Pintura de pendientes

Ampliación aceptada, pendiente de implementación. Cada fragmento de superficie expuesta dentro de una celda es una zona pintable, aunque sea triangular. No confundir celda tridimensional con cada una de sus superficies.

## Textura y orientación

Proyección desde el eje dominante de la cara, conservando las coordenadas del bloque. Mantiene continuidad y evita repetir texturas en miniatura; se acepta cierto estiramiento visual sobre superficies inclinadas. Las texturas orientadas conservan su orientación local y rotan junto con la pieza, sin ejes independientes por fragmento.

Las superficies nuevas usan la textura original `unpainted.png`, que no es una pintura. Las pinturas siguen siendo decorativas. Véase [[Texturas y Orientación]].

## Coste

Consumo proporcional al área real de la superficie: **techo(256 × área del fragmento)**, con área expresada en caras completas de bloque. Redondear hacia arriba por fragmento; ningún fragmento visible cuesta cero. En caras cuadradas actuales se conserva 256/n² unidades.

Cobrar solo los fragmentos cuya identidad cambia. Repintar sustituye directamente la pintura anterior. La vista previa muestra el coste calculado con la misma regla que el servidor; falta de pintura impide toda la aplicación. Creativo conserva su regla de no consumir.

## Área y limpieza

Selección por dos esquinas dentro del **mismo bloque y plano**, admitiendo ahora planos inclinados. Pintar los fragmentos cuyos centros estén dentro del rectángulo seleccionado sobre ese plano. Mantener exclusión de huecos y de superficies de otro plano o profundidad, así como las reglas de cancelación de [[Por Área]].

El raspador limpia todas las superficies del mismo plano dentro del bloque apuntado, aunque estén separadas por huecos; nunca alcanza bloques vecinos. La esponja limpia todas las superficies expuestas, interiores e inclinadas.
