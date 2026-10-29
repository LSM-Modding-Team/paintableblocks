# PaintableBlocks

Mod para **Minecraft 26.2** con **NeoForge 26.2.0.88**. Permite recortar bloques subdivididos y pintar independientemente sus superficies con texturas decorativas, conservando diseños completos en ítems visibles en el inventario.

Esta es la especificación acordada previa a la creación del código. Sus reglas y el método de desarrollo están aceptados; no acredita funciones implementadas ni pruebas realizadas en Minecraft.

## Sistemas

- [[Bloques Pintables]]: cinco resoluciones, geometría, propiedades, herramientas y conservación.
- [[Pinturas]]: 337 identidades decorativas, consumo, aplicación, recipientes y obtención.
- [[Recetas]]: ingredientes conocidos, rendimientos relacionados y mejoras, con límites de lo no especificado.
- [[Agente]]: arquitectura, desarrollo por dependencias, criterios de cierre y recursos.

## Alcance y método

La primera versión incluirá todo el alcance acordado, desarrollado mediante frentes elegibles, sin un orden global estricto. Para cada herramienta se completa primero el ítem y sus funciones y después sus recursos definitivos.

Los commits se crean directamente online en la rama `26.2`, sin commits locales. Compilar, generar builds o iniciar el juego requiere petición expresa; véanse [[Agente]] y [[Plan de Fases]].

## Ampliaciones aceptadas e implementadas en código

[[Recorte Volumétrico]], [[Pendientes]], [[Formador de Pendientes]], [[Pintura de Pendientes]] y [[Pestañas del Creativo]] definen el nuevo alcance acordado. Código y recursos preparados; sin compilación ni prueba dentro del juego. Los informes técnicos y el README distinguen lo ya implementado de estas ampliaciones.
