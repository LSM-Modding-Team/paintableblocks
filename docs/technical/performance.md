# Optimización de rendimiento

Cambios preparados para Minecraft 26.2 / NeoForge 26.2.0.88 después de la revisión de diez riesgos de rendimiento. Se mantienen las resoluciones, límites de selección, consumo, precisión de colisión, permisos y conservación de diseños. **Sin compilación ni ejecución Java/Minecraft: no hay mediciones de FPS, memoria real o tiempos de tick.**

## Geometría compartida

`BlockDesign` reutiliza ocupación y clave geométrica validadas al pintar o girar. Solo valida las pinturas cambiadas en esas operaciones; lectura de disco/red conserva validación completa. Las claves y los diseños tienen hash precalculado; se conserva un hash independiente de pinturas para giros. Los árboles ordenados permiten copiar pinturas en orden, sin reinsertarlas mediante búsquedas individuales. La representación serializable se reutiliza mediante una caché global acotada, evitando mantener indefinidamente una segunda lista de pinturas en cada diseño cargado.

La validación de pendientes usa extremos analíticos del plano, sin crear ocho objetos de esquina por celda. `ClipTask` permite preparar geometría y pinturas por etapas; las operaciones volumétricas avanzan en unidades de 128 celdas o pinturas antes de ceder el turno. Las rotaciones, inversas, direcciones y composiciones están precalculadas; rotar índices de celda utiliza aritmética entera.

`WorkCache` comparte cálculos en curso mediante futuros, calcula fuera de los bloqueos y aplica límites de entradas y peso. Entre las entradas antiguas prioriza conservar resultados costosos por unidad de peso. Los pesos son unidades lógicas, **no una garantía de bytes exactos de heap**. `PerformanceMetrics.snapshot()` expone, bajo demanda, entradas, peso, límites, aciertos, fallos, esperas, expulsiones y nanosegundos de cálculo. El registro utiliza referencias débiles, sin retener cachés de recursos descartadas ni realizar sondeos o logs periódicos.

## Cobertura de los diez riesgos

| Riesgo | Solución incorporada |
|---|---|
| Previsualización volumétrica | Caché por región/configuración/geometría/orientación; preparación en ticks, hasta dos piezas y aproximadamente 1 ms por turno, sin reconstruir planes en el evento de dibujo. Descarta entradas obsoletas y muestra preparación o simplificación. |
| Colisiones | Cuadrícula discreta construida una vez y optimización nativa de cajas; cachés compartidas por geometría/orientación. Pendientes conservan muestras de 1/16 y bloques normales su resolución exacta. |
| Contactos | Fronteras agrupadas por dirección, límites preparados, rechazo por cajas 2D y vías completas/vacías. Se reutilizan polígonos, contactos y visibilidad completa. Las firmas de fronteras tienen hash preparado; una frontera llena tiene firma canónica independiente de su subdivisión. |
| Exceso de caras | `SurfaceGroups` une polígonos vecinos coplanares con misma pintura, dirección y tipo de cara cuando su unión es convexa y conserva exactamente el área dentro de tolerancia numérica. La proyección recorta las capas de textura después de fusionar, conservando UV, tintes y orientación. Las celdas editables no cambian. |
| Ray tracing | Polígonos reutilizados e índice de superficies por celda; recorrido de las celdas atravesadas por el rayo, deteniéndose en el impacto más cercano. Los bloques sin pendiente siguen su forma nativa optimizada. |
| Coste de pintar | Comparte geometría validada y cachés de colisión/apoyo/selección; comprueba solo las caras modificadas. La validación externa completa no se omite. |
| Volúmenes grandes | `VolumeCursor` captura las posiciones de entidades de cada chunk, incluidas las pendientes de materializar, y examina una entrada por paso. Evita recorrer individualmente aire y bloques ajenos. La creación, limitada a 16³, sigue comprobando todos sus destinos. Planes puros reutilizados bajo límite global; permisos, inventario, eventos y colisiones del mundo nunca se cachean. |
| Actualizaciones | Conserva formas cuando solo cambia pintura; invalida vecinos solo si cambia su frontera. Agrupa reconstrucciones del cliente por sección, mensajes por bloque y notificaciones generales por posición. Apoyo y colisión se actualizan inmediatamente. Evita duplicar las notificaciones ya realizadas al cambiar `CUT`. |
| Máquinas | Recalcula la posibilidad de producir al cambiar inventarios, con revisión de respaldo cada 20 ticks y comprobación final antes de consumir. Avanzar progreso marca el bloque para guardado sin tratarlo como cambio de inventario ni volver a simular salidas. Máquinas bloqueadas esperan cambios. |
| Guardado y red | Caché de representación persistente; diferencias de celdas/pinturas/orientación/plano para ediciones, con identidad y revisiones. Recuperación completa cuando hay desajustes. Formato binario completo versionado para la red, sin cambiar el formato persistente. |

## Presupuestos y casos menores

- Cachés de polígonos, superficies, índices, formas, apoyo, fronteras, contactos, visibilidad, contornos y mallas compartidas. Límites independientes por número y peso. El presupuesto de planes es global, no una caché grande por jugador.
- El cliente prepara hasta 27 piezas cercanas y dibuja un máximo aproximado de 8192 segmentos de detalle por extracción. Cada pieza guarda hasta 1024 líneas y se indica la simplificación. El contorno global permanece completo; estos límites **no recortan la selección real** ni autorizan una ejecución incompleta.
- Contornos reutilizados y segmentos duplicados eliminados. Las formas de selección de celdas utilizan cuadrículas agrupadas en vez de miles de uniones.
- Selecciones activas indexadas por chunk: invalidar una posición no recorre todos los jugadores. Turnos equitativos entre jugadores; notificaciones generales repartidas entre niveles.
- Actualizaciones incrementales coalescidas: hasta 512 bloques o aproximadamente 2 ms por turno de envío. Los estados anteriores retenidos para calcular diferencias tienen un presupuesto de 131 072 unidades; agotarlo cambia a envío completo, sin perder actualizaciones.
- Notificaciones generales: hasta 2048 posiciones o aproximadamente 2 ms por turno; cambios de colisión/apoyo no esperan esta cola. Cliente: hasta 64 secciones por tick para reconstrucción.
- Estado y feedback de selección se envían/refrescan cuando cambian o con frecuencia limitada. Recuperación cliente mediante bits por sección, sin conservar diseños completos fallidos, con una petición por tick como máximo y reintentos. No descarta diseños cargados todavía desincronizados por un límite arbitrario de entradas.
- Las claves de visibilidad dependen de geometría y fronteras; pintar al vecino no obliga a recalcular contactos. Mundo e inventario comparten la fábrica de mallas de cada generación de recursos.
- Resultados inmutables identificados por su geometría/configuración; vistas obsoletas se eliminan antes de dibujarlas. Las revisiones impiden aplicar diferencias antiguas. No se publica una malla calculada para un diseño como si perteneciera a otro.

Los presupuestos temporales se comprueban entre unidades reanudables. No interrumpen una llamada nativa, la captura del índice de un chunk, la optimización final de una forma o un listener externo a mitad de ejecución. Tampoco garantizan un límite total de memoria del mod: existen datos de mundo cargado y recursos que deben medirse junto con las cachés.

## Compatibilidad y decisiones técnicas

**Protocolo 6**: cliente y servidor se actualizan conjuntamente. `DesignWireCodec` tiene versión binaria 1, con longitudes y datos validados. Persistencia de diseño **2**, con lectura de **1**, permanece sin cambios. No se modifican IDs, recetas, modelos, PNG ni `unpainted.png`.

Ediciones pequeñas usan diferencias; cambios con más de 1024 entradas de pintura, adición de celdas o eliminación de un plano usan estado completo. Se mantiene el paquete nativo completo para cambios de estado, carga inicial y herramientas/comandos vanilla; una actualización nativa evita duplicar la actualización personalizada pendiente de ese bloque.

Las operaciones y recuperación no fuerzan carga de chunks. Cancelar conserva lo ya ejecutado. Los planes reutilizados son puros y sus claves contienen diseño y configuración; el estado del mundo y las protecciones se comprueban al ejecutar. Preparar una vista compleja puede tardar varios ticks; no se reutiliza detalle incompatible con la selección actual.

## Regresiones y medición posterior

`PerformanceContractChecks` se conecta al arnés de diseños y prepara:

- Fusión de bloques completos a seis superficies, conservación de área en pendientes y geometría compartida al pintar/girar.
- Comparación de cada muestra de colisión de 1/16 en las cinco resoluciones y 24 orientaciones.
- Ray tracing por celdas frente a búsqueda exhaustiva con rayos reproducibles.
- Equivalencia de recorte síncrono/reanudable, reutilización de polígonos y lectura/escritura del formato compacto.
- Consumidores concurrentes compartiendo un resultado y respeto de los límites de caché.

**No ejecutado.** La revisión actual analiza sintaxis, imports y recursos; no acredita comprobación completa de tipos o funcionamiento en juego.

Cuando se autorice compilar y ejecutar, comparar la misma escena antes/después con bloques completos repetidos, diseños 16×16 alternados, muchas piezas únicas, pendientes con subdivisiones mezcladas, inventario variado, selecciones 256³ mayormente vacías y densas, varios jugadores y máquinas activas/bloqueadas. Usar un profiler/JFR para tiempo de tick y preparación de fotogramas, asignaciones y heap; medir tráfico y tamaño de guardado por separado. Tomar `PerformanceMetrics.snapshot()` antes/después para explicar reutilización y expulsiones. No fijar objetivos de FPS sin hardware y escena definidos; comprobar también pintura, copia, descarga/guardado, permisos, red y cancelación sin cambios funcionales.
