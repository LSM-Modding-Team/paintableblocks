# Modelos y texturas

## Instrucción vigente

El usuario autoriza ahora crear todos los recursos de bloques y herramientas que faltaban, evitando el parpadeo por superficies superpuestas. Esta petición sustituye la prohibición temporal de modelos/texturas. Las funciones de los grupos 1–3 preceden a esta entrega visual.

Las texturas de juego son **16×16 píxeles** y los modelos de ítem son **3D**. El tamaño de textura no limita la resolución geométrica ni cambia las subdivisiones. El original `unpainted.png` se conserva exactamente, sin recrearlo; no es una pintura del catálogo.

## Estado individual

Modelo y textura de las siguientes variantes: **implementados**. Verificación en Minecraft: **pendiente** para todas; se revisaron por software y mediante validadores estáticos.

| Recurso | Modelo y material |
|---|---|
| Martillo | Cabeza de acero y mango de madera |
| Mazo | Cabeza y mango de madera con bandas |
| Cincel | Hoja y casquillos de acero, empuñadura de madera |
| Raspador | Hoja plana ancha y mango corto |
| Esponja de limpieza | Volumen escalonado y textura porosa |
| Tenazas | Dos brazos, articulación y mordazas abiertas |
| Guantes | Par de guantes de cuero con refuerzos metálicos |
| Llave de giro | Boca abierta, mango de cobre y anillo |
| Pincel de cobblestone | Cerdas claras y abrazadera de piedra |
| Pincel de cobre | Cerdas claras y abrazadera de cobre |
| Pincel de hierro | Cerdas claras y abrazadera de acero |
| Pincel de oro | Cerdas claras y abrazadera dorada |
| Pincel de diamante | Cerdas claras y abrazadera turquesa |
| Pincel de netherite | Cerdas claras y abrazadera oscura |
| Rasqueta de cobblestone | Hoja dentada de piedra |
| Rasqueta de cobre | Hoja dentada de cobre |
| Rasqueta de hierro | Hoja dentada de acero |
| Rasqueta de oro | Hoja dentada dorada |
| Rasqueta de diamante | Hoja dentada turquesa |
| Rasqueta de netherite | Hoja dentada oscura |
| Cubo de cobre | Recipiente cerrado, asa y marca de una identidad |
| Cubo de oro | Recipiente cerrado y cuatro marcas |
| Cubo de diamante | Recipiente cerrado y ocho marcas |
| Cubo de netherite | Recipiente cerrado, ocho marcas y distintivo dorado |
| Banco | Patas, estante y tablero de trabajo |
| Cortadora | Cuerpo, guías y hoja de corte |
| Ensambladora | Cuerpo con pórtico y cabezal |
| Molino | Base, tolva y manivela |
| Prensa | Base, columnas y plato |

Las familias reutilizan materiales y modelos padre sin perder la diferencia entre niveles. Las cantidades y pinturas reales del pincel/cubo se consultan en interfaz y tooltip; no se inventa contenido líquido estático.

## Superficies, mundo e inventario

Se genera una única superficie exterior por unión de sólidos, sin caras internas de contacto, planos de grosor cero ni adornos coplanares. Los nueve bloques de mundo comparten esa geometría con sus formas físicas; los huecos y asas no conservan el cubo invisible provisional.

Los bloques pintables conservan su diseño dinámico en mundo e inventario. Las texturas del catálogo se fragmentan y giran con el diseño; no se duplican en miniatura ni se sustituyen por los nuevos materiales. Se conservan las animaciones y capas decorativas intencionadas de [[Texturas y Orientación]].

El bote de pintura mantiene su cuerpo dinámico; se remalla la tapa y se suprimen las caras ocultas de contacto. La proyección de pinturas selecciona la normal exterior real para no superponer las caras de vuelta de modelos vanilla.

## Evidencia

Informe, decisiones, lámina técnica y validadores: [recursos visuales](../../../../technical/visual-assets.md). La lámina es un render de inspección por software, no una captura del juego. El modelo y la textura creados no acreditan comprobación de iluminación, oclusión, manos, cámara o recarga en Minecraft.

No compilar ni iniciar clientes/servidores sin petición expresa, según [[Agente]]. Los commits se publican online únicamente en `26.2`.
