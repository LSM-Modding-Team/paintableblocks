# Modelos y texturas

## Instrucción vigente tras el grupo 1

El original `unpainted.png` de 16×16 está integrado sin modificar. Ninguna herramienta debe disponer ahora de modelo o textura, tampoco provisional: se retiraron los recursos de tenazas y guantes. Se acepta missing model/texture, incluidos pinceles, rasquetas, herramientas de edición/giro y cubos. El frente de recursos de herramientas se realizará individualmente al terminar los otros frentes. Esta instrucción sustituye cualquier habilitación anterior de recursos provisionales o de iniciarlos inmediatamente al terminar una herramienta.


## Especificación común

Las texturas propias son de **16×16 píxeles**. Los modelos de ítems son **3D**; los modelos no tienen una resolución en píxeles. El tamaño de textura no determina la subdivisión: 1×1, 2×2, 4×4, 8×8 y 16×16 comparten proyección continua sobre la cara completa.

## Función antes que recursos definitivos

En todas las herramientas se completa primero el ítem funcional: registro, interacción, reglas, consumo o desgaste cuando corresponda, persistencia, selección y mensajes necesarios. Solo después se crea su modelo 3D y textura definitiva de 16×16.

Durante el desarrollo funcional se acepta missing model/texture. No crear recursos provisionales. Las funciones preceden a los recursos y, por instrucción actual del usuario, el frente visual se realizará individualmente al terminar los otros frentes.

## Seguimiento individual

Cada recurso mantiene por separado los estados de función, modelo, textura y verificación. Los valores de seguimiento son pendiente, en desarrollo, implementado o verificado según corresponda; un recurso visual permanece bloqueado mientras su función no esté completa.

Esta tabla establece los recursos a seguir, no declara que se hayan creado. Su estado inicial es pendiente de inspección al iniciar código. En las familias por material, llevar una entrada independiente por variante durante la ejecución, aunque aquí se agrupen para lectura.

| Herramienta o variante | Función previa necesaria | Modelo final | Textura final | Verificación visual |
|---|---|---|---|---|
| Martillo | Retirar un subcubo y previsualizarlo | Pendiente de función | 16×16, pendiente de función | Pendiente |
| Mazo | Retirar primeros subcubos por columna | Pendiente de función | 16×16, pendiente de función | Pendiente |
| Cincel | Retirar capa plana sin atravesar huecos | Pendiente de función | 16×16, pendiente de función | Pendiente |
| Raspador | Limpiar orientación y profundidad seleccionadas | Pendiente de función | 16×16, pendiente de función | Pendiente |
| Esponja de limpieza | Limpiar todo conservando geometría | Pendiente de función | 16×16, pendiente de función | Pendiente |
| Tenazas | Recogida implementada en código; sin prueba en juego | Pendiente; sin modelo | 16×16, pendiente | Pendiente |
| Guantes | Recogida implementada en código; sin prueba en juego | Pendiente; sin modelo | 16×16, pendiente | Pendiente |
| Llave de giro | Girar con vista previa y validar colisión | Pendiente de función | 16×16, pendiente de función | Pendiente |
| Rasqueta de cobblestone | Extracción y 64 usos | Pendiente de función | 16×16, pendiente de función | Pendiente |
| Rasqueta de cobre | Extracción y 128 usos | Pendiente de función | 16×16, pendiente de función | Pendiente |
| Rasqueta de hierro | Extracción y 256 usos | Pendiente de función | 16×16, pendiente de función | Pendiente |
| Rasqueta de oro | Extracción y 128 usos | Pendiente de función | 16×16, pendiente de función | Pendiente |
| Rasqueta de diamante | Extracción y 1024 usos | Pendiente de función | 16×16, pendiente de función | Pendiente |
| Rasqueta de netherite | Extracción, 4096 usos y mejora proporcional | Pendiente de función | 16×16, pendiente de función | Pendiente |
| Pincel de cobblestone | Aplicación y capacidad 1024 | Pendiente de función | 16×16, pendiente de función | Pendiente |
| Pincel de cobre | Aplicación y capacidad 2048 | Pendiente de función | 16×16, pendiente de función | Pendiente |
| Pincel de hierro | Aplicación y capacidad 4096 | Pendiente de función | 16×16, pendiente de función | Pendiente |
| Pincel de oro | Aplicación y capacidad 8192 | Pendiente de función | 16×16, pendiente de función | Pendiente |
| Pincel de diamante | Aplicación y capacidad 16 384 | Pendiente de función | 16×16, pendiente de función | Pendiente |
| Pincel de netherite | Aplicación y capacidad 65 536 | Pendiente de función | 16×16, pendiente de función | Pendiente |

Los cubos, estaciones, máquinas, bloques pintables y el bote de pintura también necesitan recursos 3D de ítem. Sus funciones y apariencias se rigen por sus entradas; no se toma la estética de los adjuntos como un recurso ya aprobado o generado.

## Textura original del bloque

Usar exactamente el `unpainted.png` elegido por el usuario, sin recrearlo ni reinterpretarlo. Es la textura original, no una pintura: no tiene ítem ni entrada en el catálogo.

Se proyecta continuamente sobre la cara completa en las cinco subdivisiones. Las superficies nuevas tras un recorte muestran el fragmento correspondiente. Raspador y esponja restauran esta apariencia. Antes de integrarlo se comprueba el original y su tamaño de 16×16; si difiere, se comunica antes de alterarlo. No se presupone que el archivo ya esté en el repositorio.

## Mundo, inventario y selección

Los ítems conservados muestran su forma y pinturas y las restauran al colocarse. Mundo e inventario deben interpretar el mismo diseño. El bote de pintura 3D muestra la identidad correspondiente.

La vista previa destaca exactamente superficies o subcubos afectados; tenazas y guantes indican qué se conserva y la llave muestra el giro previsto. El pincel muestra el área válida y su coste, excluyendo huecos y cuadrados que no cambian.

Las texturas del catálogo se fragmentan, no se repiten en miniatura. Respetan orientación, estados visuales fijos y animaciones decorativas de [[Texturas y Orientación]]. Los packs de recursos pueden cambiar su apariencia sin cambiar esas reglas.

Una inspección estática de archivos no acredita que el modelo se vea correctamente en juego. Registrar por separado modelo creado, textura creada y resultado de verificación, respetando la autorización de ejecución de [[Agente]].

## Estado tras el grupo 1

Se prepararon los modelos dinámicos del bloque y su ítem conservado, con proyección continua compartida, y el bote 3D de pintura. No han sido visualizados en juego.

El grupo 2 incorpora el original `unpainted.png` de 16×16 sin modificar; deja de usarse la piedra vanilla provisional.

Las funciones de tenazas, guantes y herramientas del grupo 2 están implementadas en código, sin prueba en juego. Se retiran las referencias vanilla provisionales. Ninguna herramienta tiene modelos o texturas por ahora. Véase [Grupo 1](../../../../technical/group1.md).
