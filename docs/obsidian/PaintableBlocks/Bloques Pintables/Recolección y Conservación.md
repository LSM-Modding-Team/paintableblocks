# Recolección y conservación

En la rotura convencional, un bloque pintado y sin recortes suelta el bloque sin pintura. Si tiene recortes, con o sin pintura, no suelta nada salvo que se use la herramienta de conservación correspondiente.

- [[Tenazas]]: conservan subdivisión y recortes, eliminando todas las pinturas.
- [[Guantes]]: conservan subdivisión, recortes y todas las pinturas.

Mazo, cincel, raspador, esponja de limpieza, azadas y tijeras vanilla, y armas en general no dan ningún ítem al romper el bloque. El hacha, en todas sus calidades, queda exceptuada de esta lista de armas.

## Copia en creativo

**Ctrl + clic central** copia el diseño completo: subdivisión, forma y pinturas, incluidas las superficies interiores expuestas. El ítem muestra la geometría y las pinturas en el inventario para diferenciar los diseños.

Al colocarlo, reproduce el diseño conservado, orientando forma y pinturas conjuntamente según la colocación del bloque. Los guantes son la alternativa de supervivencia para recoger y conservar ese mismo diseño.

## Guardado y explosiones

El diseño guarda subdivisión, ocupación y pinturas para conservarlos al descargar el mundo y recoger o copiar el bloque. El ítem conservado muestra su forma y pinturas y las restaura al colocarse, con la orientación conjunta de [[Colocación y Rotación]].

Las explosiones siguen la política de rotura normal, sujeta a la supervivencia de drops vanilla: un bloque completo puede soltar uno sin pintura; uno recortado no suelta nada. Nunca se recuperan fragmentos ni pintura. Las excepciones de tenazas y guantes requieren que un jugador use esas herramientas.

Si una estación o un cubo sobrevive a una explosión como drop, su contenido viaja en un único ítem y no se suelta también por separado.

## Reglas de implementación

El ítem conservado y el bloque colocado representan el mismo diseño; el guardado debe conservarlo al descargar el mundo, no solo durante una interacción. La selección previa de tenazas o guantes indica qué forma se recoge y si se descartan o conservan pinturas.

La conservación de contenido en estaciones, máquinas y cubos nunca genera además un segundo drop del mismo contenido. Los cambios los valida el servidor. Las funciones y su persistencia preceden a los recursos definitivos de las herramientas de recogida, según [[Criterios de División]].
