# PaintableBlocks

Mod en desarrollo para **Minecraft 26.2**, **NeoForge 26.2.0.88** y **Java 25**. El namespace es `paintableblocks` y el paquete Java es `net.nicomar.paintableblocks`.

## Estado actual

Base del mod y **grupo 1: bloques y diseños** implementados en código: catálogo/balance, diseño inmutable, geometría, guardado/conservación y representación de mundo e inventario.

Hay cinco variantes de bloque, 337 identidades de pintura y herramientas funcionales de conservación (tenazas y guantes). Los demás frentes siguen pendientes. El detalle del alcance, decisiones y límites está en [Grupo 1](docs/technical/group1.md).

La textura original `unpainted.png` aún no está disponible: la apariencia sin pintar usa piedra vanilla de forma provisional, sin recrear el PNG. Tenazas y guantes tienen referencias visuales provisionales; sus recursos definitivos serán un trabajo individual posterior a sus funciones.

## Guías

- [Instrucciones del agente](AGENTS.md).
- [Especificación en Obsidian](docs/obsidian/PaintableBlocks/PaintableBlocks.md).
- [Plan de trabajo por dependencias](docs/obsidian/PaintableBlocks/Agente/Documentación/Plan%20de%20Fases.md).
- [Mapa del esqueleto y reservas Java](docs/technical/skeleton.md).
- [Investigación oficial y compatibilidad de NeoForge](docs/technical/neoforge-26.2.md).
- [Avance del grupo 1 y decisiones tomadas](docs/technical/group1.md).
- [Registro de cambios](CHANGELOG.md).

## Plataforma y comprobaciones

Se mantiene la configuración existente de ModDevGradle 2.0.148 y el wrapper Gradle 9.2.1. NeoForm 26.2-2 confirma Java 25 para la plataforma fijada. La investigación distingue documentación general 26.1, migración 26.2 y fuentes exactas de NeoForge 26.2.0.88.

El código tiene revisión estática y un arnés de contratos Java preparado, todavía sin ejecutar. No se ha ejecutado Gradle, compilado ni iniciado Minecraft; no se acredita carga o funcionamiento en juego. Construcción y ejecución requieren petición expresa del usuario, también desde el IDE o scripts.

Los commits se crean directamente en el repositorio online, siempre en la rama `26.2`, sin commits locales. Véase [la licencia MIT](LICENSE).
