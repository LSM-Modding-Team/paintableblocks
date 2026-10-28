# Grupo 2: herramientas y pinturas

> Actualización posterior: [correcciones de interacción](interaction-fixes.md) sustituye el protocolo, la selección de peticiones, la actualización visual, los eventos de edición y las cachés descritos originalmente en esta entrega. Incluye la correspondencia con el fallo visual y los otros 16 puntos revisados.

Código para Minecraft **26.2**, NeoForge **26.2.0.88** y Java 25. Incluye los cinco frentes elegidos: recorte/limpieza, colocación/giro, pintura/rasquetas, pinceles/aplicación y cubos. Se conserva la colocación y representación de diseños del grupo 1. La revisión es estática: no se ha compilado ni iniciado Minecraft.

## Correcciones previas

- `CollectionPolicy` utiliza `params.getLevel().getRandom()` en la probabilidad de supervivencia de explosiones, evitando acceder al campo protegido `Level.random`.
- Se incorpora el archivo original `textures/block/unpainted.png` sin modificar: 16×16 píxeles, 93 bytes, SHA-256 `aca1bd691809249ca928990bca71ea1cee4a150e409bd2d14e664baaa2472afa`. `models/block/unpainted.json` referencia esa textura original; no se convierte en pintura.
- Se eliminan `items/tongs.json` y `items/gloves.json`. No se crean modelos, definiciones visuales de ítem ni texturas de herramientas, tampoco provisionales. Se acepta missing model/texture para todas las herramientas y recipientes nuevos.

## Edición y giro

`DesignOperations` calcula planes inmutables que comparten servidor y selección visual. El martillo retira un subcubo; el mazo busca el primero ocupado en cada columna desde la dirección elegida; el cincel retira exclusivamente la capa de profundidad seleccionada. Los tres rechazan recortar 1×1. Las superficies que quedan descubiertas no heredan pinturas y el último recorte elimina directamente el bloque, sin devolver materiales.

El raspador limpia las superficies expuestas con la misma orientación y profundidad. La esponja limpia todo el diseño, incluidas superficies interiores expuestas. No modifican la ocupación ni devuelven pintura.

La llave compone giros de 90° alrededor de la normal mundial apuntada; Shift invierte el sentido. Forma y pinturas viajan juntas dentro de las 24 rotaciones propias. Se rechazan giros cuyo volumen resultante interseque entidades; el contorno avisa en rojo. No se altera la colocación similar a una escalera, el diseño conservado ni la copia creativa del grupo 1.

Las acciones de edición utilizan clic derecho. La rotura normal con clic izquierdo conserva las reglas de drops del grupo 1. Las herramientas de edición no tienen niveles ni desgaste.

## Pinturas y rasquetas

Las 337 identidades mantienen el catálogo aprobado y su representación de pintura del grupo 1. Se registran seis rasquetas con usos máximos 64, 128, 256, 128, 1024 y 4096. Un componente guarda los usos restantes; no tienen reparación ni propiedades de encantamiento y se rechaza una rasqueta encantada para extraer.

La receta especial `extract_paint` admite un bloque y una rasqueta, o un patrón 3×3 con ocho bloques idénticos y rasqueta central. Produce uno u ocho ítems de pintura, cada uno de 256 unidades. Descuenta un uso por bloque; devuelve la rasqueta con su estado o la elimina si se agota. No admite el lote de ocho si faltan usos.

Los ingredientes de origen deben ser ítems vanilla sin componentes adicionales, para no destruir datos personalizados. Nieve en polvo se obtiene desde su cubo y devuelve el cubo vacío. Las entradas exclusivas de creativo no tienen extracción de supervivencia; siguen disponibles en el catálogo creativo.

## Pinceles y selección

Se registran seis pinceles sin desgaste con capacidades **1024, 2048, 4096, 8192, 16 384 y 65 536 unidades**. Su componente distingue identidad y unidades reales; tener una identidad seleccionada con cero unidades no concede stock.

Clic derecho pinta un cuadrado. Ctrl + clic derecho fija la primera esquina; un clic derecho normal confirma la segunda. Otro Ctrl + clic derecho cancela. El área pertenece al mismo bloque, orientación y plano; ignora huecos sin alcanzar caras más profundas. Un segundo punto inválido mantiene la selección para poder corregirlo.

El consumo por cuadrado es `256 / n²`. Solo se cobran las superficies cuya identidad cambia, sin reembolso por la pintura sustituida. Si no alcanza para todo el área, no hay aplicación parcial ni consumo. La selección se cancela al cambiar de ítem, de identidad, de dimensión, de diseño o al perder alcance/permisos. Un cambio de pintura externo también invalida la instantánea para evitar confirmar sobre un estado distinto.

En creativo no se consumen unidades. La identidad se elige desde un cubo o sosteniendo un ítem de pintura en el cursor y haciendo clic derecho sobre el pincel del inventario. Esta combinación no consume pintura ni genera unidades transferibles. Si hay unidades reales de otra identidad se debe vaciar primero. Al pasar a supervivencia solo pueden utilizarse unidades reales y las pinturas permitidas allí.

## Cubos y transferencias

Se registran cuatro bloques/ítems portátiles: cobre 1×262 144; oro 4×524 288; diamante 8×262 144; netherite 8×524 288 unidades. `PaintStorage` es una paleta inmutable ordenada, sin identidades duplicadas ni entradas vacías. El número de pinturas y la capacidad individual se validan por separado, también al colocar y cargar datos.

Clic derecho con pincel carga la identidad seleccionada; Shift + clic derecho devuelve lo que quepa y deja el resto en el pincel. No se mezclan identidades con unidades reales. Clic derecho con pintura introduce los ítems completos que quepan; con mano vacía se abre el menú. Si una transferencia directa no puede continuar, se muestra un aviso.

El menú ofrece ocho filas de pintura/cantidad, selección por jugador, depósito desde el inventario y retirada de uno o hasta 64 ítems completos. Shift + clic sobre pintura del inventario la deposita. Las fracciones de 256 unidades permanecen almacenadas; nunca se redondean ni se tiran sobrantes. La inserción de salida utiliza huecos y pilas compatibles reales, evitando el descarte de excedentes que puede permitir el inventario creativo.

La selección se recuerda para el último cubo utilizado por cada jugador durante la sesión; al cambiar de cubo, o agotarse esa identidad, se elige la primera identidad disponible. No modifica la elección de otros jugadores. Los botones usan el menú nativo, revalidan jugador, posición, entidad, alcance y permisos; las cantidades se sincronizan en palabras de 16 bits para no truncar capacidades grandes.

Romper un cubo en supervivencia conserva todo en un único ítem. En creativo solo el cubo con contenido produce ese ítem al romperse. Ctrl + clic central copia contenido; la copia simple obtiene uno vacío. Se conservan componentes, tooltips y el contenido superviviente de explosiones. No los mueven pistones; el tag de pico acelera su recogida sin exigirlo para conservar el contenido. Datos de mundo inválidos se conservan en bruto y quedan sin transferencias ni drops recuperables.

## Mejoras y límites del grupo

`upgrade_tools` incorpora la herrería con plantilla de mejora e ingote de netherite para pincel, rasqueta y cubo de diamante. Copia componentes; el pincel y cubo conservan exactamente su contenido. La rasqueta conserva la proporción de usos restantes con cálculo entero, sin restaurarse gratuitamente.

Las estaciones, molino/prensa, sus menús y las recetas generales de fabricación y mejoras de otros materiales pertenecen al grupo 3. No se inventan sus patrones para cerrar este grupo: las variantes nuevas están registradas y accesibles en creativo; la extracción y herrería anteriores sí tienen recetas de datos. El archivo reservado `CreativePaintSelectionRecipe.java` sigue vacío porque se utiliza una interacción de inventario con contexto de jugador.

## Red y selección previa

El paquete cliente → servidor transmite únicamente Ctrl, sentido y cancelación. No acepta del cliente posiciones, geometría, cantidades ni resultados. El servidor vuelve a trazar el impacto contra la forma real, comprueba chunks cargados, alcance y permisos, consulta eventos de interacción/protección y recalcula el plan. Revalida el estado después de los eventos antes de consumir o aplicar. Limita acciones a una por tick y no carga chunks para buscar objetivos; rechaza alcances anómalos superiores a 128 bloques.

La confirmación de esquina viaja del servidor al cliente. Los contornos se extraen con la API 26.2 y solo capturan geometría inmutable: subcubos, superficies o forma girada. No retienen mundo/entidades en callbacks de render. Pintado sin suficientes unidades y giro obstruido usan rojo. Tenazas y guantes mantienen su contorno nativo exacto y mensajes de conservación.

## Decisiones tomadas durante esta entrega

1. **Selección creativa:** sustituir la receta sin forma por la combinación con clic derecho en inventario, comprobando el modo del jugador. Las recetas vanilla no reciben al jugador al calcular coincidencia/resultado; una receta global podría quedar disponible en supervivencia.
2. **Menú de cubos:** usar botones nativos y fondos sólidos, sin recursos visuales nuevos ni ranuras temporales que puedan retener y perder materiales al cerrar. Las cantidades completas y fracciones permanecen en la paleta.
3. **Selección por usuario:** recordar el último cubo de la sesión, con primera identidad como alternativa al cambiar o agotarse; evita estado compartido y almacenamiento persistente de preferencias.
4. **Ingredientes personalizados:** no convertir pinturas/bloques con componentes adicionales en cantidades anónimas. Mantener intactos esos ítems evita destruir nombres, contenidos o datos de otros sistemas.
5. **Selecciones concurrentes:** invalidar ante cualquier cambio del diseño, también de pinturas, para que una confirmación no consuma sobre una instantánea antigua.

## Evidencia y trabajo pendiente

Se leyeron las firmas públicas/protegidas del cliente oficial 26.2 como metadatos de clases y las fuentes/parches exactos NeoForge 26.2.0.88: eventos `BreakBlockEvent`, interacción/input, paquetes, apertura de menú y extracción de contornos. No se ejecutó ni descompiló Java. Las referencias de investigación y hashes están en [NeoForge 26.2](neoforge-26.2.md).

- `python tools/validate_group1.py --minecraft-jar /ruta/minecraft-26.2-client.jar`: catálogo, JSON, enlaces, recursos e importaciones vanilla; sin usar el antiguo `--scope-check`, que describe exclusivamente el límite histórico del grupo 1.
- `python tools/validate_group2.py`: PNG original, ausencia de los recursos de 24 herramientas/recipientes, recetas, traducciones y límite con las máquinas del grupo 3.
- Revisión de sintaxis Java mediante parser y contraste de importaciones explícitas con el JAR oficial/fuentes NeoForge. No comprueba todos los tipos ni equivale a compilación.
- `ToolPaintContractChecks.java`: casos preparados de mazo/cincel, último recorte, giros, planos con huecos, consumo, limpieza interior, transferencias y creativo. **No ejecutados**.

Quedan sin verificar compilación, carga, crafteos, menú/render, persistencia, interacción y sincronización multijugador en Minecraft. No se ejecutaron Gradle, Java, CI ni el juego. El commit se crea directamente online en `26.2`, sin commits locales.
