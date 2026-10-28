# Mapa del código Java

La base del mod fue el primer frente. Después se implementó el grupo 1: catálogo/balance, datos de diseño, geometría/selección, guardado/conservación y representación. El resto mantiene sus reservas vacías.

Raíz: `src/main/java/net/nicomar/paintableblocks/`.

## Nuevos archivos compartidos del grupo 1

- `registry/ModContent.java`: registros y acceso creativo de bloques, datos, pinturas y herramientas de conservación.
- `client/model/DesignMesh.java`: proyección continua compartida por mundo e inventario, con caché por carga de recursos.

## Inventario inicial y estado actual

| Archivo | Responsabilidad | Estado |
|---|---|---|
| `PaintableBlocks.java` | Entrada común e instalación de contenedores de registro en el bus del mod | Código de grupo 1 |
| `registry/ModRegistries.java` | Contenedores para bloques, ítems, componentes, tipos de bloque, entidades de bloque, menús, tipos y serializadores de recetas | Código de grupo 1 |
| `client/PaintableBlocksClient.java` | Entrada de cliente físico y registro de modelos dinámicos | Código de grupo 1 |
| `balance/PaintingBalance.java` | Catálogo y balance: valores compartidos, aún sin implementar. | Código de grupo 1 |
| `design/BlockDesign.java` | Datos de diseño: subdivisión, ocupación, pinturas y orientación. | Código de grupo 1 |
| `design/DesignGeometry.java` | Geometría y selección: volumen restante y superficies expuestas. | Código de grupo 1 |
| `design/DesignOrientation.java` | Colocación y giro: transformación conjunta de las 24 orientaciones. | Código de grupo 1 |
| `operation/DesignOperations.java` | Recorte, limpieza, pintura y giro; cálculo compartido con la vista previa. | Reserva vacía |
| `operation/CollectionPolicy.java` | Guardado y conservación: reglas de drops, guantes, tenazas y creativo. | Código de grupo 1 |
| `interaction/ServerInteractions.java` | Validación de acciones e impactos en el servidor. | Reserva vacía |
| `paint/PaintCatalog.java` | Catálogo: identidades del CSV; no se ha cargado en esta entrega. | Código de grupo 1 |
| `paint/PaintContent.java` | Identidad y unidades reales de pintura en recipientes y pinceles. | Reserva vacía |
| `block/PaintableBlock.java` | Bloques pintables: propiedades, forma y selección. | Código de grupo 1 |
| `block/WorkstationBlock.java` | Banco, cortadora y ensambladora. | Reserva vacía |
| `block/PaintMachineBlock.java` | Molino y prensa. | Reserva vacía |
| `block/PaintBucketBlock.java` | Cubos de pintura colocados. | Reserva vacía |
| `blockentity/PaintableBlockEntity.java` | Diseño persistente y sincronización del bloque pintable. | Código de grupo 1 |
| `blockentity/WorkstationBlockEntity.java` | Inventario y contadores de estaciones. | Reserva vacía |
| `blockentity/PaintMachineBlockEntity.java` | Inventario y proceso sin combustible de máquinas. | Reserva vacía |
| `blockentity/PaintBucketBlockEntity.java` | Almacenamiento de identidades y cantidades del cubo. | Reserva vacía |
| `item/PaintableBlockItem.java` | Colocación y representación del diseño conservado. | Código de grupo 1 |
| `item/PaintItem.java` | Tipo único de ítem con identidad de pintura. | Código de grupo 1 |
| `item/HammerItem.java` | Martillo: retirada de un subcubo. | Reserva vacía |
| `item/MalletItem.java` | Mazo: primer subcubo por columna. | Reserva vacía |
| `item/ChiselItem.java` | Cincel: retirada de una capa plana. | Reserva vacía |
| `item/FaceScraperItem.java` | Raspador: limpieza de un plano y orientación. | Reserva vacía |
| `item/CleaningSpongeItem.java` | Esponja: limpieza completa. | Reserva vacía |
| `item/TongsItem.java` | Tenazas: recogida de geometría sin pintura. | Código de grupo 1 |
| `item/GlovesItem.java` | Guantes: recogida del diseño completo. | Código de grupo 1 |
| `item/RotationWrenchItem.java` | Llave: giros de 90° con validación. | Reserva vacía |
| `item/ScraperItem.java` | Rasquetas por material: extracción y desgaste. | Reserva vacía |
| `item/BrushItem.java` | Pinceles por material: carga, selección y aplicación. | Reserva vacía |
| `item/PaintBucketItem.java` | Cubos por material: contenido portable y mejoras. | Reserva vacía |
| `inventory/PaintTransfers.java` | Transferencias limitadas por identidad y capacidad. | Reserva vacía |
| `menu/WorkstationMenu.java` | Menú de estaciones de bloques. | Reserva vacía |
| `menu/PaintMachineMenu.java` | Menú de molino y prensa. | Reserva vacía |
| `menu/PaintBucketMenu.java` | Menú de cubos; selección individual por jugador. | Reserva vacía |
| `production/WorkstationOperations.java` | Fabricación, contadores y conversión 1:1. | Reserva vacía |
| `production/PaintMachineOperations.java` | Producción de pintura en lotes completos. | Reserva vacía |
| `recipe/PaintExtractionRecipe.java` | Extracción manual y desgaste de rasqueta. | Reserva vacía |
| `recipe/UpgradeRecipe.java` | Mejoras con conservación y límites de capacidad. | Reserva vacía |
| `recipe/CreativePaintSelectionRecipe.java` | Selección de identidad exclusiva de creativo. | Reserva vacía |
| `network/ModNetworking.java` | Conexiones adicionales solo cuando la interacción nativa no sea suficiente. | Reserva vacía |
| `client/model/PaintableBlockModel.java` | Representación geométrica del diseño en el mundo. | Código de grupo 1 |
| `client/model/DesignItemModel.java` | Representación 3D del diseño conservado en inventario. | Código de grupo 1 |
| `client/model/PaintSampleItemModel.java` | Bote 3D de pintura con apariencia del catálogo. | Código de grupo 1 |
| `client/selection/SelectionPreview.java` | Extracción de datos para el contorno de la acción prevista. | Código de grupo 1 |
| `client/screen/WorkstationScreen.java` | Presentación de cantidades y acciones de estaciones. | Reserva vacía |
| `client/screen/PaintMachineScreen.java` | Presentación del proceso y salidas de máquinas. | Reserva vacía |
| `client/screen/PaintBucketScreen.java` | Presentación del contenido y selección del cubo. | Reserva vacía |

Los nombres y paquetes pueden ajustarse sin modificar las reglas del producto. Una reserva vacía no representa una función implementada. Las variantes por material siguen compartiendo las clases de sus familias; no hay clase por pintura o por diseño.

## Evidencia y continuidad

El alcance y las decisiones del grupo 1 están en [su informe](group1.md); el registro histórico permanece en [CHANGELOG](../../CHANGELOG.md). Los scripts de catálogo y validación no compilan ni ejecutan el juego. Las pruebas Java preparadas todavía no se ejecutaron.

No se generaron texturas definitivas de herramientas. La integración del original `unpainted.png` sigue pendiente y se utiliza una apariencia de piedra provisional. Grupo 2, grupo 3 y los recursos finales por herramienta mantienen sus dependencias del plan.
