# Pintura en creativo

Se utiliza el mismo pincel y la misma selección individual o por área, sin consumir unidades. El pincel debe tener una identidad de pintura seleccionada.

La identidad puede seleccionarse desde un cubo o combinando pincel e ítem de pintura con clic derecho en el inventario, exclusivamente en creativo. Esta selección no crea unidades utilizables en supervivencia.

Al pasar a supervivencia solo permanecen disponibles las unidades reales almacenadas: la selección creativa no se convierte en pintura gratuita.

Las pinturas exclusivas de creativo pueden aplicarse allí. Cambiar de modo no destruye diseños que ya las tengan. Véanse [[Catálogo de Pinturas]], [[Pinceles]] y [[Tipos de Pintado]].

## Separación de selección y unidades reales

La identidad elegida para pintar sin consumo no concede stock. Seleccionar desde un cubo o mediante la combinación creativa de inventario no debe transformar esa selección en unidades transferibles al pasar a supervivencia.

Los ítems de pintura del inventario creativo se distinguen por nombre y apariencia aunque compartan el mismo tipo. Ctrl + clic central sobre un bloque pintable conserva su diseño completo según [[Recolección y Conservación]].

## Integración en el grupo 2

En creativo se sostiene un ítem de pintura en el cursor y se hace clic derecho sobre el pincel del inventario. No se consume el ítem ni se crean unidades. Un pincel con unidades reales de otra pintura debe vaciarse antes. Se conserva además la selección desde cubos.

Esta interacción sustituye la receta creativa: las recetas vanilla no reciben al jugador al calcular coincidencia y resultado; no se publica una receta que pueda utilizarse en supervivencia. La comprobación de modo se realiza donde sí existe el jugador.

## Envolvente con ítems de pintura

Además del pincel, los ítems de pintura tienen una función exclusiva de creativo. Primer clic derecho: esquina inicial ajustada a la celda lógica apuntada. Segundo clic: esquina opuesta, sin exigir igual plano u orientación. Tercer clic sobre un bloque pintable confirma el volumen fijado; Ctrl + clic cancela. La previsualización es el contorno del volumen, no una simulación completa de todos sus materiales.

Se pintan únicamente caras completas coincidentes con los seis límites del volumen. Se incluyen caras ocultas por bloques exteriores y límites entre celdas ocupadas, guardando su pintura aunque no se dibujen. Las caras interiores quedan intactas. No se crean cortes, geometría ni material; los bloques ajenos y huecos permanecen. Una cara inclinada no coincidente con un límite axial no se pinta. Una cara lógica con varios fragmentos solo se incluye si todos pertenecen al mismo límite, para no pintar fuera de la envolvente.

Hasta 2048 posiciones, ocho veces el máximo del área del pincel, y 262144 celdas de borde examinadas. Los extremos se apuntan dentro del alcance habitual, pero se puede alejar del primer extremo mientras siga cargado y válido. La operación comprueba chunks, permisos, identidad del ítem, modo creativo y eventos del lote; no carga chunks ni consume ítems o unidades.

Formato 6 para conservar caras ocultas y lectura de formatos 1–5. No se cambian IDs ni pinturas de mundos existentes. Protocolo 18; actualizar cliente y servidor. Contratos escritos y revisión estática únicamente, sin compilación ni ejecución en Minecraft.
