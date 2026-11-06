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

## Editor ampliado y perfiles independientes

La opción Unión con escalón usa cotas libres en la inclinación central. Una parte completa al inicio puede unirse por pared vertical a una pendiente ascendente baja, como en el dibujo aceptado. Unión continua conserva el comportamiento que llega al borde del tramo completo. La modalidad libre admite uno o ambos extremos completos en cuadrícula 16×16.

Los extremos se controlan independientemente; mover la inclinación reparte ambas longitudes completas sin cambiar su suma ni las cotas. Cambiar la longitud inclinada conserva las cotas. Pulsar un número abre entrada decimal o fraccionaria, por ejemplo 0,5, 2.75 o 1/8: solo valores exactos de la cuadrícula 1/16 e intervalos válidos.

La cámara se gira arrastrando dentro del panel; la rueda ajusta zoom. Hay vistas frontal, lateral, superior e isométrica, medidas de tramos/cotas y modos Antes, Después y Material retirado. Este último representa volumen retirado muestreado a 1/16 en rojo. Copiar/Pegar reutiliza ajustes durante la sesión del cliente, sin cambiar la selección de transformación.

Los diagnósticos muestran coordenadas y distinguen tipos de impedimento; las piezas afectadas se resaltan rojas. Los conflictos individuales no ocultan todas las piezas posteriores de la previsualización. La confirmación siempre depende de la validación del servidor.

### Deshacer

El editor y Shift + usar al aire permiten deshacer la última operación registrada del formador, incluidas las partes ya realizadas de una operación interrumpida. Se restaura el estado y datos previos de las piezas sin drops ni devolución de desgaste. Se exige sostener el formador, estar en la misma dimensión, chunks cargados, permisos/protecciones válidos y ausencia de entidades en el volumen que se restaura. Si algún bloque cambió después, se rechaza el deshacer completo.

El historial es temporal, acotado a una operación por jugador, 8192 posiciones y 8 388 608 caracteres serializados. Superar el presupuesto descarta el historial, no recorta la construcción. No hay rehacer y se pierde al reiniciar. No se revierte el inventario del jugador.

Persistencia de diseños 4 con lectura 1–3. Protocolo 13. Contratos preparados sin compilar ni ejecutar: pendientes pruebas reales de restauración, permisos, cámara e interfaces.

## Interacción directa en el mundo

El flujo principal no abre pantallas. Se apuntan dos puntos en cuadrícula mundial de 1/16, independiente de la subdivisión lógica del bloque. Definen a la vez volumen, inicio y final del perfil; se conservan literalmente y determinan el sentido según el desplazamiento horizontal dominante (empate: X). Otro par de clics reemplaza el perfil dentro de la selección; Shift + clic en dos puntos fija sus uniones. Ctrl + clic o Enter confirma únicamente después de revisión del servidor. Esc o Retroceso cancela. Mientras falta el segundo marcador no se puede confirmar.

F6 alterna plano horizontal/vertical, F7 invierte el lado sólido, F8 alterna creación creativa/transformación y F9 cambia la subdivisión de las nuevas piezas. No hay ajuste manual de cotas con flechas: se calculan desde los puntos. Los puntos fuera del volumen y los perfiles sin separación horizontal se rechazan, sin moverlos silenciosamente. F4 intercambia inicio/final conservando la forma; F5 gira perfil y volumen 90° alrededor del centro ajustado a un nodo de 1/16, sin deformarlos. La flecha mundial muestra el sentido resultante. F6 reinterpreta los mismos puntos en el otro plano. Crear conserva el anclaje adyacente a la cara inicial y la exigencia de aire sin fluido; transformar solo retira material.

Se muestra cuadrícula, celda apuntada y marcadores en el mundo, resultado verde, referencia anterior roja e incompatibilidades rojas. La preparación cubre la selección progresivamente sin cargar chunks. Presupuestos de dibujo pueden simplificar el detalle con aviso, sin reducir la operación real.

### Subdivisión conservada

Un bloque 1×1, 2×2, 4×4 u 8×8 admite cortes y uniones a 1/16 sin conversión ni rechazo por resolución. Una cara lógica puede representar varios polígonos, incluidos los de las paredes de unión. La pintura conserva su identidad por cara lógica y su consumo suma las áreas de los fragmentos. No se añaden subceldas editables por usar el formador.

La restricción de un plano existente distinto sigue siendo una incompatibilidad geométrica, no una exigencia de cuadrícula 16×16. Los recortes previos se conservan y no se rellenan al transformar.

### Franja lateral independiente

F10 inicia la selección de dos límites laterales mediante clics, a precisión de 1/16. Su orden no importa; deben estar dentro del volumen y separados. Solo se recorta entre ellos: el resto conserva el material original completo y pintable. F11 cancela la selección lateral o restablece ancho completo. Los puntos del perfil siguen definiendo cotas y recorrido, sin alterar el ancho lateral.

En modo horizontal la franja delimita el ancho transversal X/Z; en modo vertical delimita la extrusión Y. Se combina con ambos extremos completos. El giro de 90° transporta también la franja; invertir sentido conserva su posición. Al cambiar el eje del perfil o alternar horizontal/vertical se restablece el ancho completo, porque cambia el eje transversal; se marca de nuevo con F10. Fuera de la franja no se añade material previamente ausente al transformar.

Caras interiores de unión, pintura, colisión a 1/16 y ray tracing consideran ambas ventanas de corte, sin convertir subdivisiones. La flecha se centra sobre la franja. Las versiones previas sin ventana lateral conservan su forma.

Formato 5 con lectura 1–4 y protocolo 16. Las notas anteriores que exigían cuadrícula 16×16 para las uniones quedan sustituidas por esta ampliación. Código y contratos preparados, sin compilación ni comprobación en juego.

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

## Formador de escaleras

Herramienta independiente, en la pestaña de herramientas, con durabilidad 4096 y receta de tres lingotes de hierro, un palo y un diamante. Comparte selección mundial por extremos, giro, inversión, lado sólido, modo horizontal/vertical, creación creativa, uniones completas, franja lateral, confirmación y deshacer.

F9/RePág aumenta y AvPág reduce el número máximo de peldaños, inicialmente cuatro, entre 1 y 256. El número efectivo no supera longitud ni desnivel disponibles en unidades de 1/16. Se distribuye el recorrido mediante división entera y se redondean las alturas a 1/16, repartiendo el resto; las huellas se anclan al sentido positivo del eje mundial para que intercambiar inicio/final conserve exactamente la forma. Los extremos definen el rango de alturas; los saltos ocurren sobre límites de huella. Un perfil plano genera una plataforma. No hay un modo separado de altura/profundidad: se calculan desde extremos y número de peldaños para mantener un único flujo de edición.

Los peldaños son ocupación axial normal, no planos oblicuos aproximados. Solo se retira material al transformar. Las piezas modificadas pasan a 16×16 mediante sustitución protegida del tipo de bloque, conservando pinturas de caras que existían; las caras nuevas quedan sin pintar. Si no se retira material, no hay conversión ni desgaste. El deshacer conserva tipo de bloque y datos anteriores. Los diseños con cortes oblicuos previos se rechazan para evitar rellenarlos o deformarlos al rasterizar.

Recursos: modelo 3D de cabeza escalonada y mango, caras exteriores sin duplicados y materiales propios existentes de 16×16; no se modifica `unpainted.png`. Receta y desbloqueo incluidos.

Formato persistente 6, lectura 1–5 sin cambios: las escaleras son diseños ordinarios. Protocolo 19 por tipo de herramienta y número de peldaños en la sesión. Solo revisión estática, sin compilación ni ejecución.
