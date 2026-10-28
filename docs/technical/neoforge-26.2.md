# Investigación técnica de NeoForge 26.2

## Alcance y calidad de la evidencia

Investigación previa al código funcional de PaintableBlocks, dentro del único frente **Base del mod**. Cubre las mecánicas acordadas para identificar puntos de integración; no implementa esos sistemas ni prueba su funcionamiento.

La documentación general oficial consultada se identifica como **26.1**. No se encontró una sección general accesible bajo `/docs/26.2/`. Para evitar atribuirle compatibilidad inexistente, se combina esa guía con el [primer oficial de migración 26.1.x → 26.2](https://docs.neoforged.net/primer/docs/26.2/) y las fuentes exactas publicadas de **NeoForge 26.2.0.88**. El primer describe cambios de vanilla y no constituye por sí solo documentación de todas las extensiones de NeoForge.

### Referencias fijadas

| Fuente oficial | Evidencia utilizada |
|---|---|
| [Documentación general](https://docs.neoforged.net/docs/gettingstarted/) | Página que identifica la guía general como 26.1; requisitos y organización conceptual |
| [Instantánea de documentación](https://github.com/NeoForged/Documentation/tree/6d9f16c291474b48032bd633a69695261909a59b/docs) | 24 páginas leídas, enlazadas abajo por tema; el commit permite reproducir la consulta |
| [Primer 26.2](https://docs.neoforged.net/primer/docs/26.2/) | Cambios de representación, extracción de estados y envío de geometría; no reutilizar sin revisar renderizado antiguo |
| [POM 26.2.0.88](https://maven.neoforged.net/releases/net/neoforged/neoforge/26.2.0.88/neoforge-26.2.0.88.pom) | Identidad de versión y referencia SCM `1ad7d233fc1ff8c3cb5c8b159f1701aabf4b7b96` |
| [Configuración ModDev 26.2.0.88](https://maven.neoforged.net/releases/net/neoforged/neoforge/26.2.0.88/neoforge-26.2.0.88-moddev-config.json) | NeoForm `26.2-2`, FML `11.0.16` y coordenadas de los artefactos |
| [Fuentes NeoForge 26.2.0.88](https://maven.neoforged.net/releases/net/neoforged/neoforge/26.2.0.88/neoforge-26.2.0.88-sources.jar) | Inspección de clases y métodos exactos, sin ejecutar el JAR |
| [SHA-256 oficial de las fuentes](https://maven.neoforged.net/releases/net/neoforged/neoforge/26.2.0.88/neoforge-26.2.0.88-sources.jar.sha256) | Coincide con `4a2d5079b93659206e3078aee205311f86b549cfbd11eebf74bee36960d5f9e0` |
| [Parches userdev 26.2.0.88](https://maven.neoforged.net/releases/net/neoforged/neoforge/26.2.0.88/neoforge-26.2.0.88-userdev.jar) | Parches de `BlockEntity`, `BlockBehaviour`, `Item` y registros; leídos como ZIP |
| [NeoForm 26.2-2](https://maven.neoforged.net/releases/net/neoforged/neoform/26.2-2/neoform-26.2-2.zip) | Su `config.json` declara Minecraft 26.2 y `java_target: 25` |
| [Fuentes FML 11.0.16](https://maven.neoforged.net/releases/net/neoforged/fancymodloader/loader/11.0.16/loader-11.0.16-sources.jar) | `Mod` y `FMLModContainer`: separación física y constructor con bus inyectado |

Solo se descargaron documentos, fuentes, metadatos y parches para lectura estática. No se ejecutó Java, Gradle, decompilación, instalación, generación de recursos ni Minecraft. Los artefactos externos de investigación no se incorporan al repositorio.

## Arranque, registros y lados

La entrada común usa `@Mod` con el ID `paintableblocks` y un constructor público con `IEventBus`. Las fuentes de FML confirman que el contenedor inyecta ese bus y que exige exactamente un constructor público. La entrada de cliente comparte el ID y se restringe mediante `dist = Dist.CLIENT`; la base común no importa clases de `net.minecraft.client`.

`DeferredRegister` de 26.2.0.88 confirma `createBlocks`, `createItems`, `createDataComponents` y `register(IEventBus)`. La entrega conecta contenedores para ocho registros vanilla, sin entradas: bloques, ítems, componentes, codecs de tipo de bloque, tipos de entidad de bloque, menús, tipos y serializadores de recetas. No se consulta ningún objeto de registro durante el arranque ni se añaden registros propios vacíos innecesarios.

El namespace y paquete existentes se conservan: `paintableblocks` y `net.nicomar.paintableblocks`. No se adopta `paintingmod` de los adjuntos comparados. Se mantienen las versiones Gradle existentes, y los metadatos describen honestamente una base sin contenido funcional.

Fuentes conceptuales: [archivos del mod](https://github.com/NeoForged/Documentation/blob/6d9f16c291474b48032bd633a69695261909a59b/docs/gettingstarted/modfiles.md), [estructura](https://github.com/NeoForged/Documentation/blob/6d9f16c291474b48032bd633a69695261909a59b/docs/gettingstarted/structuring.md), [registros](https://github.com/NeoForged/Documentation/blob/6d9f16c291474b48032bd633a69695261909a59b/docs/concepts/registries.md), [eventos](https://github.com/NeoForged/Documentation/blob/6d9f16c291474b48032bd633a69695261909a59b/docs/concepts/events.md) y [lados](https://github.com/NeoForged/Documentation/blob/6d9f16c291474b48032bd633a69695261909a59b/docs/concepts/sides.md).

## Mecánicas acordadas y puntos de integración

Estas son conclusiones de preparación: cada fila describe el uso futuro de APIs, no una implementación terminada. Las condiciones del producto proceden de la especificación de Obsidian y siguen prevaleciendo sobre ejemplos genéricos.

| Mecánica del mod | Punto de integración investigado y límite de esta entrega |
|---|---|
| Cinco resoluciones, propiedades y bloques especializados | Registros de bloque/ítem y `BlockBehaviour.Properties`; reservar las implementaciones sin añadir bloques incompletos al mundo |
| Ocupación de subcubos y pinturas por superficie | Datos propios del diseño y entidad de bloque, no una combinación de blockstates por cada diseño |
| Colisión real, huecos y caras interiores | Forma del bloque y raycast sobre geometría restante; se requiere cálculo propio para las cinco resoluciones |
| Subcubos desconectados y desaparición del último | Operación sobre ocupación y actualización del bloque; no es una mecánica proporcionada automáticamente por NeoForge |
| Colocación tipo escalera y 24 orientaciones | Estado/colocación del bloque y transformación propia conjunta; la API de estados no calcula las rotaciones del diseño |
| Martillo, mazo y cincel | Interacción del ítem más cálculo común de subcubos afectados y validación en servidor |
| Raspador y esponja | Interacción con el bloque y modificación de pinturas conservando ocupación; la textura original no tiene identidad de pintura |
| Tenazas, guantes y drops convencionales | Pipeline de rotura y loot, condicionado al estado y herramienta, con un único resultado de conservación |
| Ctrl + clic central con diseño completo | Hook `IBlockExtension.getCloneItemStack(..., boolean includeData, Player player)` confirmado en las fuentes exactas; distinguir creativo de supervivencia |
| Guardado del diseño y contenido de ítems | Componentes de datos con codecs; Value I/O en entidades de bloque, publicación a clientes y marcado de cambios |
| Texturas continuas e interiores expuestos | Modelo de bloque personalizado y recorte propio de geometría/UV; no basta un modelo completo repetido por subcubo |
| Apariencia de las 337 pinturas | Resolver representación del bloque de catálogo sin adoptar sus propiedades ni lógica; identidad en componente del único tipo de ítem |
| Ítem conservado 3D y bote de pintura | Registro de modelos de ítem y representación a partir de componentes; reservado sin crear modelos definitivos |
| Vista previa antes de actuar | Extraer la selección y entregar solo datos al renderer; no conservar referencias al mundo para ejecutar renderizado diferido |
| Pincel individual y área coplanar | Interacciones más selección propia de dos esquinas; confirmar, cancelar y calcular el mismo coste que la vista previa |
| Capacidades, sobrantes y no mezclar pinturas | Componentes e inventarios; operación limitada por identidad/capacidad con resultado calculado antes de consumir |
| Creativo sin consumo y pinturas exclusivas | Modo del jugador y selección separada de unidades reales; la API no resuelve por sí sola esa política |
| Rasquetas y desgaste por bloque | Recetas dinámicas, resultado y restos del crafteo; datos de usos propios, sin reparación ni encantamientos |
| Mejoras y conservación de contenido | Serialización y validación de entradas/resultados; comprobar capacidad y vaciado antes de consumir |
| Banco, cortadora y ensambladora | Entidad de bloque y menú con contadores/inventario; acciones instantáneas, sin ticker de procesamiento |
| Conversión 1:1 solo en estaciones | Acción del menú sobre bloques completos y sin pintura; no registrar una receta manual de conversión |
| Molino y prensa | Ticker de servidor de entidad de bloque e inventario; cuatro segundos, lote completo y ningún combustible |
| Interfaces y selección individual del cubo | `MenuType`, menú servidor y pantalla cliente; mantener selección del usuario fuera del contenido compartido del cubo |
| Interacciones directas con cubos | Flujo de uso del ítem/bloque, carga, devolución e introducción de ítems completos, con validación de servidor |
| Inventario lleno y transferencias parciales permitidas | Contenedores y, si se necesitan, transacciones de recursos; conservar cantidades no transferidas |
| Recoger estaciones, máquinas y cubos sin duplicar contenido | Datos del ítem y efectos de retirada de la entidad; revisar `preRemoveSideEffects` antes de combinar contenido conservado y drops |
| Explosiones y pistones | Loot y reacción a pistones; seguir la política acordada y no conceder excepciones de herramientas a explosiones |
| Sin agua por subcubo | No añadir un inventario de fluido ni una simulación de celdas; los cubos de pintura guardan unidades propias |
| Recetas, tags y traducciones | Archivos de datos y recursos; reservar rutas sin inventar patrones ni declarar contenido todavía ausente |

### Fuentes conceptuales por sistema

- Bloques y orientación: [bloques](https://github.com/NeoForged/Documentation/blob/6d9f16c291474b48032bd633a69695261909a59b/docs/blocks/index.md), [estados](https://github.com/NeoForged/Documentation/blob/6d9f16c291474b48032bd633a69695261909a59b/docs/blocks/states.md).
- Ítems y datos: [ítems](https://github.com/NeoForged/Documentation/blob/6d9f16c291474b48032bd633a69695261909a59b/docs/items/index.md), [componentes](https://github.com/NeoForged/Documentation/blob/6d9f16c291474b48032bd633a69695261909a59b/docs/items/datacomponents.md), [interacciones](https://github.com/NeoForged/Documentation/blob/6d9f16c291474b48032bd633a69695261909a59b/docs/items/interactions.md).
- Persistencia y proceso: [entidades de bloque](https://github.com/NeoForged/Documentation/blob/6d9f16c291474b48032bd633a69695261909a59b/docs/blockentities/index.md), [Value I/O](https://github.com/NeoForged/Documentation/blob/6d9f16c291474b48032bd633a69695261909a59b/docs/datastorage/valueio.md).
- Inventarios y pantallas: [contenedores](https://github.com/NeoForged/Documentation/blob/6d9f16c291474b48032bd633a69695261909a59b/docs/inventories/container.md), [menús](https://github.com/NeoForged/Documentation/blob/6d9f16c291474b48032bd633a69695261909a59b/docs/inventories/menus.md), [transacciones](https://github.com/NeoForged/Documentation/blob/6d9f16c291474b48032bd633a69695261909a59b/docs/inventories/transactions.md), [capabilities](https://github.com/NeoForged/Documentation/blob/6d9f16c291474b48032bd633a69695261909a59b/docs/inventories/capabilities.md).
- Sincronización adicional: [payloads](https://github.com/NeoForged/Documentation/blob/6d9f16c291474b48032bd633a69695261909a59b/docs/networking/payload.md). No añadir paquetes personalizados cuando la interacción nativa ya resuelva el caso.
- Apariencia: [sistema de modelos](https://github.com/NeoForged/Documentation/blob/6d9f16c291474b48032bd633a69695261909a59b/docs/resources/client/models/modelsystem.md), [modelos personalizados](https://github.com/NeoForged/Documentation/blob/6d9f16c291474b48032bd633a69695261909a59b/docs/resources/client/models/modelloaders.md), [ítems de cliente](https://github.com/NeoForged/Documentation/blob/6d9f16c291474b48032bd633a69695261909a59b/docs/resources/client/models/items.md), [texturas](https://github.com/NeoForged/Documentation/blob/6d9f16c291474b48032bd633a69695261909a59b/docs/resources/client/textures.md).
- Producción y drops: [recetas propias](https://github.com/NeoForged/Documentation/blob/6d9f16c291474b48032bd633a69695261909a59b/docs/resources/server/recipes/custom.md), [loot tables](https://github.com/NeoForged/Documentation/blob/6d9f16c291474b48032bd633a69695261909a59b/docs/resources/server/loottables/index.md), [tags](https://github.com/NeoForged/Documentation/blob/6d9f16c291474b48032bd633a69695261909a59b/docs/resources/server/tags.md).

## Comprobación específica de las fuentes 26.2.0.88

Rutas dentro del JAR de fuentes oficial, bajo `net/neoforged/neoforge/` salvo las de FML:

| Archivo o grupo | Confirmación útil para trabajos futuros |
|---|---|
| `registries/DeferredRegister.java` | Fábricas, tipos especializados y conexión al bus; no copiar ejemplos antiguos sin revisar su implementación |
| `common/extensions/IBlockExtension.java` | Hook de copia con jugador, retirada por jugador y consulta de reacción de pistón |
| `common/extensions/IItemExtension.java`, `ItemInstanceExtension.java` | Uso inicial y restos sensibles al ítem; aparecen `ItemInstance` e `ItemStackTemplate`, por lo que no se deben portar firmas antiguas a ciegas |
| `common/extensions/IBlockEntityExtension.java` | Recepción con `ValueInput`, `getModelData` y solicitud de actualización del modelo |
| `event/level/block/BreakBlockEvent.java` | Evento cancelable de rotura; revisar las condiciones previas de cancelación al integrar herramientas |
| `client/event/RegisterBlockStateModels.java` | Registro de codecs de modelo de estado y definición personalizada |
| `client/event/RegisterItemModelsEvent.java` | Registro de codecs de modelos de ítem |
| `client/event/ExtractBlockOutlineRenderStateEvent.java`, `client/CustomBlockOutlineRenderer.java` | Extracción de contorno y envío de representación; el renderer no debe capturar el mundo |
| `client/event/RegisterMenuScreensEvent.java` | Asociación de tipos de menú a pantallas exclusivamente de cliente |
| `network/event/RegisterPayloadHandlersEvent.java` | Registro de payloads y su protocolo; el tratamiento predeterminado se lleva al hilo principal |
| `transfer/ResourceHandler.java`, `transfer/transaction/Transaction.java` | Transferencias con commit explícito; los límites del producto siguen siendo cálculos propios |
| `capabilities/RegisterCapabilitiesEvent.java` | Punto de exposición de handlers si un frente futuro lo necesita; no añade automatización como requisito nuevo |
| FML: `net/neoforged/fml/common/Mod.java`, `javafmlmod/FMLModContainer.java` | Entradas por lado físico y constructor con bus inyectado |

Las fuentes y parches no sustituyen una comprobación en ejecución. Los detalles de métodos vanilla no utilizados todavía se deben contrastar al implementar cada integración; esta investigación no asegura que los algoritmos propios, el renderizado o los menús funcionen sin esa etapa.

## Resultado del frente Base del mod

Se conservan las versiones fijadas, se conecta el arranque común a los contenedores vacíos y se separa el punto de entrada de cliente. Se reservan los paquetes y Java vacíos descritos en [el mapa del esqueleto](skeleton.md), sin implementar otros frentes.

Las texturas definitivas de todas las herramientas se generan después de sus funciones. Aquí no se crean modelos, texturas, recetas, catálogo ejecutable ni sistemas de datos. `unpainted.png` no se recrea. Las carpetas vacías no constituyen contenido registrado.

Estado: **esqueleto implementado y revisado estáticamente**. Compilación, arranque de cliente/servidor y comportamiento dentro de Minecraft siguen **sin verificar**. Los commits se crean directamente online en `26.2`, sin commits locales.
