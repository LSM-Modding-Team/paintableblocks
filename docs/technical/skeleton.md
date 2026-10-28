# Mapa del esqueleto Java

El único frente avanzado es **Base del mod**. Los archivos reservados son literalmente vacíos (0 bytes): no declaran clases, no tienen anotaciones, no se cargan y no constituyen implementación de sus funciones. Sus nombres y paquetes son una organización inicial, ajustable sin cambiar las reglas del mod.

Raíz Java: `src/main/java/net/nicomar/paintableblocks/`.

## Archivos activos de la base

| Archivo | Responsabilidad |
|---|---|
| `PaintableBlocks.java` | Entrada común e instalación de contenedores de registro en el bus del mod |
| `registry/ModRegistries.java` | Ocho contenedores vacíos para bloques, ítems, componentes, tipos de bloque, entidades de bloque, menús, tipos y serializadores de recetas |
| `client/PaintableBlocksClient.java` | Entrada separada y restringida al cliente físico, todavía sin eventos de presentación |

## Reservas vacías por responsabilidad

| Archivo relativo | Frente o propósito futuro |
|---|---|
| `balance/PaintingBalance.java` | Catálogo y balance: valores compartidos, aún sin implementar. |
| `design/BlockDesign.java` | Datos de diseño: subdivisión, ocupación, pinturas y orientación. |
| `design/DesignGeometry.java` | Geometría y selección: volumen restante y superficies expuestas. |
| `design/DesignOrientation.java` | Colocación y giro: transformación conjunta de las 24 orientaciones. |
| `operation/DesignOperations.java` | Recorte, limpieza, pintura y giro; cálculo compartido con la vista previa. |
| `operation/CollectionPolicy.java` | Guardado y conservación: reglas de drops, guantes, tenazas y creativo. |
| `interaction/ServerInteractions.java` | Validación de acciones e impactos en el servidor. |
| `paint/PaintCatalog.java` | Catálogo: identidades del CSV; no se ha cargado en esta entrega. |
| `paint/PaintContent.java` | Identidad y unidades reales de pintura en recipientes y pinceles. |
| `block/PaintableBlock.java` | Bloques pintables: propiedades, forma y selección. |
| `block/WorkstationBlock.java` | Banco, cortadora y ensambladora. |
| `block/PaintMachineBlock.java` | Molino y prensa. |
| `block/PaintBucketBlock.java` | Cubos de pintura colocados. |
| `blockentity/PaintableBlockEntity.java` | Diseño persistente y sincronización del bloque pintable. |
| `blockentity/WorkstationBlockEntity.java` | Inventario y contadores de estaciones. |
| `blockentity/PaintMachineBlockEntity.java` | Inventario y proceso sin combustible de máquinas. |
| `blockentity/PaintBucketBlockEntity.java` | Almacenamiento de identidades y cantidades del cubo. |
| `item/PaintableBlockItem.java` | Colocación y representación del diseño conservado. |
| `item/PaintItem.java` | Tipo único de ítem con identidad de pintura. |
| `item/HammerItem.java` | Martillo: retirada de un subcubo. |
| `item/MalletItem.java` | Mazo: primer subcubo por columna. |
| `item/ChiselItem.java` | Cincel: retirada de una capa plana. |
| `item/FaceScraperItem.java` | Raspador: limpieza de un plano y orientación. |
| `item/CleaningSpongeItem.java` | Esponja: limpieza completa. |
| `item/TongsItem.java` | Tenazas: recogida de geometría sin pintura. |
| `item/GlovesItem.java` | Guantes: recogida del diseño completo. |
| `item/RotationWrenchItem.java` | Llave: giros de 90° con validación. |
| `item/ScraperItem.java` | Rasquetas por material: extracción y desgaste. |
| `item/BrushItem.java` | Pinceles por material: carga, selección y aplicación. |
| `item/PaintBucketItem.java` | Cubos por material: contenido portable y mejoras. |
| `inventory/PaintTransfers.java` | Transferencias limitadas por identidad y capacidad. |
| `menu/WorkstationMenu.java` | Menú de estaciones de bloques. |
| `menu/PaintMachineMenu.java` | Menú de molino y prensa. |
| `menu/PaintBucketMenu.java` | Menú de cubos; selección individual por jugador. |
| `production/WorkstationOperations.java` | Fabricación, contadores y conversión 1:1. |
| `production/PaintMachineOperations.java` | Producción de pintura en lotes completos. |
| `recipe/PaintExtractionRecipe.java` | Extracción manual y desgaste de rasqueta. |
| `recipe/UpgradeRecipe.java` | Mejoras con conservación y límites de capacidad. |
| `recipe/CreativePaintSelectionRecipe.java` | Selección de identidad exclusiva de creativo. |
| `network/ModNetworking.java` | Conexiones adicionales solo cuando la interacción nativa no sea suficiente. |
| `client/model/PaintableBlockModel.java` | Representación geométrica del diseño en el mundo. |
| `client/model/DesignItemModel.java` | Representación 3D del diseño conservado en inventario. |
| `client/model/PaintSampleItemModel.java` | Bote 3D de pintura con apariencia del catálogo. |
| `client/selection/SelectionPreview.java` | Extracción de datos para el contorno de la acción prevista. |
| `client/screen/WorkstationScreen.java` | Presentación de cantidades y acciones de estaciones. |
| `client/screen/PaintMachineScreen.java` | Presentación del proceso y salidas de máquinas. |
| `client/screen/PaintBucketScreen.java` | Presentación del contenido y selección del cubo. |

Las variantes por material comparten la reserva de su familia; no se generan clases por pintura o por diseño. Ningún archivo reservado se importa desde la base. No se han implementado componentes, red, algoritmos, inventarios, recetas, recursos visuales ni contenido de catálogo.

## Recursos y configuración

Las carpetas `assets/paintableblocks` y `data/paintableblocks` se conservan mediante `.gitkeep`. Los diccionarios `en_us.json` y `es_es.json` están vacíos. No hay modelos, PNG, recetas, tags o loot tables de contenido. La reserva de una carpeta de texturas no genera ninguna textura.

Se mantiene la configuración Gradle existente: Java 25, Minecraft 26.2, NeoForge 26.2.0.88, ModDevGradle 2.0.148 y wrapper Gradle 9.2.1. No se ha ejecutado ni sincronizado Gradle. El namespace sigue siendo `paintableblocks`, no el `paintingmod` de los adjuntos de referencia.

Las texturas definitivas de todas las herramientas solo se crean después de sus funciones; el mismo orden se aplica a sus modelos definitivos. `unpainted.png` no se recrea y permanece pendiente de integrar desde el original.

## Estado y siguientes alternativas

Base implementada como esqueleto, con revisión estática; compilación, carga de cliente y servidor y comportamiento en juego no verificados. La investigación técnica está en [NeoForge 26.2](neoforge-26.2.md).

Quedan habilitados para elegir como próximo frente el catálogo/balance, los datos de diseño y las preparaciones independientes de ítems o recetas cuyos requisitos estén completos. El diseño con geometría, las herramientas operativas, sus recursos definitivos y las interfaces requieren sus dependencias particulares. Esta entrega no inicia ninguno de esos frentes.
