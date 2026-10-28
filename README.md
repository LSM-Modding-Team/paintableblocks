# PaintableBlocks

Mod en desarrollo para **Minecraft 26.2**, **NeoForge 26.2.0.88** y **Java 25**. El namespace es `paintableblocks` y el paquete Java es `net.nicomar.paintableblocks`.

## Estado actual

Base del mod y grupos **1: bloques y diseños** y **2: herramientas y pinturas** implementados en código. Hay cinco subdivisiones, 337 pinturas, herramientas de conservación/edición/giro, seis niveles de pinceles y rasquetas y cuatro cubos portátiles con interfaz.

Se integró el original `unpainted.png` de 16×16. **Ninguna herramienta tiene modelos ni texturas**, tampoco provisionales; se acepta missing model/texture hasta su frente visual independiente. Estaciones, máquinas y recetas generales de fabricación corresponden al grupo 3.

El detalle del alcance, decisiones y límites está en [Grupo 1](docs/technical/group1.md) y [Grupo 2](docs/technical/group2.md). Implementado no significa probado en juego.

## Guías

- [Instrucciones del agente](AGENTS.md).
- [Especificación en Obsidian](docs/obsidian/PaintableBlocks/PaintableBlocks.md).
- [Plan de trabajo por dependencias](docs/obsidian/PaintableBlocks/Agente/Documentación/Plan%20de%20Fases.md).
- [Mapa del esqueleto y reservas Java](docs/technical/skeleton.md).
- [Investigación oficial y compatibilidad de NeoForge](docs/technical/neoforge-26.2.md).
- [Avance del grupo 1 y decisiones tomadas](docs/technical/group1.md).
- [Avance del grupo 2 y decisiones tomadas](docs/technical/group2.md).
- [Registro de cambios](CHANGELOG.md).

## Plataforma y comprobaciones

Se mantiene la configuración existente de ModDevGradle 2.0.148 y el wrapper Gradle 9.2.1. NeoForm 26.2-2 confirma Java 25 para la plataforma fijada. La investigación distingue documentación general 26.1, migración 26.2 y fuentes exactas de NeoForge 26.2.0.88.

El código tiene revisión estática y un arnés de contratos Java preparado, todavía sin ejecutar. No se ha ejecutado Gradle, compilado ni iniciado Minecraft; no se acredita carga o funcionamiento en juego. Construcción y ejecución requieren petición expresa del usuario, también desde el IDE o scripts.

Los commits se crean directamente en el repositorio online, siempre en la rama `26.2`, sin commits locales. Véase [la licencia MIT](LICENSE).
