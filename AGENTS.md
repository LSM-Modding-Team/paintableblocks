# Instrucciones para agentes

- Lee este archivo antes de trabajar. Este debe colocarse en la raíz del proyecto.
- Lee **únicamente** las partes de la documentación pedidas por el usuario, a menos de que sea explícitamente pedida la lectura de toda la carpeta.
- Si vas a modificar la documentación, mantén el formato Markdown legible en Obsidian.

## Restricciones de ejecución

- NO compiles, generes builds, ejecutes tareas de construcción ni inicies Minecraft, servidores o clientes de desarrollo, salvo petición explícita del usuario para hacerlo.
- Esta prohibición incluye invocaciones indirectas: tareas de IDE, scripts, CI, comandos de Gradle y pruebas que desencadenen compilación o arranquen el juego. No ejecutes una herramienta de comprobación sin conocer sus efectos.
- Solo se hará lo anterior con la solicitud explícita del usuario.

## Desarrollo y publicación

- Las reglas aceptadas de `docs/obsidian/PaintableBlocks` son requisitos del mod. Consulta la guía `Agente/Agente.md` y solo las páginas pertinentes al trabajo.
- Organiza el desarrollo por dependencias y alternativas elegibles, sin un orden global estricto. Antes de cada bloque de trabajo, explica las alternativas disponibles y lo que desbloquean.
- Para todas las herramientas, implementa primero el ítem y sus funciones. Genera después su modelo y textura definitivos; la dependencia es individual, incluidas rasquetas y pinceles. Durante la etapa funcional usa una representación provisional identificable, sin generar texturas definitivas.
- Las texturas propias son de 16×16 píxeles y los modelos de ítems son 3D.
- Distingue código implementado de comportamiento verificado. Una revisión estática no acredita pruebas dentro de Minecraft.
- Resuelve las decisiones técnicas rutinarias dentro de lo acordado. Consulta solo por reglas de comportamiento ausentes, sin solicitar de nuevo aprobación de decisiones aceptadas.
- Crea los commits directamente en el repositorio online, siempre en la rama `26.2`. No crees commits locales. Comprueba el estado actual de la rama antes de publicar y conserva los cambios ajenos.
