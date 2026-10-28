# Guía del agente

Esta guía organiza la implementación de PaintableBlocks para Minecraft **26.2** y NeoForge **26.2.0.88**. Las reglas funcionales de [[Bloques Pintables]], [[Pinturas]] y [[Recetas]] son requisitos aceptados, no propuestas pendientes de aprobación.

## Forma de trabajo

- Elegir trabajos por dependencias y resultados observables; no seguir una secuencia fija de fases.
- Antes de iniciar un bloque de trabajo, explicar las alternativas disponibles, sus dependencias y qué desbloquea cada una. La elección explícita del usuario tiene prioridad.
- En todas las herramientas, implementar primero el ítem y sus funciones. Solo después abordar su modelo y textura definitivos. Esta dependencia se aplica individualmente, sin esperar a terminar las demás herramientas.
- Por instrucción posterior del usuario, no crear ahora ningún modelo ni textura de herramienta, tampoco provisional. El frente visual se realizará individualmente después de los demás frentes; se admite missing model/texture.
- Mantener separados el estado de implementación y la evidencia de verificación. Una revisión estática no acredita funcionamiento dentro de Minecraft.
- Resolver decisiones técnicas rutinarias dentro de los requisitos aceptados. Consultar únicamente si falta una regla que afecte al comportamiento del mod; no pedir otra aprobación de decisiones ya aceptadas.
- Leer las partes de documentación pertinentes al trabajo, respetando el `AGENTS.md` de la raíz.

## Ejecución y publicación

Los commits se crean **directamente en el repositorio online, únicamente en la rama `26.2`**. No crear commits locales. Comprobar el estado actual de la rama antes de publicar y conservar los cambios ajenos al trabajo.

No compilar, generar builds, ejecutar tareas de construcción ni iniciar Minecraft, clientes o servidores de desarrollo sin petición expresa del usuario. Esto incluye Gradle, tareas de IDE, scripts o pruebas que lo hagan indirectamente. Antes de ejecutar una comprobación, conocer sus efectos. La autorización para programar o publicar no autoriza por sí sola a compilar o iniciar el juego.

## Documentos de trabajo

- [[Plan de Fases]]: mapa de alternativas, dependencias y estados.
- [[Arquitectura]]: responsabilidades, datos y límites entre sistemas.
- [[Criterios de División]]: tamaño de tareas, cierre y verificación.
- [[Decisiones Pendientes]]: reglas aún no concretadas frente a trabajo por ejecutar.
- [[Modelos y Texturas]]: especificaciones visuales y seguimiento por recurso.

La documentación establece el alcance; no declara código implementado ni pruebas realizadas. La primera versión incluye todo el alcance acordado, aunque se construya por partes.
