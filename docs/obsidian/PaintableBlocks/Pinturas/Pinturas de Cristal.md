# Pinturas de cristal

Diseño aceptado; implementación aún incompleta y sin verificar.

Se prevén 18 materiales: cristal normal, los 16 colores y cristal tintado oscuro. Cada uno tendrá acabado vanilla y conectable, representados por identidades independientes. No se añaden bloques: las pinturas se aplican a las superficies de los cinco tipos existentes, incluidas pendientes.

La extracción con rasqueta produce una pintura por bloque; el molino produce cuatro y la prensa dieciséis. Una pintura completa cambia entre acabados mediante fabricación sin forma, sin coste ni pérdida, conservando el tipo de cristal. Un ítem contiene 256 unidades y se aplican las reglas de consumo, conservación y limpieza existentes.

El acabado conectable debe eliminar bordes interiores de superficies coplanares contiguas del mismo tipo y acabado, también entre bloques y resoluciones. Conserva contornos de huecos, cambios de ángulo, materiales distintos y contacto con cristal vanilla. Las caras de contacto entre cristales conectables iguales se ocultan. Las superficies nuevas de un corte quedan sin pintar.

El cristal normal y de color deja pasar luz solo cuando todas las superficies expuestas tienen una de esas pinturas. Cualquier superficie opaca, sin pintar o de cristal tintado oscuro bloquea luz para toda la posición. El cristal tintado oscuro sigue siendo visualmente transparente. Las propiedades físicas siguen siendo las del bloque pintable.

La actualización de luz, conexiones, renderizado por ambas caras y modelos de objetos requiere implementación y comprobación. El acabado no cambia las propiedades físicas ni el coste.
