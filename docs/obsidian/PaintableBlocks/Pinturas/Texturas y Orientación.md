# Texturas y orientación

Cada cuadrado muestra el fragmento correspondiente a su zona de la textura completa. Los cuadrados mantienen continuidad; no repiten la textura entera en miniatura.

## Orientación compartida

El bloque tiene una colocación similar a la de una escalera. Todas sus pinturas utilizan una orientación compartida, determinada al colocarlo. No se rota la textura de cada cuadrado de forma independiente.

Las pinturas con extremos y laterales muestran el extremo en las caras perpendiculares al eje y el lateral en las paralelas. Los leños y las hifas muestran corteza en todas las caras, orientada según ese mismo eje.

Las superficies descubiertas por recortes siguen la misma regla y muestran el fragmento correspondiente a su posición dentro del bloque. Al colocar un diseño conservado, su geometría y pinturas se orientan conjuntamente. Dos pinturas de tronco dentro del mismo bloque no tienen ejes independientes.

## Pinturas con eje longitudinal del catálogo

Las siguientes familias reúnen 55 entradas del [[Catálogo de Pinturas]]:

| Familia | Variantes incluidas |
|---|---|
| Troncos, con y sin corteza | Acacia, abedul, cerezo, roble oscuro, jungla, mangle, roble, roble pálido y abeto |
| Leños, con y sin corteza | Las mismas nueve maderas |
| Tallos e hifas, con y sin corteza | Carmesí y distorsionados |
| Bambú | Bloque de bambú y bloque de bambú pelado |
| Basalto | Basalto y basalto pulido |
| Pilares | Cuarzo y purpur |
| Otros bloques con eje | Bloque de huesos y bala de heno |
| Ranaluces | Ocre, nacarada y verde |

## Colocación y giro

La orientación inicial sigue la colocación de escaleras. La [[Llave de Giro]] permite alcanzar 24 orientaciones mediante giros de 90° en tres ejes, sin reflejos. Giran juntos la geometría, las pinturas y sus ejes; no se habilitan orientaciones independientes por cuadrado.

## Estados visuales del catálogo

Cada pintura utiliza una apariencia estable, independiente del entorno:

| Bloque o familia | Apariencia |
|---|---|
| Lámparas y lámparas de cobre | Apagadas |
| Nexo de reaparición | Sin carga |
| Observador | Sin activar |
| Cobre | Oxidación de su entrada del catálogo |
| Césped y otros tintes ambientales | Color fijo predeterminado, igual en mundo e inventario |

Las texturas animadas conservan su animación visual, pero ninguna pintura emite luz ni aporta funciones. Los packs de recursos pueden cambiar las texturas sin alterar las reglas de proyección.

Las texturas propias son de 16×16 píxeles y los modelos de ítems son 3D. La superficie sin pintar utiliza la textura original `unpainted.png`; véase [[Modelos y Texturas]].

## Coherencia entre mundo e ítem

El diseño conservado debe mostrar la misma forma y pinturas en inventario que al colocarse, ajustadas a la orientación del bloque. La vista previa de giro y el resultado usan la misma transformación, sin reinterpretar la pintura por cada cuadrado.

Las texturas definitivas de herramientas se generan después de completar sus funciones; esa secuencia no altera las reglas de proyección del catálogo ni retrasa los frentes funcionales independientes. Véanse [[Arquitectura]] y [[Modelos y Texturas]].
