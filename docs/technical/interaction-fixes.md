# Correcciones de interacción del bloque pintable

Minecraft 26.2 / NeoForge 26.2.0.88. Esta entrega trata el fallo visual confirmado por el usuario y los 16 puntos de la revisión anterior. Se distingue corrección del código de verificación en ejecución: no se compiló ni inició Minecraft.

## Correspondencia con la revisión

| Caso | Corrección incorporada |
|---|---|
| Visual: solo se ve el primer cuadrado/subcubo | Tras cargar o aceptar datos en el cliente, refrescar ModelData y solicitar reconstrucción de la sección mediante `setBlocksDirty` y `sendBlockUpdated`, aunque no cambie `CUT` |
| 1. Área invalidada que termina pintando individualmente | Consumir la confirmación inválida sin aplicar; comprobar el identificador de selección incluso si el servidor ya la canceló |
| 2. Clics ajenos bloqueados | Interceptar únicamente impactos contra bloques pintables/cubos; retirar la cancelación global de `RightClickBlock`. Cofres, puertas, botones y entidades mantienen su flujo nativo |
| 3. Mano secundaria | Transportar la mano y validar/consumir su objeto; cliente y servidor usan la misma prioridad de manos sin ejecutar dos veces |
| 4. Chunk descargado detrás del objetivo | Recorrido DDA por chunks que trunca el rayo en el primer chunk descargado; permite impactos anteriores sin cargar terreno |
| 5. Contorno de cuadrados que no cambian | Construir el contorno de pintura con las caras del plan que realmente cambian; coste y superficies afectadas coinciden |
| 6. Coste anterior tras una esquina inválida | Invalidar plan/caché/aviso al cambiar de objetivo; mostrar aviso de plano inválido, sin reutilizar el coste anterior |
| 7. Conservación anunciada para datos inválidos | Comprobar validez antes del mensaje; avisar también al intentar copiar datos inválidos. Mantener el rechazo de drops recuperables |
| 8. Vecinos que no revisan sus apoyos | Ejecutar actualizaciones directas e indirectas de formas al cambiar ocupación/orientación, aunque el BlockState siga igual |
| 9. Orientación instalada después de colocar | Contexto de colocación delimitado con `try`/`close`; constructor, carga y componentes ven el diseño final. Se conserva el ciclo nativo de BlockItem |
| 10. Selección vinculada a la instancia ItemStack | Identificar mano, ranura, tipo e identidad de pintura; una instancia nueva por sincronización no cancela por sí sola |
| 11. Confirmaciones/cancelaciones antiguas | Identificadores de petición y selección; respuesta vinculada a petición. Comprobar dimensión, identidad y revisión del bloque; una cancelación antigua no afecta a una selección nueva |
| 12. Acción aplicada a otro objetivo/herramienta | Capturar bloque, impacto, cara, mano, ranura, tipo, pintura e identidad/revisión; volver a trazar en servidor y rechazar discrepancias. Nunca aceptar del cliente el resultado |
| 13. Pintura/giro registrados como rotura | Evento cancelable `PaintableBlockEditEvent` para ediciones; `BreakBlockEvent` adicional únicamente cuando se retiran subcubos. Conservar comprobaciones nativas de interacción |
| 14. Fila de cubo cambia de pintura | Botón vinculado al ID de pintura; retirada vinculada a la pintura seleccionada esperada. Paquete de menú con enteros explícitos y validación de contenedor/jugador/alcance |
| 15. Shift + clic predice mover la pila | Cliente y servidor calculan depósito con la misma función; nunca predecir un traslado a la barra rápida para pintura. La autoridad final sigue en servidor |
| 16. Coste de reconstrucción y bloqueo global de mallas | Caché de exposición independiente de pinturas/orientación local, proyecciones por superficie y cachés acotadas; generación fuera de los monitores compartidos, deduplicada por diseño. Colisión calcula ocupación sin transformar pinturas |

## Datos y compatibilidad

Cada bloque colocado tiene una identidad UUID y una revisión de edición. Se guardan/sincronizan junto al diseño para rechazar clics sobre una instancia reemplazada o un estado anterior. No forman parte del diseño conservado: se excluyen de los datos del ítem y se ignoran como identidad de destino durante una colocación. Un diseño antiguo sin estos campos recibe una identidad nueva y revisión inicial cero; no cambia el codec v1 del diseño.

El protocolo de red pasa de `2` a `3`. Cliente y servidor deben actualizarse juntos. La confirmación se envía después de procesar cada petición y la selección se invalida también desde el tick del servidor. El cliente descarta respuestas de peticiones anteriores y no mantiene referencias a instancias de ItemStack.

Las integraciones pueden cancelar `PaintableBlockEditEvent` y consultar jugador, mano y plan inmutable. Los eventos nativos de uso siguen comprobándose; la rotura convencional y las excepciones de guantes/tenazas mantienen su política. Después de los eventos se vuelven a comprobar estado, herramienta, modo, menú y unidades; el consumo se aplica al objeto actualmente sostenido, aunque un listener haya sustituido la instancia por otra equivalente.

La colocación prepara forma/orientación antes de que el ciclo nativo observe la entidad. El contexto se restaura al anidar colocaciones y se elimina incluso si falla la operación. No se modifica el comportamiento de colocación de otros bloques ni se crean modelos/texturas de herramientas.

## Cachés y límites

Las superficies locales se reutilizan por resolución/ocupación: máximo 64 geometrías y 65 536 superficies. Las mallas completas mantienen el máximo de 64 diseños/65 536 quads; las proyecciones añaden un máximo de 8192 entradas/16 384 quads por carga de recursos. Los cálculos se realizan fuera de los monitores de las cachés y las generaciones simultáneas del mismo diseño comparten resultado. Otra carga de recursos obtiene otras cachés.

Estas medidas reducen recomputación y contención; no constituyen una medición de FPS ni garantizan ausencia de tirones con cualquier cantidad de bloques o paquete de recursos.

## Comprobaciones realizadas

- Validadores estáticos de grupos 1 y 2: catálogo, recursos, traducciones, PNG original, ausencia de recursos visuales de herramientas y límites con máquinas pendientes.
- Parser de sintaxis Java y contraste de importaciones explícitas con el cliente oficial 26.2 y fuentes NeoForge 26.2.0.88. Lectura de firmas de métodos nuevos, sin ejecutar/descompilar Java.
- `InteractionRegressionChecks.java` preparado para codec de los paquetes —incluidos enteros/longs, manos y UUID—, restauración de contexto tras excepciones, reutilización de geometría, superficies sin cambios y conservación de pintura interior en las 24 orientaciones con las ocho colocaciones de escalera. **No ejecutado**.

## Escenarios de aceptación en juego — pendientes de ejecución

1. Pintar repetidamente cuadrados diferentes, repintar y limpiar. Recortar varios subcubos sucesivos sin salir del mundo: cada cambio debe verse inmediatamente y coincidir con la colisión.
2. Comparar martillo/mazo/cincel sobre una cara con huecos a distintas profundidades y sobre las cinco resoluciones. El 1×1 no se recorta y el último subcubo elimina el bloque.
3. Iniciar área, cambiarla desde otro jugador y confirmar con latencia: no debe pintarse un cuadrado individual ni consumirse pintura. Probar respuestas antiguas, cancelaciones, cambio de dimensión, mano y ranura.
4. Sostener herramientas al abrir cofres, usar puertas/botones, interactuar con entidades y utilizar objetos de la mano secundaria. Repetir edición con herramientas en ambas manos.
5. Editar un objetivo cargado situado antes de un chunk descargado. El rayo no debe cargar terreno ni rechazar el objetivo anterior.
6. Retirar apoyos sucesivos de antorchas, escaleras de mano y otros bloques dependientes; girar una forma que deje de ofrecer apoyo. Comprobar también el rechazo de giro por entidades.
7. Crear diseño con recortes y pinturas interiores, recoger con guantes/tenazas y copiar con Ctrl + clic central. Colocar las 24 orientaciones con colocación normal/invertida, guardar, descargar y volver a cargar. Guantes conservan pinturas; tenazas las quitan.
8. Modificar y agotar paletas desde dos jugadores mientras otro selecciona o retira pintura. No seleccionar otra identidad por desplazarse una fila. Probar inventario lleno, fracciones y Shift + clic con cantidades parcialmente admitidas.
9. Comprobar datos inválidos: aviso correcto y ausencia de ítems recuperables; verificar bloques/ítems anteriores sin los metadatos nuevos.
10. Probar cancelación de los eventos de edición/uso/recorte y listeners que sustituyan objetos; no consumir ni crear pintura indebidamente. Mostrar numerosos diseños 16×16 y medir rendimiento con/sin recarga de recursos.

Sin Gradle, compilación, builds, Java, CI ni clientes/servidores iniciados. La publicación se realiza directamente online en `26.2`, sin commits locales.
