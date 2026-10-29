# Pendientes, volúmenes y pestañas del creativo

Implementación de las ampliaciones aceptadas para Minecraft 26.2 / NeoForge 26.2.0.88. Código y recursos preparados; **sin compilación, ejecución Java ni pruebas dentro del juego**. La revisión estática no acredita funcionamiento ni rendimiento en Minecraft.

## Uso y alcance

- Martillo: Ctrl + clic derecho marca dos esquinas sobre bloques pintables al alcance normal. Se permite desplazarse entre ellas. Tras la validación, clic derecho confirma; Escape o cambiar de herramienta cancela. Máximo inclusivo 256 posiciones por eje; selección por centros de celdas, también con subdivisiones mezcladas. El 1×1 se ignora.
- Formador: dos clics sobre bloques pintables delimitan posiciones completas, hasta 16 por eje. Abre configuración de ascenso (+X, +Z, −X, −Z) y lado conservado. Previsualizar valida la operación; confirmar la ejecuta. Se muestran medidas, proporción de ascenso, posiciones revisadas y desgaste previsto. Las configuraciones incompatibles se pueden corregir sin perder las esquinas.
- Creativo: modo del formador para crear piezas nuevas, con dimensiones X/Y/Z de 1 a 16 y resolución elegible. Se ancla junto a la cara de la primera marca. Requiere aire sin fluidos y rechaza intersección con entidades; no sustituye construcciones existentes. El modo de transformación respeta bloques ajenos y huecos.
- Pestañas: bloques (cinco normales, cinco pendientes básicas y producción), herramientas (incluido formador y cubos) y 337 pinturas vanilla ordenadas por catálogo. Pinturas imposibles en supervivencia llevan indicación en su nombre. No se añaden integraciones ni pestañas vacías.

## Geometría y pintura

`SlopePlane` representa un semiespacio con coeficientes enteros normalizados. Cada pieza guarda un plano local y una orientación compartida. La transformación intersecta la ocupación con el plano global de la selección: no añade material; conserva caras pintadas supervivientes y deja sin pintar las nuevas. Un plano diferente en una pieza inclinada se rechaza; repetir el mismo es estable. Las piezas siguen siendo independientes.

`SlopeGeometry` calcula polígonos convexos de caras axiales y del corte. El fragmento inclinado tiene una identidad distinta de las seis caras de celda. Selección y ray tracing usan polígonos reales (`DesignOutlineShape`); la colisión utiliza cajas de 1/16, independiente de la subdivisión. El apoyo se limita a caras exteriores axiales completas. La versión 1×1 admite formación de pendiente pero mantiene la prohibición de recorte por martillo, mazo y cincel.

Pintar cobra `techo(256 × área real)` por fragmento cambiado, mínimo una unidad. Repintar con la misma identidad no cobra. El área sigue en un único bloque y plano, seleccionando centros de fragmentos. Raspador limpia el mismo plano, incluso a través de huecos; esponja limpia todas las pinturas. Mazo y cincel conservan su lógica de columnas/capas de celdas; al apuntar una cara inclinada usan su eje dominante, también empleado por la llave para seleccionar un eje de giro permitido.

La proyección recorta las capas de textura en el eje dominante y eleva sus vértices al polígono real, con UV continuas y normales inclinadas. No repite la textura por celda. La malla del inventario usa el mismo diseño; tenazas, guantes, copia y giro conservan el plano conforme a sus reglas de pintura. Pendientes cuentan como recortadas y no se convierten en estaciones. Se restan las superficies de contacto cubiertas por piezas vecinas, incluso parcialmente y con distinta resolución, conservando solo los fragmentos exteriores.

## Trabajo por lotes y protecciones

`VolumeEditing` mantiene una sesión por jugador: primera esquina, configuración, validación, listo y ejecución. Recorre posiciones de bloque sin crear una lista de todos sus minicubos. Presupuesto compartido inicial: hasta 2048 posiciones o aproximadamente 3 ms por tick, con turnos entre jugadores; una operación individual puede exceder el tiempo si un bloque es costoso. Son límites técnicos iniciales, no rendimiento medido.

La validación completa comprueba chunks, geometría, permisos y eventos antes de permitir confirmar. Al ejecutar se repiten permisos, estado y eventos de edición/rotura; creación publica también evento de colocación con estado previsto. Los listeners no deciden la geometría: se recalcula tras ellos. No se fuerza carga de chunks. Mutaciones de diseños y colocación/retirada invalidan selecciones afectadas; callbacks de trabajadores se trasladan al hilo del servidor.

Interrupción, descarga, pérdida de permiso o desconexión detienen lo pendiente y conservan lo ejecutado. No hay rollback, drops ni trabajos guardados/reanudados. La interfaz avisa antes de confirmar. Previsualización: contorno global y detalle de hasta 27 piezas cercanas, sin construir una malla de todo el volumen.

## Decisiones resueltas en esta entrega

- Nombre definitivo **Formador de pendientes**; ID `paintableblocks:slope_former`; una variante, **4096 puntos**, uno por bloque realmente modificado. Comprobar que alcanza para toda la operación antes de empezar; creativo no desgasta. No aplicar reducción aleatoria por Irrompibilidad al coste definido.
- Receta: tres lingotes de hierro en la fila superior, diamante central y palo debajo (`III / ␠D␠ / ␠S␠`); un formador. Desbloqueo al obtener diamante. El coste sitúa la formación posterior al martillo; 4096 permite cubrir el máximo de posiciones de una operación 16³ con una herramienta nueva.
- Pendientes se representan como variantes de diseño de los cinco bloques existentes, sin duplicar registros. El creativo ofrece cinco diseños básicos y el formador configura el resto.
- Creación creativa solo en aire sin fluidos; anclaje junto a la cara inicial. Transformación nunca añade volumen. Las interrupciones también conservan lo ya creado/transformado.
- Formato de diseño **2**, lectura de formato **1** como diseño sin plano; se conservan sus claves de pintura. Nueva cara inclinada en claves separadas desde 24576. Protocolo **5**: actualizar cliente y servidor conjuntamente.
- Modelo 3D propio del formador generado después de sus funciones, con materiales existentes de 16×16 ya incluidos en el atlas. No se modifica ningún PNG ni el original `unpainted.png`. Geometría de carcasa sin contactos coplanares superpuestos.

## Comprobaciones y límites

Se revisan sintaxis Java con tree-sitter, imports contra el cliente oficial y firmas nuevas contra metadatos del cliente/patches exactos NeoForge; sin ejecutar clases. La receta se compara sin coincidencias con 733 patrones vanilla. Pasan validadores estáticos de grupos 1–3, atlas, recursos y ampliaciones; seis pruebas Python del atlas. El modelo del formador eleva a 30 las variantes de recursos revisadas, con 21 014 parches y cero solapes coplanares en modelos JSON.

`SlopeVolumeContractChecks` prepara regresiones de migración, planos y sus 24 giros, área inclinada, consumo, herramientas, límites inclusivos, ray tracing y contacto entre resoluciones. **No ejecutado**. Antes de afirmar verificación en juego faltan compilación autorizada y pruebas de marcha/uniones, selección y UI, multijugador/protecciones, guardado/copia y rendimiento con volúmenes grandes. No se afirma comprobación completa de tipos ni funcionamiento visual por el análisis de sintaxis.
