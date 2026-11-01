# Pinturas de cristal

Diseño aceptado e implementado en código. Revisión por lectura y diff; sin compilación ni pruebas ejecutadas y sin verificación dentro de Minecraft.

Hay 18 materiales: cristal normal, los 16 colores y cristal tintado oscuro. Cada uno tiene acabado vanilla y conectable, representados por identidades independientes (338–373). No se añaden bloques: las pinturas se aplican a las superficies de los cinco tipos existentes, incluidas pendientes.

La extracción con rasqueta produce una pintura por bloque; el molino produce cuatro y la prensa dieciséis. Una pintura completa cambia entre acabados mediante fabricación sin forma, sin coste ni pérdida, conservando el tipo de cristal. Un ítem contiene 256 unidades y se aplican las reglas de consumo, conservación y limpieza existentes.

El acabado conectable debe eliminar bordes interiores de superficies coplanares contiguas del mismo tipo y acabado, también entre bloques y resoluciones. Conserva contornos de huecos, cambios de ángulo, materiales distintos y contacto con cristal vanilla. Las caras de contacto entre cristales conectables iguales se ocultan. Las superficies nuevas de un corte quedan sin pintar.

El cristal normal y de color deja pasar luz solo cuando todas las superficies expuestas tienen una de esas pinturas. Cualquier superficie opaca, sin pintar o de cristal tintado oscuro bloquea luz para toda la posición. El cristal tintado oscuro sigue siendo visualmente transparente. Las propiedades físicas siguen siendo las del bloque pintable.

La actualización de luz, conexiones, renderizado por ambas caras y modelos de objetos está integrada en código y requiere comprobación en juego. El acabado no cambia las propiedades físicas ni el coste.

## Decisiones de implementación

- El marco mide 1/16 de bloque sobre la superficie, con el sprite vanilla como fuente. Su área y la del interior se recortan en polígonos separados para evitar capas coplanares superpuestas. No se crean texturas propias ni se modifica `unpainted.png`.
- Las uniones comparan plano, normal, pintura y tramos de arista compartidos. Una coincidencia parcial elimina únicamente su tramo de marco; un vecino diagonal también puede continuar una pendiente coplanar.
- La transparencia visual no oculta la superficie opaca del bloque de detrás. En contactos entre cristales distintos se mantienen los frentes opuestos sin sus reversos duplicados; en superficies expuestas se dibujan ambas caras.
- La regla de luz se evalúa sobre las superficies geométricas del diseño, incluidas caras interiores de recortes y pendientes, sin depender de ocultaciones visuales por vecinos.
- Las cachés y las claves de modelo consideran el cristal vecino; las ediciones, cargas y retiradas invalidan las secciones circundantes sin cargar chunks. Los ítems usan la misma geometría sin vecinos del mundo.
- Protocolo 7 para cliente y servidor. No se implementan migraciones de mundos anteriores.

## Verificación pendiente de ejecución autorizada

Se prepararon contratos de geometría/luz y de extracción, producción y conservación en recetas. No se ejecutaron Java, Gradle, scripts de validación ni Minecraft. Quedan por comprobar tipos/APIs, mezcla y orden de transparencias, muestreo de UV con paquetes de recursos, actualización de iluminación, límites de chunk y rendimiento/multijugador.
