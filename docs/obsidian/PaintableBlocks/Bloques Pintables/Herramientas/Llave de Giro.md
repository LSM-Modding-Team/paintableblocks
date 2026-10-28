# Llave de giro

Herramienta sin durabilidad para orientar bloques pintables en los tres ejes.

- Clic derecho: giro de 90° alrededor del eje perpendicular a la cara apuntada.
- Mayús + clic derecho: giro en sentido contrario.
- La vista previa muestra el resultado antes de actuar.

Forma, pinturas y orientación giran conjuntamente; no consume pintura ni altera el diseño conservado. Apuntar a caras de distintos ejes permite alcanzar 24 orientaciones, sin reflejos. Si el resultado colisiona con una entidad, se rechaza.

Sus ingredientes acordados son hierro y cobre; el patrón exacto no está definido. Véanse [[Colocación y Rotación]] y [[Recetas]].

## Desarrollo del recurso

Primero se implementan el ítem, los giros, la validación de colisión, el guardado y la vista previa. Solo entonces se generan su modelo 3D y textura definitiva de 16×16. Véanse [[Modelos y Texturas]] y [[Criterios de División]].
