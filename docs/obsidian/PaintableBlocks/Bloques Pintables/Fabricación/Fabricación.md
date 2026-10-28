# Fabricación

Las alternativas producen mayores cantidades con los mismos materiales por lote:

**[[Fabricación Manual|Crafteo]] → [[Banco]] → [[Cortadora]] → [[Ensambladora]]**

Cada lote consume una piedra central y ocho exteriores iguales. Las piedras admitidas son exactamente `stone`, `deepslate`, `andesite`, `diorite`, `granite` y `tuff`. Cobblestone sirve como exterior del primer nivel.

| Exterior | Crafteo | Banco ×2 | Cortadora ×4 | Ensambladora ×8 |
|---|---:|---:|---:|---:|
| Cobblestone | 1 | 2 | 4 | 8 |
| Lingotes de cobre | 4 | 8 | 16 | 32 |
| Lingotes de hierro | 8 | 16 | 32 | 64 |
| Lingotes de oro | 16 | 32 | 64 | 128 |
| Diamantes | 32 | 64 | 128 | 256 |
| Lingotes de netherite | 64 | 128 | 256 | 512 |

El crafteo produce 1×1. Las estaciones fabrican directamente cualquiera de las cinco subdivisiones, sin cambiar coste ni rendimiento. Funcionan instantáneamente y sin combustible.

## Almacenes y transferencias

La capacidad de cada estación se comparte entre las cinco subdivisiones: un bloque ocupa una unidad. Solo se guardan en contadores bloques nuevos sin personalización; los ítems renombrados o con otros datos no deben perderlos al almacenarse.

La fabricación exige espacio para el lote completo antes de consumir materiales. Al retirar se entrega únicamente lo que admite el inventario; el resto permanece almacenado. Nunca se tiran sobrantes al suelo. La interfaz muestra cantidades reales superiores a 64; el límite de pila se aplica al retirar ítems.

Las estaciones permiten [[Conversión de Subdivisiones]] 1:1 sin coste adicional, solo para bloques completos y sin pintura. Al recoger una estación, un único ítem conserva todo su contenido y lo restaura al colocar, sin drops adicionales de ese contenido.

Las mejoras consumen la estación anterior vacía. Las familias de ingredientes están en [[Recetas]]; los patrones exactos de las estaciones no están definidos.

## Organización de la implementación

La lógica de costes y capacidad se comparte con la interfaz: el resultado mostrado debe coincidir con el que puede ejecutarse. Separar esa lógica permite desarrollar almacenamiento, conversión o una estación concreta sin esperar a todas las herramientas ni a sus recursos visuales.

Las estaciones y sus ítems tendrán los recursos acordados en [[Modelos y Texturas]]. Los límites de los lotes se revisan por datos; su comportamiento en menús e inventarios se verifica en ejecución solo cuando esté autorizada.
