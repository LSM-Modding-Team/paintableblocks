# Colocación y rotación

## Colocación habitual

La colocación inicial sigue la lógica de una escalera: cuatro direcciones horizontales y posición normal o invertida.

- Sobre la cara superior de un bloque: posición normal.
- Bajo la cara inferior: posición invertida.
- Contra un lateral: la mitad inferior o superior del impacto determina la posición normal o invertida.
- La dirección horizontal depende de hacia dónde mira el jugador, con una relación constante como la de las escaleras.

El diseño completo se transforma conjuntamente: subcubos, pinturas y orientación de sus texturas. Un hueco superior puede quedar debajo al colocar el diseño invertido.

## Rotaciones adicionales

Las ocho orientaciones iniciales no permiten tumbar cualquier diseño sobre cualquier cara: su eje vertical sigue vertical, aunque pueda invertirse. La [[Llave de Giro]] añade giros de 90° en los tres ejes y permite alcanzar las **24 orientaciones** de un objeto, sin reflejarlo ni deformarlo.

El giro conserva el diseño y no consume pintura. Se rechaza si la nueva geometría colisiona con una entidad. La vista previa muestra el resultado antes de actuar.
