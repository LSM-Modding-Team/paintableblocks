# PaintableBlocks

Mod en desarrollo para **Minecraft 26.2**, **NeoForge 26.2.0.88** y **Java 25**. El namespace es `paintableblocks` y el paquete Java es `net.nicomar.paintableblocks`.

## Estado actual

Base del mod y grupos **1: bloques y diseños**, **2: herramientas y pinturas** y **3: producción e integración** implementados en código. Hay cinco subdivisiones, 337 pinturas, herramientas de conservación/edición/giro, pinceles, rasquetas, cubos, tres estaciones y dos máquinas de pintura.

Las estaciones fabrican, almacenan y convierten; molino/prensa procesan lotes completos en 80 ticks. Se incorporan recetas y mejoras con conservación de datos. Los recursos de 20 herramientas, cuatro cubos y cinco bloques de producción están implementados: modelos 3D, texturas originales de 16×16 y colisión coincidente. Se validan caras y UV para evitar superficies superpuestas. [Vista técnica y detalles](docs/technical/visual-assets.md).

Detalle: [Grupo 1](docs/technical/group1.md), [Grupo 2](docs/technical/group2.md), [correcciones de interacción](docs/technical/interaction-fixes.md) y [Grupo 3](docs/technical/group3.md). El protocolo es **4**: cliente y servidor deben actualizarse juntos. Implementado no significa probado en juego.

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
