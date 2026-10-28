# Registro de cambios

Registra avances reales del repositorio y distingue código preparado, revisión estática y comportamiento comprobado dentro de Minecraft. Las entradas no implican una versión distribuida del mod.

## 2026-10-28 — Corrección de carga de texturas compartidas

- Detectado que los PNG de `textures/material/` no eran descubiertos por los atlas estándar de Minecraft 26.2, aunque existieran en disco. Trece materiales referenciados podían mostrarse como textura ausente; el directorio contenía 14 PNG.
- Trasladados los 14 materiales a `textures/block/material/` y actualizadas referencias de modelos, herencia, partículas, manifiesto y generador. Se conservan exactamente los bytes/píxeles de las imágenes y el original `unpainted.png`.
- Nuevo helper `tools/texture_atlas.py` y validación de pertenencia real al atlas, usando los JSON del cliente oficial 26.2 con SHA-1 comprobado. La nueva comprobación reproduce el fallo antes de corregirlo y confirma 28 sprites de modelos disponibles después.
- Añadidas seis pruebas de regresión Python para directorios, namespaces, alias, separación de atlas y fuentes explícitas; pasan todas. Pasan también los validadores de grupos 1–3 y de superficies/UV/colisión.
- Revisados otra vez los modelos: se mantienen idénticos elementos y transformaciones de 33 archivos, sin solapes en 20 654 parches. Las nueve formas físicas coinciden y la vista técnica regenerada permanece idéntica.
- Decisión adoptada: usar rutas convencionales de sprites de bloque, sin modificar atlas globales ni duplicar PNG. No fue necesario rediseñar texturas o geometría.
- Informe actualizado en [recursos visuales](docs/technical/visual-assets.md). Sin cambios Java, protocolo, mecánicas o recetas; sin Gradle, compilación, Java, CI o Minecraft. Publicación directa online únicamente en `26.2`, sin commits locales.

## 2026-10-28 — Recursos visuales de bloques y herramientas

- Completados modelos 3D de 20 herramientas: ocho de edición/conservación/giro, seis pinceles y seis rasquetas, con materiales y siluetas diferenciados.
- Añadidos recursos de los cuatro cubos de pintura: recipientes con tapa, asa y distintivos de 1/4/8/8 identidades por nivel. Sus pinturas/cantidades siguen siendo datos reales de interfaz/tooltip.
- Sustituidos los cinco cubos provisionales de producción por banco, cortadora, ensambladora, molino y prensa con volumetría de taller.
- Creadas **28 texturas originales de 16×16**, todas opacas; conservado el original `unpainted.png` sin modificar. Las familias comparten geometría y materiales mediante modelos padre; no se utilizan iconos planos como sustituto del 3D.
- Prevención de z-fighting: geometría exterior de uniones de vóxeles, eliminación de caras de contacto, rectángulos fusionados, UV explícitos y ausencia de planos de grosor cero o adornos coplanares. Cero solapes detectados en 20 654 parches unitarios de los 33 modelos con superficies revisados.
- Nueve bloques de mundo integran colisión, selección y apoyo desde los mismos sólidos del modelo, mediante 91 cajas disjuntas. Se evita el cubo invisible provisional y la ocultación de vecinos a través de huecos.
- Bote de pintura: tapa/asa remalladas con acero propio, retiradas las caras ocultas entre cuerpo y tapa. Proyección vanilla: filtrada la normal real de los quads para no aplanar también caras de vuelta, como las de nieve en polvo. Se mantienen las capas decorativas intencionadas y las 337 identidades.
- Decisiones adoptadas: estética de madera/acero/cobre; niveles por material/color; recipientes cerrados con marcas de capacidad de identidades; formas físicas coincidentes; preservación de los diseños y texturas originales del catálogo. Detalle en [recursos visuales](docs/technical/visual-assets.md).
- Añadidos generador reproducible de recursos/formas, render técnico de inspección y validador de UV, referencias, opacidad, contactos y equivalencia visual/física. Actualizados los validadores de grupos 2/3 y la documentación para la autorización visual vigente.
- Los validadores de recursos pasan y se revisó la sintaxis de 70 archivos Java. La lámina técnica representa los JSON/PNG por software; no es una captura de Minecraft ni acredita ausencia de artefactos con shaders externos.
- Sin Gradle, compilación, ejecución de Java, CI o Minecraft. Quedan pendientes las comprobaciones visuales en juego. Publicación directa online en `26.2`, sin commits locales. El protocolo se mantiene en 4; actualizar ambos lados por las nuevas formas físicas compartidas.

## 2026-10-28 — Grupo 3: producción e integración

- Añadidos banco, cortadora y ensambladora: multiplicadores ×2/×4/×8, capacidades compartidas 1024/4096/16 384, fabricación de las cinco subdivisiones y consumo únicamente si cabe el lote completo.
- Conversión 1:1 de contadores y un par de ranuras para convertir bloques completos sin pintura conservando orientación y componentes; los contadores solo admiten ítems canónicos nuevos. Pintura y recortes impiden convertir.
- Añadidos molino y prensa: 4/16 pinturas por bloque, 80 ticks, sin combustible, validación del lote completo y del cubo vacío de nieve en polvo. Progreso conservado al recoger/colocar y reiniciado al cambiar la identidad de entrada; salida bloqueada pausa el proceso.
- Inventarios, contadores y progreso viajan en un único ítem mediante `ProductionState`. Sin dispersar inventario adicional al romper; Ctrl + clic central, creativo, explosiones, validación de datos y bloqueo de pistones integrados.
- Menús de estaciones y máquinas, pantallas con botones nativos/fondos sólidos, retiradas sin tirar excedentes, predicción coherente de depósitos y selección independiente por menú. Paquete de estación valida contenedor, permisos, alcance, subdivisión y material esperado. Protocolo de red **4**.
- Fabricación manual con los seis centros/exteriores aceptados. Recetas de ocho herramientas, cinco pinceles y cinco rasquetas iniciales, cubo de cobre, banco y molino; mejoras de estaciones/máquinas vacías y mejoras de pinceles/cubos conservando componentes y capacidad. Netherite mantiene herrería con plantilla.
- Unificados los filtros del catálogo entre rasquetas y máquinas; entradas personalizadas y pinturas exclusivas de creativo se rechazan sin consumir. Molino/prensa admiten tolvas con entrada arriba/laterales y salida debajo.
- Cinco modelos de bloque e ítem cúbicos **sin texturas asignadas**, conforme a la instrucción del usuario. No se genera ningún PNG ni se añaden recursos visuales de herramientas.
- Decisiones adoptadas: distribución de ranuras y cuatro salidas de máquina; conversión personalizada separada de contadores; progreso portátil; rutas de tolvas; dureza 3,5/resistencia 6/sonido piedra de bloques de trabajo; patrones y cantidades antes ausentes concretados en [recetas del grupo 3](docs/technical/group3-recipes.md). Mejoras especiales sin autorrelleno del libro para conservar datos; recetas normales con desbloqueos.
- Actualizados README, mapa de código, plan y documentación. Informe de alcance y límites: [Grupo 3](docs/technical/group3.md).
- Ampliados validadores estáticos y preparados contratos de lotes/capacidad, contadores, salidas llenas, nieve en polvo, persistencia y mejoras inválidas. Contrastes de recursos, importaciones y sintaxis; patrones nuevos comparados con 733 recetas vanilla con forma, sin coincidencias.
- Sin ejecutar Gradle, compilación, builds, Java, CI, clientes ni servidores. Los contratos Java y el funcionamiento en juego quedan sin verificar; publicación directa online en `26.2`, sin commits locales.

## 2026-10-28 — Correcciones de interacción del bloque pintable

- Corregido el fallo confirmado de actualización visual: cada recepción de cambios reconstruye la geometría cliente además de refrescar ModelData, aunque no cambie el estado `CUT`. Pintura y recortes posteriores dejan de depender de la primera actualización de estado.
- **1–4:** una confirmación de área invalidada no se convierte en pintura individual; los clics sobre otros bloques/entidades conservan su flujo nativo; herramientas y carga de pinceles admiten ambas manos sin doble ejecución; el trazado acepta objetivos cargados antes de un chunk descargado.
- **5–7:** el contorno de pintura incluye solo caras que cambian; planes y avisos no conservan costes de áreas inválidas; guantes, tenazas y copia muestran avisos de datos inválidos sin prometer conservación.
- **8–9:** recorte/giro actualizan formas y apoyos vecinos aun con el mismo BlockState. La colocación instala el diseño orientado mediante un contexto delimitado antes de callbacks y restauración de componentes, sin alterar el ciclo nativo de BlockItem.
- **10–12:** selección por mano/ranura/tipo/pintura, sin referencias de ItemStack. Peticiones y selecciones llevan identificadores; se validan dimensión, bloque UUID/revisión, impacto, cara y herramienta. Respuestas y clics antiguos se rechazan sin editar otro objetivo. El protocolo pasa a `3`; cliente y servidor deben actualizarse juntos.
- **13:** nuevo evento cancelable `PaintableBlockEditEvent`; únicamente retirar subcubos emite además `BreakBlockEvent`. Pintar, limpiar o girar no se anuncian como rotura. Se revalida después de listeners y se consume el objeto actual de la mano correcta.
- **14–15:** acciones del menú vinculadas a la identidad de pintura mediante un paquete con enteros explícitos y validación del contenedor; no dependen de filas móviles ni del ancho del botón nativo. Shift + clic predice el mismo depósito que realiza el servidor, sin mover pintura a la barra rápida.
- **16:** exposición local reutilizada por ocupación, proyecciones por superficie y cachés acotadas. La generación de mallas deja de mantener un bloqueo global durante los cálculos y comparte trabajos del mismo diseño; la colisión evita transformar mapas de pinturas.
- UUID/revisión se conservan en el mundo y se excluyen como identidad del ítem al recoger/copiar/colocar. Los diseños antiguos sin esos campos reciben metadatos nuevos sin cambiar el codec v1 de forma/pinturas.
- Añadidos avisos de área inválida en español/inglés y contratos de regresión para paquetes, contexto de colocación, reutilización de geometría y conservación de pinturas interiores/orientaciones. Los contratos Java quedan **sin ejecutar**.
- Informe y correspondencia de los 17 casos, límites de rendimiento y escenarios de aceptación: [correcciones de interacción](docs/technical/interaction-fixes.md).
- Pasan las comprobaciones estáticas de datos/recursos, sintaxis e importaciones. No se han ejecutado Gradle, compilación, builds, Java, CI ni Minecraft; no se declara verificación en juego o medición de FPS.
- Publicación por commit creado directamente online en `26.2`, sin commits locales. No se crean modelos ni texturas de herramientas.

## 2026-10-28 — Grupo 2: herramientas y pinturas

### Correcciones solicitadas

- Corregido el acceso protegido de `CollectionPolicy`: usa `Level.getRandom()`.
- Integrado el original `unpainted.png` de 16×16 sin modificar y conectado al bloque sin pintar.
- Eliminadas las definiciones visuales de tenazas y guantes. Todas las herramientas y recipientes nuevos quedan sin modelos ni texturas, tampoco provisionales, aceptando missing model/texture.

### Código incorporado

- Planes compartidos y selección previa para martillo, mazo, cincel, raspador, esponja y llave; capas fijas, columnas con profundidades distintas, limpieza interior y giros propios con rechazo de colisiones.
- Seis rasquetas con usos persistentes y extracción de uno u ocho bloques idénticos; desgaste exacto, agotamiento y devolución del cubo de nieve en polvo.
- Seis pinceles con identidad/unidades reales, selección de esquinas en el mismo bloque/plano, aplicación sin consumo parcial, sustitución y cancelación. Creativo selecciona sin crear stock transferible; supervivencia valida disponibilidad de la pintura.
- Cuatro cubos colocables y portátiles, paletas con límites de identidades/capacidad, carga/devolución de pinceles, depósitos y retiradas de ítems completos, fracciones conservadas y selección por jugador.
- Menú/pantalla con botones nativos y fondos sólidos; portabilidad, tooltips, copia creativa, conservación al romper y manejo no recuperable de datos inválidos.
- Receta de herrería con plantilla para mejorar pincel, rasqueta y cubo de diamante a netherite, conservando componentes y contenido/proporción de usos.
- Red de intenciones y confirmación de selección; servidor recalcula impacto/plan y revalida protección, alcance, chunks, estado e inventario. Contornos extraídos sin capturar entidades de mundo.
- Acceso creativo, nombres y mensajes en español/inglés; tag de pico ampliado a cubos. Las recetas generales y máquinas siguen correspondiendo al grupo 3.

### Decisiones tomadas por autorización del usuario

- Seleccionar pintura creativa con clic derecho de inventario sobre el pincel sustituye la receta sin forma: permite comprobar el modo del jugador sin exponer una receta global a supervivencia.
- Menú de cubos sin ranuras temporales ni recursos visuales propios; inserción limitada al inventario real, sin descarte creativo de sobrantes.
- La preferencia de pintura es individual y recuerda el último cubo de la sesión; al cambiar de cubo o agotarse una identidad se elige la primera disponible.
- No se convierten ingredientes con componentes personalizados en pintura anónima; se preservan intactos para evitar pérdida de datos.
- Una selección pendiente se invalida también si otro cambio modifica las pinturas, además de forma/orientación.

### Documentación y comprobaciones

- Actualizados AGENTS, README, mapa de código, plan, política visual y selección creativa; informe detallado en [Grupo 2](docs/technical/group2.md). Las decisiones provisionales del grupo 1 quedan sustituidas explícitamente.
- Validador estático del grupo 1 actualizado para los cubos y nuevo validador del grupo 2; comprobados catálogo, recursos, traducciones, PNG original y separación común/cliente. Revisadas sintaxis e importaciones/API de referencia.
- Añadido un arnés de contratos de herramientas/pintura, sin ejecutar. La revisión estática no acredita compilación ni funcionamiento en juego.
- Sin Gradle, compilación, builds, Java, CI, clientes ni servidores. Publicación directa online únicamente en `26.2`; sin commits locales.

## Sin publicar — Grupo 1: bloques y diseños

### Frentes implementados en código

- **Catálogo y balance:** 337 identidades sin renumeración, JSON derivado del CSV, nombres en español/inglés, validación del registro vanilla y constantes compartidas de propiedades, unidades, capacidades, desgaste y rendimientos. Las tres pinturas no obtenibles en supervivencia se identifican explícitamente como exclusivas de creativo.
- **Datos de diseño:** objeto inmutable y canónico con cinco resoluciones, ocupación por bits, pinturas solo en caras expuestas, codec versión 1, tamaños acotados y rechazo de duplicados/IDs/formatos inválidos. Grupo de 24 orientaciones sin reflejos y operaciones de datos para preparar integraciones posteriores.
- **Geometría y selección:** superficies expuestas, unión de cajas disjuntas sin rellenar huecos, colisión/contorno/selección sobre la forma real y conversión de impactos a caras interiores. La forma se conserva entre cambios que solo afectan pinturas.
- **Guardado y conservación:** cinco bloques/ítems, entidad compartida, componentes implícitos, Value I/O, actualizaciones y publicación de datos de modelo. Colocación del diseño con comprobación de volumen real y orientación conjunta; política de drops normal/explosión/herramienta y copia creativa exacta.
- **Representación:** modelos de bloque e ítem con la misma proyección de quads/UV por superficie; tintes fijos, capas y animaciones decorativas, emisión cero. Bote 3D para cada identidad de pintura. Caché compartida por carga de recursos con límite de entradas y quads.
- Tenazas y guantes se implementan dentro de conservación, sin durabilidad, con recogida nativa y mensajes previos sobre lo que conservan. No se implementaron las demás herramientas ni las máquinas o interfaces.

### Recursos y seguimiento

- Acceso creativo a las cinco subdivisiones, herramientas de conservación y 337 pinturas; traducciones de nombres y mensajes de selección.
- Tag aditivo de pico y archivos de modelos/ítems/estados. Sin recetas nuevas: el acceso de supervivencia corresponde al frente de recetas.
- Generador reproducible `tools/generate_catalog.py` y validador estático `tools/validate_group1.py`, ambos sin Java ni Gradle.
- Arnés de contratos Java preparado para codecs, orientaciones, superficies, túneles, cajas y caso 16×16 desconectado. No ejecutado porque requiere compilación.
- README, mapa del código, plan y seguimiento de recursos actualizados. Detalle en [Grupo 1](docs/technical/group1.md).

### Decisiones tomadas sin consulta, por autorización del usuario

- Al faltar el original `unpainted.png`, piedra vanilla como apariencia temporal sin crear ni recrear un PNG. El original sigue siendo el acabado pendiente.
- Bedrock, budding amethyst y reinforced deepslate identificados como pinturas exclusivas de creativo en el catálogo actual.
- Iluminación aproximada por estado completo/recortado: el completo atenúa como sólido y el recortado permite paso, sin simular luz por subcubo. Colisión y selección sí son exactas.
- Datos corruptos: conservar valor bruto, usar forma sólida de reserva y no generar ítems válidos automáticamente.
- Tenazas y guantes con referencias visuales 3D vanilla provisionales; sus recursos definitivos se realizarán individualmente después de sus funciones.
- Caché de mallas compartida entre mundo e inventario por carga de recursos, con presupuesto de 64 diseños y 65 536 quads.

### Comprobación y límites

- Validación estática de catálogo/674 traducciones, cinco variantes, 19 JSON y referencias, separación común/cliente y límites del frente.
- Cliente oficial 26.2 comprobado por SHA-1 y leído como ZIP para contrastar imports, 337 recursos vanilla y firmas; fuentes/access transformers de NeoForge 26.2.0.88 revisados. No se ejecutó ni decompiló el cliente.
- Sintaxis Java revisada con parser. No equivale a compilación ni comprobación completa de tipos.
- Sin Gradle, builds, Java, clientes, servidores ni pruebas en Minecraft. El arnés Java permanece sin ejecutar y el funcionamiento en juego sigue sin verificar.
- No se avanzaron los grupos 2/3 ni se generaron texturas definitivas de herramientas. Las reservas correspondientes siguen vacías.
- Publicación por commit creado directamente online en `26.2`, sin commits locales.

## Sin publicar — Base del mod

### Alcance

- Único frente elegido y avanzado: **Base del mod** para Minecraft 26.2 / NeoForge 26.2.0.88. Los demás frentes siguen pendientes; sus archivos reservados no representan funciones implementadas.
- Se crea este registro en la raíz, junto a `AGENTS.md`, los archivos Gradle y la licencia.

### Investigación

- Leídas 24 páginas de la documentación oficial, fijadas al commit `6d9f16c291474b48032bd633a69695261909a59b` de NeoForged/Documentation.
- La guía general disponible se identifica como 26.1; se contrasta con el primer oficial 26.2 y fuentes exactas 26.2.0.88, sin presentar ejemplos de otra versión como confirmados automáticamente.
- Descargados para lectura como ZIP las fuentes y parches userdev de NeoForge 26.2.0.88, NeoForm 26.2-2 y fuentes de FML 11.0.16. No se ejecutaron esos artefactos ni se incorporan al repositorio.
- Verificado el SHA-256 de las fuentes de NeoForge contra el publicado oficialmente. NeoForm confirma `java_target: 25`.
- Cubiertos registros/lados, diseño y geometría, interacciones y herramientas, componentes/persistencia, copia creativa/drops, modelos/contornos, pintura/cubos, inventarios/transferencias, menús, recetas/mejoras, máquinas y sincronización. Los cálculos propios y comportamientos del juego siguen pendientes.
- Informe reproducible: [NeoForge 26.2](docs/technical/neoforge-26.2.md), con enlaces, versiones, límites y puntos de integración.

### Esqueleto

- Entrada común `PaintableBlocks` conectada mediante `IEventBus` a ocho contenedores de `DeferredRegister`: bloques, ítems, componentes, tipos de bloque, entidades de bloque, menús, tipos y serializadores de recetas. No se registró contenido jugable.
- Entrada `PaintableBlocksClient` separada y restringida al cliente físico. No se añadieron importaciones de cliente a la base común.
- Creados **47 archivos Java literalmente vacíos**, organizados por responsabilidades futuras: balance, diseño, operaciones, interacción, pinturas, bloques, entidades, ítems/herramientas, inventarios, menús, producción, recetas, red, modelos, selección y pantallas.
- El código activo queda limitado a tres archivos Java de arranque/registro. Las reservas no se importan ni declaran funciones ficticias.
- Metadatos TOML actualizados para identificar la base, declarar `javafml` y Java 25, y conservar las dependencias exactas de plataforma.
- Conservados sin cambios Java 25, ModDevGradle 2.0.148, Gradle 9.2.1, el ID `paintableblocks`, paquete, versión del mod y configuración existente de cliente/servidor.
- Reservadas diez carpetas de recursos mediante `.gitkeep` y dos diccionarios vacíos `en_us.json` / `es_es.json`. No se crearon texturas, modelos, recetas, loot tables o tags de contenido.
- Añadidos [README](README.md) y [mapa de archivos](docs/technical/skeleton.md); registrado el estado de esta tarea en el plan, sin imponer orden al resto de frentes.

### Comprobación y límites

- Comprobación estática completada correctamente: TOML tras expandir variables, coherencia de plataforma, separación común/cliente, ocho registros conectados, tres archivos Java activos sin lógica jugable y 47 reservas de cero bytes.
- Comprobación completada de enlaces locales añadidos (ninguno roto), diez carpetas de recursos reservadas, dos diccionarios JSON vacíos y `git diff --check` sin errores. Estas comprobaciones no equivalen a compilación.
- No se ejecutaron Gradle, builds, tareas de IDE, decompiladores, Java, clientes, servidores ni pruebas dentro de Minecraft. Compilación y carga permanecen sin verificar.
- No se avanzó en catálogo/balance ejecutable, diseño, geometría, herramientas funcionales, máquinas o interfaces. Tampoco se generaron sus recursos visuales: primero se completarán las funciones de cada herramienta.
- `unpainted.png` sigue pendiente de integrar desde el original; no se recreó. Las recetas cuyos patrones no fueron definidos siguen pendientes, sin inventarlos.
- Publicación mediante commit creado directamente online en la rama `26.2`, sin crear commits locales.

### Alternativas habilitadas

Catálogo/balance, datos de diseño y preparación independiente de ítems o recetas cuyos requisitos estén completos. La elección del próximo frente sigue abierta y respeta las dependencias del plan.
