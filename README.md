# PaintableBlocks

Mod en desarrollo para **Minecraft 26.2**, **NeoForge 26.2.0.88** y **Java 25**. El namespace es `paintableblocks` y el paquete Java es `net.nicomar.paintableblocks`.

## Estado actual

Base del mod y grupos **1: bloques y diseños**, **2: herramientas y pinturas** y **3: producción e integración** implementados en código. Hay cinco subdivisiones, 373 pinturas (337 originales y 36 acabados de cristal), herramientas de conservación/edición/giro, pinceles, rasquetas, cubos, tres estaciones y dos máquinas de pintura.

Las estaciones fabrican, almacenan y convierten; molino/prensa procesan lotes completos en 80 ticks. Se incorporan recetas y mejoras con conservación de datos. Los recursos de 20 herramientas, cuatro cubos y cinco bloques de producción están implementados: modelos 3D, texturas originales de 16×16 y colisión coincidente. Se validan caras y UV para evitar superficies superpuestas. [Vista técnica y detalles](docs/technical/visual-assets.md).

Detalle: [Grupo 1](docs/technical/group1.md), [Grupo 2](docs/technical/group2.md), [correcciones de interacción](docs/technical/interaction-fixes.md) y [Grupo 3](docs/technical/group3.md). El protocolo es **19**: cliente y servidor deben actualizarse juntos. Implementado no significa probado en juego.

## Ampliaciones acordadas

Se han documentado el recorte volumétrico del martillo, las pendientes y su formador, la pintura de superficies inclinadas y tres pestañas del creativo. **Código y recursos preparados, sin compilación ni pruebas en Minecraft**. Formato de diseño 4, con lectura compatible de diseños 1, 2 y 3; [detalles, controles y decisiones](docs/technical/slopes-and-volumes.md). Véase la [especificación](docs/obsidian/PaintableBlocks/PaintableBlocks.md).

Las pinturas de cristal incorporan acabados vanilla y conectables, bordes por perímetro, contactos entre resoluciones/pendientes, luz y conversión reversible con conservación de datos. [Reglas y decisiones](docs/obsidian/PaintableBlocks/Pinturas/Pinturas%20de%20Cristal.md). Implementación revisada por lectura; sin compilación, scripts ni pruebas ejecutadas en esta entrega.

El formador permite crear dimensiones fraccionarias y desplazar el origen hasta precisión de 1/16, independientemente de la subdivisión editable. La creación continúa siendo exclusiva de creativo; los extremos de cada pieza conservan sus límites al guardarse, girarse y colocarse.

El perfil del formador permite cotas inicial y final independientes (por ejemplo 0,5 → 1 conservando base sólida), perfiles descendentes o planos y un modo vertical de cuña lateral. Disponible en creación creativa y transformación; esta última solo retira material. Código preparado sin prueba de juego.

Una misma pieza puede combinar una zona completa y otra inclinada. El formador permite elegir el extremo completo y su longitud en pasos de hasta 1/16, también en vertical. La actualización conserva diseños previos; cliente y servidor deben actualizarse juntos.

El formador reúne sus controles en una lista desplazable, con una escena de bloques 3D integrada y confirmación fija. El modo de ambos extremos completos delimita la inclinación central; crea piezas 16×16 y transforma piezas alineadas de esa resolución. Los extremos se ajustan independientemente y las paredes de unión son superficies pintables.

Corregida la continuidad del plano entre bloques al normalizar sus coeficientes. La previsualización 3D muestra geometría y pinturas con zoom; se prepara por partes y avisa cuando su detalle es parcial. Las formas guardadas se conservan: los diseños defectuosos anteriores no se reconstruyen automáticamente.

El editor ofrece unión continua o con escalón y cotas libres, movimiento y longitud del tramo inclinado, números decimales/fracciones al pulsar un valor, copia de ajustes durante la sesión, giro por arrastre, vistas predefinidas y comparación antes/después/material retirado. Los errores indican posición y marcan piezas en rojo. Shift + usar al aire con el formador deshace la última operación conservada; no devuelve desgaste y rechaza cambios posteriores o falta de permisos.

El flujo principal del formador se realiza en el mundo: dos puntos sobre cuadrícula 1/16 definen inicio, final, volumen y sentido; otro par de clics reemplaza el perfil y Shift + clic ajusta las uniones. Ctrl + clic o Enter confirma; Esc cancela. F4 intercambia inicio/final, F5 gira el volumen y perfil 90°, F6 cambia el plano conservando los puntos, F7 el lado sólido, F8 el modo creativo y F9 la subdivisión de las piezas creadas. La subdivisión de bloques existentes se conserva, incluidas 1×1 y 2×2, sin exigir conversión a 16×16. La pantalla anterior deja de abrirse automáticamente. F10 permite marcar dos límites laterales independientes a 1/16: solo esa franja lleva pendiente y el resto queda completo y pintable; F11 recupera el ancho completo.

## Rendimiento

Optimización preparada de geometría compartida, mallas, selección volumétrica, producción y sincronización incremental. [Cobertura, presupuestos y límites](docs/technical/performance.md). Revisión estática; sin mediciones ni ejecución en Minecraft.

## Guías

- [Instrucciones del agente](AGENTS.md).
- [Especificación en Obsidian](docs/obsidian/PaintableBlocks/PaintableBlocks.md).
- [Plan de trabajo por dependencias](docs/obsidian/PaintableBlocks/Agente/Documentación/Plan%20de%20Fases.md).
- [Mapa del esqueleto y reservas Java](docs/technical/skeleton.md).
- [Investigación oficial y compatibilidad de NeoForge](docs/technical/neoforge-26.2.md).
- [Avance del grupo 1 y decisiones tomadas](docs/technical/group1.md).
- [Avance del grupo 2 y decisiones tomadas](docs/technical/group2.md).
- [Producción, interfaces e integración](docs/technical/group3.md).
- [Recetas concretadas del grupo 3](docs/technical/group3-recipes.md).
- [Modelos, texturas y vista técnica](docs/technical/visual-assets.md).
- [Registro de cambios](CHANGELOG.md).

## Plataforma y comprobaciones

Se mantiene la configuración existente de ModDevGradle 2.0.148 y el wrapper Gradle 9.2.1. NeoForm 26.2-2 confirma Java 25 para la plataforma fijada. La investigación distingue documentación general 26.1, migración 26.2 y fuentes exactas de NeoForge 26.2.0.88.

El código tiene revisión estática y un arnés de contratos Java preparado, todavía sin ejecutar. No se ha ejecutado Gradle, compilado ni iniciado Minecraft; no se acredita carga o funcionamiento en juego. Construcción y ejecución requieren petición expresa del usuario, también desde el IDE o scripts.

Los commits se crean directamente en el repositorio online, siempre en la rama `26.2`, sin commits locales. Véase [la licencia MIT](LICENSE).

El pincel permite áreas coplanares entre bloques con Ctrl + clic y clic de confirmación; el cristal usa dos clics directamente. La selección respeta las subdivisiones, previsualiza el consumo y conserva huecos. Jade opcional 26.2.10+neoforge muestra el material de la cara apuntada. Dependencia de compilación en Modrinth; no se ha resuelto ni compilado durante esta implementación.

En creativo, los ítems de pintura seleccionan dos esquinas y un tercer clic confirma la envolvente: solo se pintan los seis límites, incluidas caras ocultas, hasta 2048 posiciones. Ctrl + clic cancela. No corta geometría ni pinta caras interiores. Formato 6 con lectura 1–5.

Formador de escaleras: mismo flujo mundial, giro, unión, franja lateral y deshacer del formador de pendientes. F9/RePág aumenta y AvPág reduce el máximo de peldaños (1–256). Las piezas modificadas usan cuadrícula 16×16; no trabaja sobre cortes oblicuos previos. Receta propia, modelo 3D con materiales existentes de 16×16. Código sin prueba en juego.
