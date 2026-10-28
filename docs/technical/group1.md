# Grupo 1: bloques y diseños

> Actualización posterior: el grupo 2 incorpora el original `unpainted.png`, retira las referencias visuales de tenazas/guantes y corrige `Level.random` a `getRandom()`. Las decisiones 1 y 5 de esta entrega quedan sustituidas; véase [Grupo 2](group2.md).

## Alcance y estado

Se avanzan los cinco frentes elegidos: **catálogo y balance**, **datos de diseño**, **geometría y selección**, **guardado y conservación** y **representación del diseño**. Estado: implementados en código y recursos, con comprobaciones estáticas; **sin compilación, carga ni pruebas en Minecraft**.

Las herramientas de recorte, pinceles, cubos, producción, máquinas, interfaces y recetas de supervivencia siguen pendientes. Tenazas y guantes sí forman parte de conservación. Sus funciones se implementaron primero; sus recursos actuales son referencias provisionales a modelos vanilla, no sus modelos/texturas definitivos.

## Catálogo y balance

- `PaintCatalog` carga 337 identidades inmutables desde un JSON incluido en el mod. Se genera desde el CSV definitivo sin renumerar entradas y sin permitir a un pack cambiar sus identidades.
- Los nombres del catálogo están en español e inglés y cada pintura conserva nombre e identidad propios mediante un componente del único tipo de ítem `paint`.
- En creativo se muestran cinco bloques, tenazas, guantes y las 337 pinturas. No se añade extracción de pintura en esta entrega.
- Se valida la existencia de los bloques vanilla tras el registro. Las apariencias fijan eje vertical, frente norte, estados apagados y nexo sin carga cuando corresponda.
- `PaintingBalance` centraliza resoluciones, dureza, resistencia, unidades, capacidades, durabilidad y rendimientos acordados. Los frentes futuros consultarán esos mismos valores.

## Datos de diseño

`BlockDesign` es inmutable. Guarda ocupación mediante bits, pinturas únicamente en caras expuestas y una orientación compartida. Los mapas de caras se ordenan para que el orden de edición no distinga ítems equivalentes.

Formato persistente inicial:

| Campo | Significado |
|---|---|
| `version` | 1; se rechazan versiones desconocidas |
| `resolution` | 1, 2, 4, 8 o 16 |
| `occupied` | Lista de enteros largos; bit de la celda `x + n*(y + n*z)` |
| `paints` | Entradas `key` / `paint`, sin duplicados ni caras ocultas |
| `orientation` | Índice 0..23 de una rotación propia del cubo |

La clave de cara es `cell*6 + direction.ordinal()` para las seis direcciones de la plataforma fijada. No se guarda una pintura para la apariencia original: ausencia de entrada significa sin pintar. Se limita el tamaño de las listas y se rechazan bits fuera del volumen, IDs fuera de 1..337, caras ocultas y duplicados.

Los 24 giros son transformaciones de determinante positivo, sin reflejos. La identidad es 0; la colocación habitual compone una de las ocho orientaciones tipo escalera con la orientación conservada. La herramienta de giro queda para el grupo 2.

## Geometría y selección

`DesignGeometry` enumera únicamente superficies expuestas. La colisión se construye con cajas disjuntas combinadas a partir de las celdas existentes; no rellena huecos ni elimina piezas desconectadas. La forma se conserva en la entidad y no se recalcula por un simple cambio de pintura.

El raycast vanilla usa esa forma; una conversión de impacto a subcubo trabaja con la orientación inversa y verifica que la cara esté expuesta. La forma adicional de interacción queda vacía para no sustituir el resultado por una caja completa.

La selección vanilla destaca la geometría restante. Con tenazas o guantes, el cliente añade un mensaje previo que indica si conservará las pinturas. No hay modificaciones del mundo en el código cliente.

## Guardado y conservación

- Cinco registros de bloque/ítem comparten un tipo de entidad de bloque. No hay registros por diseño o pintura.
- El componente `block_design` usa el mismo codec para persistencia y sincronización de componentes. Las entidades usan Value I/O, tag de chunk, paquete de actualización e integración de componentes implícitos con los ítems.
- El servidor cambia los diseños y publica cambios de forma/modelo. Un diseño vacío elimina el bloque sin drops.
- La colocación valida resolución, contenido y colisión real antes de consumir el ítem. Después restaura el diseño y aplica la misma orientación usada para comprobar la colocación.
- Los guantes conservan forma y pinturas; las tenazas conservan forma sin pinturas. Las excepciones solo corresponden a una rotura atribuida a jugador con esa herramienta.
- La rotura convencional entrega un bloque sin pinturas si está completo, y nada si tiene recortes. Las herramientas excluidas y las armas salvo hachas no recuperan bloques. Fortuna y Toque de seda no añaden una excepción a esta política.
- Ctrl + clic central conserva el diseño únicamente en creativo; la copia simple obtiene una variante nueva. Los ítems distintos se distinguen por componentes y por su representación.
- Los datos de mundo inválidos o de otra versión se conservan sin reinterpretarlos como un bloque nuevo; no generan drops ni ítems de diseño. Se muestran con una forma sólida de reserva para que puedan retirarse sin crear recursos.

## Representación

`DesignMesh` proyecta los quads del modelo vanilla de cada pintura sobre la superficie local seleccionada. Recorta el polígono por los límites tangenciales del cuadrado e interpola sus UV: no escala la textura entera por subcubo. La profundidad solo coloca el plano expuesto. La transformación del bloque gira después las posiciones y las normales de cara conjuntamente.

Las capas y animaciones del recurso se conservan. Los tintes se calculan con los valores predeterminados y se incorporan como color fijo, igual en mundo e inventario. Los materiales pintados se normalizan a emisión de luz cero y sombreado normal; las propiedades del bloque nunca se copian del catálogo.

Mundo e inventario usan la misma fábrica de mallas. El modelo de ítem usa el wrapper 3D nativo, sus transformaciones y límites. El bote de pintura tiene un cuerpo que muestra la pintura y una tapa/asa metálicas con recursos vanilla. La geometría de mundo se obtiene de `ModelData`, sin acceder directamente a la entidad desde el hilo de mallado.

La fábrica se comparte dentro de cada carga de recursos mediante `ModelBaker.compute`. Su caché limita el número de diseños a 64 y el total almacenado a 65 536 quads; los diseños más grandes no quedan retenidos si exceden el presupuesto. Una recarga crea otra fábrica y evita reutilizar geometría de la carga anterior.

## Decisiones tomadas durante esta entrega

El usuario autorizó resolver problemas o reglas faltantes sin consulta previa. Se aplicaron estas decisiones:

1. **Textura original ausente:** no se encontró el archivo original `unpainted.png` en el repositorio. Se usa temporalmente la textura vanilla de piedra mediante `models/block/unpainted.json`, sin generar un PNG ni recrear la imagen elegida. La integración de ese original sigue pendiente; la reserva no lo sustituye como acabado definitivo.
2. **Pinturas exclusivas de creativo:** bedrock, budding amethyst y reinforced deepslate se identifican como no obtenibles en supervivencia en el catálogo actual. Las rutas futuras de obtención consultarán esa condición; no se añade una receta alternativa.
3. **Iluminación de recortes:** un estado booleano `cut` diferencia bloque completo y recortado. El completo atenúa la luz como sólido; el recortado permite paso de luz, sin una simulación por subcubo. Colisión y selección sí utilizan el volumen exacto. Las pinturas nunca emiten luz.
4. **Datos inválidos:** conservación del valor bruto y rechazo de recursos recuperables; forma sólida de reserva. No se restaura automáticamente un bloque completo válido.
5. **Recursos de conservación:** referencias 3D vanilla provisionales para tenazas y guantes; sus modelos y texturas finales quedan disponibles como trabajo individual después de cerrar sus funciones.
6. **Código compartido de representación:** caché por carga de recursos y presupuesto de quads, para evitar una caché independiente por cada una de las cinco subdivisiones y por inventario.

## Comprobaciones y límites

`python tools/validate_group1.py` (Python 3.11 o posterior) solo lee datos y fuentes. `--scope-check` comprueba además los límites de esta entrega, sin impedir usar el validador básico después de avanzar otros grupos. Con `--minecraft-jar /ruta/al/cliente-26.2.jar`, primero comprueba el SHA-1 oficial y después lee el ZIP: contrasta los 337 modelos vanilla, importaciones y referencias de recursos sin ejecutar Java.

Se comprobaron catálogo, 674 traducciones de sus nombres, cinco variantes, JSON, tags, separación común/cliente y límites del frente. Se revisó además la sintaxis Java con un parser, las firmas del cliente oficial 26.2 y las extensiones/access transformers de NeoForge 26.2.0.88. Esto no es compilación ni comprobación completa de tipos.

`src/test/java/net/nicomar/paintableblocks/DesignContractChecks.java` prepara casos de codec, 24 orientaciones, invariantes de volumen, superficies internas, túneles, colisión y el caso 16×16 con 2048 subcubos desconectados. **No se ejecutó**, ya que requiere compilar y disponer del classpath de desarrollo.

No se ejecutaron Gradle, builds, Java ni Minecraft. La fidelidad visual, persistencia durante una partida, sincronización y comportamiento real siguen sin verificación en ejecución. La publicación se hace directamente online en `26.2`, sin commits locales.

El cliente de referencia se obtuvo del [manifest oficial de Mojang](https://piston-meta.mojang.com/mc/game/version_manifest_v2.json), verificando la metadata y el SHA-1 antes de leerlo como ZIP.
