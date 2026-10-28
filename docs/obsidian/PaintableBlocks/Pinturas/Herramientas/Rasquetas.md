# Rasquetas

Se combinan mediante crafteo con bloques del [[Catálogo de Pinturas]] para obtener sus pinturas. Esta es la alternativa manual de obtención.

Un bloque produce un ítem de pintura, equivalente a **256 unidades**. Ocho bloques iguales alrededor de la rasqueta producen ocho ítems de pintura.

La rasqueta consume un punto de durabilidad por bloque procesado. Sus niveles son cobblestone, cobre, hierro, oro, diamante y netherite.

## Duración y mejoras

| Material | Bloques que puede procesar |
|---|---:|
| Cobblestone | 64 |
| Cobre | 128 |
| Hierro | 256 |
| Oro | 128 |
| Diamante | 1024 |
| Netherite | 4096 |

No admiten encantamientos ni reparación. Cada bloque procesado consume exactamente un uso; al agotarse, desaparecen.

La mejora de diamante a netherite utiliza herrería con plantilla y conserva la proporción de usos restantes, sin restaurar una herramienta casi agotada. Una herramienta con el 25 % de duración produce otra con el 25 %.

El raspador de limpieza es independiente de esta familia. Véanse [[Raspador]] y [[Recetas]].

## Desarrollo y acceso

Cada nivel se implementa primero como ítem funcional, con extracción, desgaste y conservación de usos al mejorar cuando corresponda. Después se generan su modelo 3D y textura definitiva de 16×16; una variante funcional no espera a las demás.

La producción respeta [[Catálogo de Pinturas]]: las pinturas de bloques inaccesibles en supervivencia son exclusivas de creativo, sin recetas alternativas. El tipo compartido de pintura conserva una identidad distinta por textura.

[[Modelos y Texturas]] · [[Criterios de División]]
