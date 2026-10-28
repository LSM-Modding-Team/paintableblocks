# Plan de fases

## Mapa de trabajo elegible

La primera versión incluye todo el alcance acordado. «Fases» significa frentes conectados por dependencias, no un orden estricto. El usuario puede elegir entre las alternativas disponibles; al avanzar deben abrirse más opciones.

Antes de iniciar un bloque de trabajo se explican las alternativas, sus dependencias mínimas y lo que desbloquean. No es necesario terminar todo un frente para comenzar otro: pueden prepararse componentes independientes y posponer solo las integraciones bloqueadas.

## Estados

| Estado | Significado |
|---|---|
| Pendiente | No iniciado; comprobar sus dependencias antes de elegirlo |
| Disponible | Tiene las dependencias mínimas resueltas y puede iniciarse |
| En desarrollo | Trabajo activo sobre un objetivo delimitado |
| Implementada | Código o recursos de su alcance completados; registrar comprobaciones y límites |
| Verificada | Criterio de cierre respaldado por evidencia explícita; indicar si es estática o en juego |

La aprobación de esta documentación no marca tareas de código como implementadas. El estado inicial del mapa es de planificación; al comenzar se inspecciona el repositorio para determinar qué ya existe y cuáles pasan a disponibles. No se supone que los adjuntos técnicos acrediten una implementación.

## Frentes y dependencias mínimas

| Frente | Objetivo y criterio de cierre | Depende de | Desbloquea |
|---|---|---|---|
| Base del mod | Registros y estructura para Minecraft 26.2 / NeoForge 26.2.0.88 | Requisitos de plataforma | Ítems, bloques, datos y recetas independientes |
| Catálogo y balance | 337 identidades fieles al CSV y valores comunes de unidades/capacidades | Requisitos acordados; base para integración | Pinturas, rasquetas, pinceles, cubos y producción |
| Datos de diseño | Representar las cinco resoluciones, ocupación, superficies pintadas y orientación | Reglas del diseño; base para integración | Persistencia, operaciones, conversión y conservación |
| Geometría y selección | Colisión real, huecos seleccionables y caras expuestas | Datos de diseño y bloque integrado | Recorte, limpieza, pintura y vista previa |
| Guardado y conservación | Mantener diseños al descargar, recoger, copiar y colocar | Datos de diseño e integración de bloque/ítem | Tenazas, guantes, copia creativa y diseños persistentes |
| Recorte y limpieza | Martillo, mazo, cincel, raspador y esponja con selección exacta | Operaciones, geometría y actualización persistente | Edición funcional y recursos visuales de cada herramienta terminada |
| Colocación y giro | Colocación de escalera y 24 orientaciones sin reflejos | Diseño, geometría y validación | Llave funcional y reutilización de diseños orientados |
| Ítem de pintura y rasquetas | Identidades y extracción con desgaste correcto | Catálogo, balance y recetas acordadas | Producción manual y recursos visuales de rasquetas |
| Pinceles y aplicación | Pintado individual/área con consumo y cancelación correctos | Pinturas, superficies, operaciones y datos de pincel | Pintura funcional y recursos visuales de pinceles |
| Cubos de pintura | Guardar, seleccionar y transferir sin pérdida de unidades | Identidades, cantidades e inventarios | Carga integrada de pinceles, portabilidad y mejoras |
| Estaciones de bloques | Fabricar, almacenar y convertir con límites correctos | Diseño completo, balance e inventarios | Banco, cortadora, ensambladora y mejoras vacías |
| Molino y prensa | Procesar cuatro segundos sin combustible y entregar lotes completos | Pinturas, balance e inventarios | Producción ×4/×16 y mejora a prensa |
| Representación del diseño | Mundo e ítem muestran la misma forma, proyección y orientación | Diseño, geometría y catálogo; recurso base para acabado definitivo | Diseños distinguibles y comprobación visual |
| Recursos de herramientas | Modelo 3D y textura 16×16 por herramienta | Funciones completas de esa herramienta | Acabado visual individual |
| Interfaces | Selección, cantidades y mensajes coherentes con operaciones reales | Datos y operaciones del sistema correspondiente | Gestión completa de estaciones, máquinas y cubos |
| Recetas y mejoras | Patrones especificados y conservación de datos/capacidad | Ingredientes acordados; resolver solo reglas realmente ausentes | Acceso de supervivencia y progresión |
| Verificación e integración | Comprobar criterios y registrar fallos/resultados | Alcance implementado; autorización si requiere construir o jugar | Correcciones concretas y cierre de comportamientos |

Los criterios particulares se toman de las páginas de cada sistema; una fila no obliga a implementar toda una familia en una sola tarea. Se subdivide con [[Criterios de División]].

## Alternativas según el avance

- Al principio: preparar catálogo/balance, datos de diseño y registros mínimos, con integración solo donde sea necesaria.
- Con diseño disponible: elegir persistencia, conversión, cálculo de recortes o conservación; no esperar a todas las texturas.
- Con identidad de pintura: preparar rasquetas, cubos o máquinas; la aplicación del pincel se integra al disponer de superficies.
- Con geometría seleccionable: elegir una herramienta de recorte, limpieza, rotación o pintura.
- Con funciones de una herramienta cerradas: elegir sus recursos definitivos o continuar las funciones de otra herramienta.
- Más adelante: nuevas funciones pendientes, interfaces, recursos visuales habilitados, correcciones o comprobaciones autorizadas.

La variedad depende de las dependencias reales: no inventar alternativas disponibles si falta una base común. Mantener esa base pequeña y explicar por qué bloquea un trabajo concreto.

## Registro de ejecución

Cada tarea elegida usa objetivo, dependencias, estado, criterio de cierre, evidencia y opciones desbloqueadas. Registrar por separado implementación y comprobaciones en ejecución. Un script estático no sustituye pruebas dentro del juego.

No compilar ni iniciar Minecraft sin petición expresa. Publicar commits directamente online en `26.2`, nunca mediante commits locales. Los modelos y texturas se siguen por recurso en [[Modelos y Texturas]].

## Primera ejecución: Base del mod

El usuario eligió únicamente **Base del mod**. Se preparó el esqueleto de arranque común/cliente y los contenedores de registro, sin contenido jugable. Se reservaron archivos Java vacíos para los demás frentes; una reserva no cambia su estado a en desarrollo o implementado.

Estado de esta tarea: **implementada como esqueleto**, con revisión estática y compilación/carga en Minecraft pendientes de verificación. La investigación de APIs cubre las mecánicas previstas, sin ejecutarlas ni implementar sus algoritmos.

Evidencia y alcance: [registro de cambios](../../../../../CHANGELOG.md), [mapa del esqueleto](../../../../technical/skeleton.md) e [investigación NeoForge 26.2](../../../../technical/neoforge-26.2.md).

Próximas alternativas elegibles: catálogo/balance, datos de diseño y preparaciones independientes de ítems o recetas con requisitos completos. Las integraciones de geometría, herramientas, pintura e interfaces siguen requiriendo las dependencias de la tabla. No se inició otro frente en esta entrega.

## Ejecución: grupo 1 — bloques y diseños

El usuario eligió los cinco frentes y autorizó resolver las decisiones faltantes durante el trabajo, informándolas después del commit.

| Frente | Estado de implementación | Evidencia de ejecución |
|---|---|---|
| Catálogo y balance | Código, catálogo y traducciones preparados | Validación estática del CSV y recursos; sin carga del mod |
| Datos de diseño | Datos inmutables y codec versionado preparados | Casos de contrato escritos; Java no ejecutado |
| Geometría y selección | Formas exactas, superficies y conversión de impactos preparadas | Revisión de APIs/sintaxis; sin interacción en juego |
| Guardado y conservación | Componentes, entidad, drops, tenazas, guantes y copia preparados | Sin partida, descarga de mundo ni prueba de sincronización |
| Representación del diseño | Modelos de mundo/ítem y proyección común preparados | Sin visualización en juego; textura original pendiente |

Tenazas y guantes habilitan sus recursos definitivos como tareas individuales; esta ejecución no los genera. Sus referencias visuales son provisionales. Los demás frentes no se marcan implementados por tener una reserva Java.

Las decisiones tomadas y limitaciones están en [Grupo 1](../../../../technical/group1.md) y el detalle de avance en [CHANGELOG](../../../../../CHANGELOG.md). El grupo 2 dispone ahora de catálogo, diseño, selección y conservación como bases; las estaciones y máquinas del grupo 3 siguen requiriendo sus inventarios y operaciones propias.

## Ejecución: grupo 2 — herramientas y pinturas

El usuario eligió este grupo y autorizó resolver decisiones faltantes durante el trabajo, explicándolas después del commit.

| Frente | Código incorporado | Verificación |
|---|---|---|
| Recorte y limpieza | Martillo, mazo, cincel, raspador y esponja; planes y contornos de selección compartidos | Revisión estática; contratos preparados sin ejecutar |
| Colocación y giro | Llave con cuartos de vuelta en 24 orientaciones, inversión con Shift y rechazo de intersecciones con entidades; conserva colocación del grupo 1 | Sin prueba en juego |
| Ítem de pintura y rasquetas | Seis niveles, extracción individual/×8, desgaste y mejora proporcional a netherite | JSON/API/sintaxis revisados; sin crafteo en ejecución |
| Pinceles y aplicación | Seis capacidades, selección por esquinas coplanares, sustitución, coste y cancelación; creativo sin stock gratuito | Sin prueba de red/inventario en ejecución |
| Cubos de pintura | Cuatro capacidades, portabilidad, menú y selección individual, transferencias y mejora a netherite | Validación estática; sin prueba multijugador |

Se incorporó el PNG original y se retiraron los recursos de tenazas y guantes. La instrucción actual deja todas las herramientas sin modelos ni texturas, aceptando missing model/texture. Los recursos se realizarán individualmente al terminar los demás frentes.

Quedan disponibles las estaciones de bloques, molino/prensa, recetas generales y sus interfaces/integraciones del grupo 3. Las recetas de extracción y mejora a netherite de herramientas/cubos ya forman parte del grupo 2; esto no implementa las máquinas. Véanse [Grupo 2](../../../../technical/group2.md) y [CHANGELOG](../../../../../CHANGELOG.md).

## Ejecución: grupo 3 — producción e integración

El usuario eligió las cinco áreas del grupo y autorizó resolver decisiones faltantes durante el trabajo, informándolas después del commit.

| Frente | Implementación | Evidencia |
|---|---|---|
| Estaciones de bloques | Banco/cortadora/ensambladora, lotes, contadores, conversión y portabilidad | Revisión estática; sin partida de prueba |
| Molino y prensa | Procesamiento de 80 ticks, salidas completas, recipiente sobrante, progreso y tolvas | Contratos preparados; no ejecutados |
| Interfaces | Slots, cantidades reales, selección por menú/jugador, acciones y predicción de inventario | Sin prueba multijugador |
| Recetas y mejoras | Manual, herramientas, estaciones, máquinas y conservación de datos/capacidad | JSON y patrones revisados |
| Verificación e integración | Registros, componentes, recetas, menús, ticker, acceso creativo y validadores conectados | Comprobación estática; compilación/carga y pruebas de juego pendientes de autorización |

Los cinco bloques de esta entrega tienen cubo 3D sin texturas asignadas. No se generan recursos visuales de herramientas. Quedan como alternativas el frente individual de recursos de herramientas y las comprobaciones de ejecución cuando se autoricen; las correcciones concretas pueden elegirse según sus resultados.

Evidencia, decisiones y límites: [Grupo 3](../../../../technical/group3.md), [recetas](../../../../technical/group3-recipes.md) y [CHANGELOG](../../../../../CHANGELOG.md).

## Ejecución: recursos visuales completos

Por petición del usuario se completan los recursos de 20 herramientas, cuatro cubos y cinco bloques de producción. Las texturas propias son de 16×16 y los ítems tienen modelos 3D. El PNG original y los diseños dinámicos de bloques/pinturas se mantienen.

Estado: recursos implementados; inspección técnica y validación estática de geometría/colisión realizadas. Comprobación en Minecraft pendiente. Esta petición sustituye las restricciones temporales de recursos de las entregas anteriores.

Las alternativas siguientes son inspección de manos/cámara/recarga, pruebas de integración autorizadas y correcciones según sus resultados. No se declara verificación en juego. Evidencia y decisiones: [recursos visuales](../../../../technical/visual-assets.md).
