# Pinceles

Almacenan una única pintura y la aplican exclusivamente sobre bloques pintables. Consumen unidades proporcionales a la superficie pintada y no tienen durabilidad.

| Material | Capacidad en unidades |
|---|---:|
| Cobblestone | 1024 |
| Cobre | 2048 |
| Hierro | 4096 |
| Oro | 8192 |
| Diamante | 16 384 |
| Netherite | 65 536 |

Se cargan desde un [[Cubos de Pintura|cubo de pintura]] y pueden devolverle el sobrante.

Permiten pintar un cuadrado con clic derecho o iniciar una selección con Ctrl + clic derecho y confirmarla con otro clic derecho. Véanse [[Individual]] y [[Por Área]].

## Carga, devolución y mejoras

Clic derecho sobre un cubo carga la pintura seleccionada hasta llenar el pincel o agotar existencias. Mayús + clic derecho devuelve el sobrante: se transfiere solo lo que admita el cubo y el resto permanece en el pincel.

Si el pincel contiene pintura, solo acepta la misma identidad. Debe devolver el sobrante antes de cargar otra; una selección incompatible muestra un aviso sin transferir nada.

Las mejoras conservan pintura y componentes si el resultado tiene capacidad suficiente. La mejora a netherite usa herrería y plantilla. Los ingredientes generales están en [[Recetas]].

## Aplicación

Aplicar la misma pintura no consume unidades. Un área cobra únicamente los cuadrados que cambian. Si no hay suficiente para toda la aplicación, no pinta parcialmente ni consume nada. Repintar con otra identidad sustituye la anterior con el consumo normal.

La selección se cancela al cambiar de ítem, alejarse del bloque o cambiar su geometría. Los controles completos están en [[Por Área]]. En [[Pintura en Creativo|creativo]] se selecciona una identidad y se pinta sin consumir unidades.
