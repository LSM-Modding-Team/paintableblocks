# PaintableBlocks

Mod en desarrollo para **Minecraft 26.2**, **NeoForge 26.2.0.88** y **Java 25**. El namespace es `paintableblocks` y el paquete Java es `net.nicomar.paintableblocks`.

## Estado actual

El único frente avanzado es **Base del mod**: arranque común, entrada de cliente separada, contenedores de registro sin contenido y estructura reservada para el alcance acordado. Todavía no hay bloques, herramientas ni pinturas jugables.

Los archivos Java reservados para futuros frentes están literalmente vacíos. No se han generado modelos o texturas definitivos. Para todas las herramientas, primero se implementan el ítem y sus funciones y después sus recursos visuales 3D con texturas de 16×16.

## Guías

- [Instrucciones del agente](AGENTS.md).
- [Especificación en Obsidian](docs/obsidian/PaintableBlocks/PaintableBlocks.md).
- [Plan de trabajo por dependencias](docs/obsidian/PaintableBlocks/Agente/Documentación/Plan%20de%20Fases.md).
- [Mapa del esqueleto y reservas Java](docs/technical/skeleton.md).
- [Investigación oficial y compatibilidad de NeoForge](docs/technical/neoforge-26.2.md).
- [Registro de cambios](CHANGELOG.md).

## Plataforma y comprobaciones

Se mantiene la configuración existente de ModDevGradle 2.0.148 y el wrapper Gradle 9.2.1. NeoForm 26.2-2 confirma Java 25 para la plataforma fijada. La investigación distingue documentación general 26.1, migración 26.2 y fuentes exactas de NeoForge 26.2.0.88.

Esta entrega tiene revisión estática. No se ha ejecutado Gradle, compilado ni iniciado Minecraft; no se acredita carga o funcionamiento en juego. Construcción y ejecución requieren petición expresa del usuario, también desde el IDE o scripts.

Los commits se crean directamente en el repositorio online, siempre en la rama `26.2`, sin commits locales. Véase [la licencia MIT](LICENSE).
