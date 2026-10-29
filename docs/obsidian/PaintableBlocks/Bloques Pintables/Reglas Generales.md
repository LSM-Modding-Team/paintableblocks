# Reglas generales

Los tipos [[1x1]], [[2x2]], [[4x4]], [[8x8]] y [[16x16]] indican cuadrados por eje de cada lado. El volumen se divide en subcubos tridimensionales: 4×4 tiene 16 cuadrados por lado y 64 subcubos.

## Geometría y superficies

Se permiten piezas desconectadas dentro del mismo bloque. No caen ni desaparecen por quedar separadas. Solo hay colisión donde quedan subcubos: los huecos dejan pasar la selección y las entidades cuyo tamaño permita atravesarlos. Las piezas restantes ofrecen apoyo y colisión; no hay una caja completa invisible alrededor del diseño.

Cada superficie expuesta admite pintura independiente. Los recortes son irreversibles, no devuelven material ni pintura y dejan las nuevas superficies con la textura original sin pintar. El último subcubo retirado hace desaparecer el bloque. El tipo 1×1 no admite recortes, pero sí pintura, limpieza y recolección.

Los huecos no almacenan ni conducen agua dentro del bloque. No se simula agua por subcubo.

## Propiedades comunes

| Propiedad | Valor |
|---|---|
| Dureza | 1,5 |
| Resistencia a explosiones | 6 |
| Sonido | Piedra |
| Herramienta más rápida | Pico |
| Gravedad | Sin gravedad |

Las cinco subdivisiones tienen las mismas propiedades, incluso recortadas. Las pinturas no cambian dureza, resistencia o sonido ni aportan luz, funciones, señales de redstone o transparencia física. El pico mejora la velocidad, pero no es obligatorio para el drop normal, sujeto a [[Recolección y Conservación]].

Los pistones no pueden mover bloques pintables, estaciones ni cubos colocados.

## Colocación y diseño

La colocación habitual sigue la lógica de escaleras. La [[Colocación y Rotación]] y la [[Llave de Giro]] permiten orientar conjuntamente forma y pinturas, sin ejes independientes por pintura.

La apariencia original utiliza `unpainted.png`, no una pintura del catálogo. Véase [[Modelos y Texturas]].

## Desarrollo de estas reglas

Vista previa y acción real deben calcular los mismos resultados, validados por el servidor. El diseño persistente conserva subdivisión, forma y pinturas. La implementación se divide por dependencias y resultados observables, no por una lista rígida de fases; véanse [[Arquitectura]] y [[Plan de Fases]].

## Extensión a pendientes

[[Pendientes]] amplía las reglas de geometría: admite fragmentos inclinados incluso en la variante 1×1 mediante el formador. La prohibición de recortar 1×1 con martillo, mazo y cincel permanece. Su selección usa geometría real; su colisión usa cajas a precisión 1/16, en vez de afirmar colisión exacta de subcubos completos. Las propiedades comunes, ausencia de agua por celda y bloqueo de pistones siguen vigentes.
