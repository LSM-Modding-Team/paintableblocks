# Herramientas

Las herramientas de recorte, limpieza y conservación no tienen durabilidad ni niveles por material y se apilan de una en una. Las [[Rasquetas]] de obtención de pintura son una familia distinta y sí se desgastan.

Martillo, mazo y cincel funcionan en 2×2, 4×4, 8×8 y 16×16; el tamaño y número de subcubos cambian, sus reglas no. El 1×1 admite limpieza y recolección, pero no recortes.

Antes de actuar se destacan exactamente los subcubos o superficies afectados. Tenazas y guantes destacan la forma completa e indican si conservarán las pinturas. La [[Llave de Giro]] muestra la orientación resultante.

- [[Cincel]]
- [[Esponja de Limpieza]]
- [[Guantes]]
- [[Martillo]]
- [[Mazo]]
- [[Raspador]]
- [[Tenazas]]
- [[Llave de Giro]]

## Desarrollo por herramienta

Primero se implementan el ítem y todas sus funciones, con selección, persistencia y mensajes necesarios. Después se crean el modelo 3D y la textura definitiva de 16×16. Durante la función se usa una representación provisional identificable. La dependencia es individual y se extiende también a [[Rasquetas]] y [[Pinceles]]; no exige terminar todas las herramientas antes de texturizar una ya funcional.

[[Plan de Fases]] · [[Modelos y Texturas]]
