# Pendientes

Familia aceptada, con código preparado y revisión estática; sin verificación en Minecraft. Una pendiente es una superficie realmente inclinada, no una escalera visual de minicubos. Se obtiene en creativo o transformando bloques pintables con el [[Formador de Pendientes]].

## Forma y dimensiones

Volumen rectangular cortado por un único plano inclinado, conservando uno de sus lados. Ascenso constante en uno de los cuatro sentidos horizontales; anchura perpendicular. Incluye versiones invertidas. No incluye curvas, pendientes diagonales en planta, esquinas interiores/exteriores ni pirámides.

Longitud, anchura y altura independientes de **1 a 16 bloques por operación**, tanto en supervivencia como inicialmente en creativo. Todas sus combinaciones de dimensiones y sentidos pertenecen a esta familia. El plano cruza los límites seleccionados sin escalones adicionales. La interfaz muestra medidas y relación de ascenso, por ejemplo, «sube 3 bloques en 8».

El límite corresponde a cada operación, no al tamaño total de una construcción conectada. Es posible ampliarla por etapas. Cada posición contiene su fragmento de pendiente: no existe un bloque gigante ni una estructura indivisible. Tras transformar, las piezas son independientes; romper una no recalcula las demás.

## Dimensiones fraccionarias aceptadas

La creación creativa admite dimensiones de 1/16 a 16 bloques por eje, con pasos elegibles hasta 1/16 y desplazamiento del inicio entre 0 y 15/16. Por ejemplo: 2,5 × 0,75 × 1,25 bloques. La precisión de ajuste es independiente de la subdivisión editable de cada fragmento.

Cada pieza guarda un prisma local acotado y su plano. Sus celdas editables se distribuyen dentro de ese prisma; las piezas de los extremos no se redondean a bloques enteros. La geometría visible, las caras pintables, selección y conservación siguen esos límites. La colisión conserva la aproximación existente de 1/16. Una extensión desplazada de 16 bloques puede tocar 17 posiciones.

Se mantienen el anclaje creativo junto a la cara inicial, la exigencia de aire sin fluidos y las comprobaciones de permisos/colisión. La transformación de construcciones existentes conserva sus reglas anteriores y no mueve ni estira material. Formato de diseño 3 y protocolo 8; sin migración de mundos obsoletos.

Código preparado y contratos escritos; sin compilación ni ejecución de pruebas o Minecraft.

## Cotas inicial/final y modo vertical

El perfil tiene dos cotas en pasos de hasta 1/16, medidas desde el origen del prisma, entre cero y su extensión dependiente. Son independientes del desplazamiento ΔY: con ΔY=0, Y=1 y cotas 0,5 → 1 se mantiene la base sólida desde el suelo; con ΔY=0,5 se desplaza todo el prisma.

Se admiten cotas ascendentes, descendentes e iguales (perfil plano). El modo horizontal corta en Y según X/Z. El modo vertical corta en Z según X o en X según Z y se extruye a lo largo de Y, generando una cuña lateral. Los cuatro sentidos y la inversión seleccionan orientación y lado sólido. En vertical las cotas representan distancias laterales, no alturas.

Ambos perfiles se ofrecen al transformar y al crear. La transformación solo intersecta el material existente: no rellena huecos; se mantiene la regla de un único plano por pieza. La creación continúa siendo exclusiva de creativo y conserva validaciones de espacio vacío, fluidos, permisos y colisiones.

La interfaz separa perfil y dimensiones en dos páginas. El servidor valida las cotas contra el prisma real y sincroniza el perfil para vista previa y confirmación. Protocolo 9, persistencia 3 sin migraciones. No se ha compilado ni ejecutado el juego o los contratos añadidos.

## Combinación con una zona completa

Una misma pieza puede conservar una parte completa y otra inclinada. La página Combinación del formador permite activar esta función, ajustar la longitud completa en pasos de hasta 1/16 y elegir si ocupa el inicio o el final del sentido seleccionado. Por ejemplo: longitud total 1, longitud completa 0,5 produce media pieza completa y media inclinada.

La pendiente llega hasta el borde completo en la unión, sin escalón ni cara interna. La cota libre se controla con Inicio cuando la parte completa está al final, o con Fin cuando está al inicio. La otra cota queda determinada por el borde del prisma; en el lado sólido invertido se refleja el perfil para mantener completa la zona elegida.

Cero longitud completa deja toda la extensión inclinada; longitud completa igual al total deja el prisma completo. Se admiten los modos horizontal y vertical, desplazamientos fraccionarios y 24 rotaciones. La representación sigue siendo el prisma intersectado por un plano: el tramo que supera el borde queda completo. No hacen falta múltiples piezas ni un formato nuevo.

La transformación solo retira material de diseños compatibles; no reconstruye recortes ni reemplaza otro plano ya existente. Crear sigue siendo exclusivo de creativo. Colisión, selección, pintura, cristal y conservación usan la geometría común.

### Compatibilidad de mundos

Se mantiene el formato de escritura 3 y se admiten diseños guardados en versiones 1, 2 y 3. Las pendientes anteriores sin límites explícitos conservan el prisma completo. No cambian identificadores ni pinturas. La compatibilidad es al actualizar; no implica poder abrir diseños nuevos con versiones antiguas del mod. Cliente y servidor requieren protocolo 10.

Contratos preparados para lectura antigua y geometría combinada; sin compilación ni pruebas en ejecución.

## Ambos extremos completos y nueva interfaz

El modo Ambos extremos conserva dos zonas completas y limita el corte a la región central. Las longitudes de ambos lados son independientes y admiten pasos hasta 1/16. Su suma no puede superar la longitud total; si la iguala no se recorta material. Se aplican cotas inicial/final al tramo central, por lo que pueden aparecer paredes en las uniones con las partes completas; también son superficies pintables y seleccionables.

Se usa cuadrícula mundial 16×16 para alinear exactamente las uniones. La creación creativa la selecciona automáticamente. La transformación requiere piezas 16×16 alineadas, sin convertir ni estirar otros diseños personalizados; las piezas incompatibles se rechazan durante la preparación. Los modos anteriores conservan sus resoluciones.

Todos los controles están en una lista desplazable, sin páginas, junto a una previsualización integrada de sección. La sección muestra geometría del perfil y ocupación preexistente, no una cámara ni texturas del mundo. La confirmación sigue condicionada a la revisión del servidor. Las acciones permanecen visibles abajo.

Persistencia 4 con lectura de 1–3, ventanas de corte antiguas ausentes equivalen al plano original. Protocolo 11. Compatibilidad al actualizar, no al volver a una versión anterior. Contratos escritos sin ejecutar: no se acredita funcionamiento en Minecraft.

## Corrección de continuidad y escena 3D

El plano se normaliza dividiendo todos sus coeficientes, incluido el término de posición. La misma pendiente global debe atravesar los límites de bloque sin mesetas ni saltos. El paso de ajuste no escalona la superficie visible.

La previsualización integrada utiliza los modelos 3D de los diseños resultantes, con pinturas y recortes, contactos ocultos y profundidad compartida. La rueda sobre el panel ajusta el zoom; sobre los controles desplaza la lista. La preparación es incremental y puede mostrar un resultado parcial con aviso por carga, incompatibilidad o presupuesto visual. La operación autorizada no se recorta por ese presupuesto.

No se envía al chat el aviso de preparación en cada cambio. Se mantienen estados de interfaz y feedback de validación. Protocolo 12; formatos persistentes 1–4 legibles. Se conserva la forma almacenada, sin reconstruir automáticamente piezas antiguas mal generadas.

Código y contratos preparados; sin compilación ni pruebas o Minecraft ejecutados.

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


## Estado y evidencia

Implementación y decisiones: [Pendientes y volúmenes](../../../technical/slopes-and-volumes.md). No se ha compilado ni ejecutado Java/Minecraft; las reglas no equivalen a comportamiento probado.
