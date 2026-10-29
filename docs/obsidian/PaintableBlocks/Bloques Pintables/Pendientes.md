# Pendientes

Familia aceptada, **pendiente de implementación**. Una pendiente es una superficie realmente inclinada, no una escalera visual de minicubos. Se obtiene en creativo o transformando bloques pintables con el [[Formador de Pendientes]].

## Forma y dimensiones

Volumen rectangular cortado por un único plano inclinado, conservando uno de sus lados. Ascenso constante en uno de los cuatro sentidos horizontales; anchura perpendicular. Incluye versiones invertidas. No incluye curvas, pendientes diagonales en planta, esquinas interiores/exteriores ni pirámides.

Longitud, anchura y altura independientes de **1 a 16 bloques por operación**, tanto en supervivencia como inicialmente en creativo. Todas sus combinaciones de dimensiones y sentidos pertenecen a esta familia. El plano cruza los límites seleccionados sin escalones adicionales. La interfaz muestra medidas y relación de ascenso, por ejemplo, «sube 3 bloques en 8».

El límite corresponde a cada operación, no al tamaño total de una construcción conectada. Es posible ampliarla por etapas. Cada posición contiene su fragmento de pendiente: no existe un bloque gigante ni una estructura indivisible. Tras transformar, las piezas son independientes; romper una no recalcula las demás.

## Subdivisión y transformación

Cinco variantes: 1×1, 2×2, 4×4, 8×8 y 16×16. El plano puede generar fragmentos de celda con caras triangulares o inclinadas. Se permiten subdivisiones mezcladas bajo el mismo plano global.

La transformación intersecta la geometría existente con el lado conservado: solo retira volumen, nunca añade material ni rellena huecos. Mantiene recortes anteriores. Los bloques ajenos quedan intactos y señalados en la previsualización. Se conservan las pinturas de superficies supervivientes; las nuevas aparecen con `unpainted.png`.

Una pieza ya inclinada admite repetir exactamente su plano, pero rechaza otro distinto. No se introducen múltiples cortes oblicuos. Si no cambia nada, no hay desgaste; sin volumen restante, la pieza desaparece.

## Compatibilidad de herramientas

| Herramienta | Regla sobre pendientes |
|---|---|
| Martillo | Retira la celda apuntada y su fragmento inclinado; también admite [[Recorte Volumétrico]] |
| Mazo | Primera celda ocupada en cada columna desde la dirección elegida |
| Cincel | Celdas de la capa seleccionada, incluidos sus fragmentos; no profundiza por huecos |
| Pincel | Superficies expuestas, también inclinadas; véase [[Pintura de Pendientes]] |
| Raspador | Todas las superficies del mismo plano dentro del bloque, incluso separadas por huecos |
| Esponja | Todas las pinturas, manteniendo geometría |
| Tenazas | Conservan forma, plano y subdivisión; eliminan pinturas |
| Guantes / copia creativa | Conservan forma, plano, subdivisión y pinturas |
| Llave de giro | Rota conjuntamente geometría, plano y pinturas |

La formación de pendientes permite una variante 1×1 inclinada. No elimina la restricción del martillo, mazo y cincel: el 1×1 no admite recorte por celdas.

## Colisión y presentación

Geometría visible y selección de apuntado usan la superficie inclinada real. La colisión se aproxima mediante cajas con precisión de **1/16 de bloque**, independiente de la subdivisión. Verificar en juego suavidad de marcha, uniones y entidades; no se afirma colisión inclinada nativa exacta.

Ocultar caras internas entre piezas contiguas y compartir límites exactos para evitar grietas y parpadeo. Solo caras completas, planas y alineadas con los ejes sirven de apoyo cuando un elemento lo exija. Iluminación según geometría; pinturas sin emisión de luz ni propiedades físicas. Dureza, resistencia y sonidos comunes del bloque pintable.

## Orientación y conservación

Se conserva la colocación similar a escaleras y el acceso a 24 orientaciones. Geometría, plano y pinturas giran juntos. La pendiente cuenta como bloque recortado: no admite conversión de subdivisión en estaciones y su recuperación exige las herramientas de conservación correspondientes.

Guantes y Ctrl + clic central copian **una pieza individual**, no toda la rampa. El ítem representa su diseño y lo restaura al colocarse. Véanse [[Recolección y Conservación]] y [[Colocación y Rotación]].
