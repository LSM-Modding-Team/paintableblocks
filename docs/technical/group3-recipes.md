# Recetas incorporadas en el grupo 3

Las cantidades/patrones que antes faltaban se concretan en esta entrega por autorización del usuario. Se mantiene el coste y rendimiento de producción ya acordado. Cada ranura ocupada aporta una unidad del ingrediente, aunque la pila tenga más.

## Bloques pintables

Patrón `EEE / ECE / EEE`: C es stone, deepslate, andesite, diorite, granite o tuff; las ocho E son iguales. Cobblestone, cobre, hierro, oro, diamante y netherite producen respectivamente 1, 4, 8, 16, 32 y 64 bloques nuevos 1×1. No acepta componentes personalizados ni convierte subdivisiones.

## Herramientas iniciales

`/` separa filas; los espacios son casillas vacías. I = lingote de hierro, S = palo, P = tablón, L = cuero, C = lingote de cobre.

| Resultado | Patrón / ingredientes |
|---|---|
| Martillo | `II· / IIS / ·S·`, donde · es espacio |
| Cincel | `I / S` |
| Raspador | `II / S·` |
| Tenazas | `I·I / ·S· / ·S·` |
| Mazo | `PPP / PSP / ·S·` |
| Esponja de limpieza | `·I· / IXI / ·I·`, X = esponja seca |
| Guantes | `L·L / LIL` |
| Llave de giro | `I·I / ·C· / ·I·` |
| Pincel de cobblestone/cobre/hierro/oro/diamante | Sin forma: 1 palo + 1 lana blanca + 1 material del nivel |
| Rasqueta de esos cinco niveles | Sin forma: 1 palo + 3 materiales del nivel |
| Cubo de pintura de cobre | `CCC / C·C / CCC`: 8 lingotes de cobre |

Se eligieron patrones distintos entre las herramientas y se evita reutilizar el patrón del pico para el martillo. Las herramientas siguen sin modelos ni texturas.

## Estaciones y máquinas

| Resultado | Receta sin forma |
|---|---|
| Banco | 1 mesa de crafteo + 4 hierro + 2 cobre + 2 stone |
| Cortadora | 1 banco vacío + 4 hierro + 2 cobre + 1 diamante + 1 redstone |
| Ensambladora | 1 cortadora vacía + 4 diamantes + 2 oro + 1 redstone + 1 hierro |
| Molino | 1 horno + 4 cobblestone + 2 hierro + 2 cobre |
| Prensa | 1 molino vacío + 4 hierro + 2 diamantes + 2 redstone |

Vacío significa sin contadores, entradas, salidas ni trabajo pendiente. Las mejoras retienen los componentes del ítem; no convierten inventarios almacenados en material gratuito ni eliminan su contenido. Hierro/cobre/oro/netherite se refieren a lingotes.

## Mejoras con contenido

- Pincel: cobblestone → cobre → hierro → oro → diamante mediante receta sin forma con el pincel anterior y un material del destino. Conserva identidad, unidades reales y componentes.
- Cubo: cobre → oro con cuatro lingotes de oro; oro → diamante con cuatro diamantes. Conserva todas las pinturas y componentes solo si caben en el destino. Las cuatro unidades materiales ocupan cuatro casillas de la receta.
- Rasqueta, pincel y cubo de diamante → netherite: herrería con plantilla de mejora de netherite e ingote. La rasqueta conserva la proporción de usos restantes, el pincel/cubo su contenido.
- La extracción con rasqueta de uno u ocho bloques permanece en el grupo 2; molino/prensa multiplican sin rasqueta ni combustible.

Las recetas de mejoras y fabricación manual que dependen de componentes son especiales, sin autorrelleno del libro. El resto se descubre mediante avances por sus materiales. La selección creativa de pintura sigue siendo clic derecho de inventario sobre el pincel, no una receta utilizable en supervivencia.
