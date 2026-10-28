# Grupo 3: producción e integración

Implementación para Minecraft 26.2 / NeoForge 26.2.0.88. Incluye estaciones de bloques, molino/prensa, sus interfaces, recetas/mejoras e integración estática. No se ha compilado ni ejecutado Minecraft: los estados de implementación no acreditan verificación en juego.

## Estaciones

Banco, cortadora y ensambladora multiplican el rendimiento manual por 2, 4 y 8 y comparten entre las cinco subdivisiones capacidades de 1024, 4096 y 16 384 bloques. Cada lote consume una piedra central y ocho exteriores iguales; solo se consume si cabe todo el resultado. No requieren tiempo ni combustible.

Las dos primeras ranuras reciben centro y exteriores. La selección de subdivisión y los botones son propios de cada menú/jugador. La interfaz calcula el lote con el balance común; el servidor valida entradas, capacidad, subdivisión y material exterior esperado antes de fabricar. Los botones de retirada entregan 1 o hasta 64 bloques; lo que no quepa permanece almacenado.

Shift + clic sobre bloques nuevos los deposita en los contadores. Solo se acepta el ítem canónico de la subdivisión, sin personalización. Las flechas convierten 1 o hasta 64 unidades del contador hacia la subdivisión seleccionada, sin cambiar el total ni consumir materiales.

Una tercera ranura permite convertir directamente un bloque completo sin pintura que tenga orientación o componentes personalizados. La cuarta recibe una unidad convertida, conservando esos datos y la orientación. Así, las restricciones de contadores no impiden convertir un bloque completo al que se haya quitado la pintura. Recortes y pintura impiden esta operación; no se reconstruyen subcubos perdidos.

## Molino y prensa

Molino y prensa producen respectivamente 4 y 16 ítems de pintura por bloque, en 80 ticks, sin combustible. Usan una entrada y cuatro salidas físicas. Se comprueba el lote completo y cualquier recipiente sobrante antes de consumir la entrada. Si no caben, se espera; no se entregan fracciones ni se pierden materiales.

La nieve en polvo se procesa desde su cubo y devuelve un cubo vacío en las salidas. Los mismos identificadores/restricciones de catálogo alimentan extracción manual y máquinas: no se procesan pinturas exclusivas de creativo ni ingredientes personalizados cuya conversión destruiría datos.

El progreso se guarda con la entrada y las salidas. Se conserva al recoger/colocar la máquina y se pausa si se bloquea la salida. Cambiar o retirar la identidad de entrada reinicia el progreso; no se aplica trabajo de una pintura a otra. La tolva introduce por arriba/laterales y extrae salidas por debajo. Las estaciones conservan control manual de fabricación y conversión; sus entradas admiten interacción de contenedor, pero los contadores no son ranuras de extracción automática.

## Persistencia, recogida y menús

`ProductionState` usa contadores y plantillas de ítems inmutables, con índice de ranura, progreso e identidad en curso. Se valida contra el tipo/capacidad del destino al cargar y colocar. Los datos de mundo inválidos se conservan en bruto y no generan inventarios o drops recuperables.

Un único ítem de estación/máquina conserva inventario, contadores, componentes y progreso. No se dispersan además las entradas/salidas al retirar el bloque. En creativo, romper uno con contenido devuelve ese ítem; uno vacío no produce drop. Ctrl + clic central copia contenido y la copia simple da uno vacío. Se aplica la probabilidad de supervivencia de explosiones al ítem completo y se bloquea el movimiento por pistones.

Las pantallas utilizan botones nativos y fondos sólidos, sin PNG propios. Los materiales/salidas viajan por los slots nativos y los contadores/progreso por datos de menú; las cantidades máximas quedan dentro de su representación de 16 bits. Las acciones comprueban contenedor, entidad, permisos, alcance y selección esperada. La predicción de Shift + clic usa el mismo depósito que el servidor; retirar no utiliza el descarte creativo de excedentes.

El protocolo pasa a **4** por la nueva acción de estación. Cliente y servidor deben actualizarse juntos.

## Recetas y mejoras

La receta manual admite exactamente seis centros y seis niveles exteriores, con sus rendimientos 1/4/8/16/32/64 y resultado 1×1. Las estaciones permiten las cinco subdivisiones. Las recetas especiales son calculadas y no ofrecen autorrelleno de libro de recetas; las recetas normales sí tienen desbloqueos por materiales.

Se incorporan recetas de las ocho herramientas de edición/conservación, cinco pinceles y cinco rasquetas iniciales, cubo de cobre, banco y molino. Los niveles netherite siguen utilizando herrería con plantilla. Las mejoras de estaciones consumen el nivel anterior vacío; molino → prensa también exige vacío, incluyendo progreso. Las mejoras de pincel/cubo retienen componentes y contenido; oro → diamante se rechaza si alguna pintura excede su capacidad individual.

Patrones, cantidades y decisiones: [recetas del grupo 3](group3-recipes.md). No se importa ninguna receta de adjuntos antiguos ni se cambia el balance de producción acordado.

## Representación provisional

Los cinco bloques nuevos tienen modelos cúbicos mediante `minecraft:block/cube_all`, sin asignar texturas. No se genera ni añade ningún PNG para ellos; se utiliza la textura de ausencia del juego hasta definir los recursos. Sus ítems usan el mismo cubo 3D. No se añaden modelos ni texturas de herramientas o cubos de pintura.

## Decisiones adoptadas durante el trabajo

- Dos ranuras de materiales, contadores compartidos y un par independiente de conversión personalizada en estaciones; evita borrar nombres, orientación o componentes al reducir ítems a cantidades.
- Una entrada y cuatro salidas en máquinas, con espacio reservado por el plan para todos los resultados, incluido el cubo vacío. Progreso guardado y reiniciado al cambiar la identidad.
- Automatización sencilla de molino/prensa: entrada arriba/laterales, salida debajo. No se añade energía ni combustible.
- Patrones/cantidades de fabricación ausentes concretados en la tabla de recetas; mejoras con contenido implementadas como recetas especiales para conservar datos.
- Dureza 3,5, resistencia 6 y sonido de piedra para los nuevos bloques de trabajo; pico rápido y sin requisito de pico para conservar el contenido.
- Modelos de cubo sin texturas asignadas, siguiendo la instrucción del usuario. Las pantallas no requieren recursos gráficos nuevos.

## Comprobaciones y límites

Los tres validadores Python solo leen datos/fuentes. Se contrastaron catálogo, JSON y referencias, desbloqueos, formas de recetas, nombres de bloques y ausencia de texturas nuevas. Se revisaron sintaxis e importaciones Java y firmas de contenedores, tickers, plantillas de ítems, menús y recetas con el cliente oficial 26.2/fuentes NeoForge 26.2.0.88. Esto no sustituye una compilación ni comprueba todos los tipos.

`ProductionContractChecks.runAfterRegistries()` prepara comprobaciones de rendimiento/capacidad de lotes, conservación de contadores, salidas llenas, recipiente de nieve en polvo, pinturas de creativo, datos personalizados, codec de progreso e inventario y mejoras inválidas. No se ejecutó: requiere registros disponibles y autorización para construir/ejecutar.

Quedan pendientes pruebas en juego de 80 ticks, tolvas, partidas guardadas, retirada y copia sin doble drop, conversiones personalizadas, inventarios llenos, menús simultáneos y mejoras con contenido. No se ejecutaron Gradle, Java, builds, CI, clientes ni servidores. Publicación directa online en `26.2`, sin commits locales.
