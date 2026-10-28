# Registro de cambios

Registra avances reales del repositorio y distingue código preparado, revisión estática y comportamiento comprobado dentro de Minecraft. Las entradas no implican una versión distribuida del mod.

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
