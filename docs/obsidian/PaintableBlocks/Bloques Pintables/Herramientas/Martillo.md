# Martillo

Retira únicamente el subcubo apuntado en 2×2, 4×4, 8×8 o 16×16. No recorta el 1×1.

El recorte es irreversible y no devuelve materiales ni pintura. La nueva superficie interior queda sin pintar, con `unpainted.png`. Al retirar el último subcubo, desaparece el bloque.

No tiene durabilidad ni niveles por material y se apila de una en una. La selección destaca el subcubo que se retirará antes de actuar. Sus ingredientes conocidos son hierro y palos; no se ha concretado el patrón.

Primero se implementan el ítem, operación, selección y guardado; después el modelo 3D y la textura definitiva de 16×16. Véanse [[Herramientas]], [[Recetas]] y [[Criterios de División]].
