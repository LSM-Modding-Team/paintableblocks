# Pintado por área

Se seleccionan dos esquinas dentro del **mismo plano y bloque**:

1. Ctrl + clic derecho fija la primera esquina.
2. Al mover la mira se previsualiza el rectángulo hasta el cuadrado apuntado, mostrando las superficies afectadas y el consumo total.
3. Otro clic derecho fija la segunda esquina y pinta el área.

Solo afecta a superficies existentes en ese plano. Los huecos y las superficies posteriores o de distinta profundidad quedan excluidos.

## Cancelación y errores

- Otro Ctrl + clic derecho cancela la selección.
- Cambiar de ítem, alejarse del bloque o cambiar su geometría cancela la selección.
- Confirmar sobre otro bloque o plano no pinta y conserva la selección para corregirla.
- Si falta pintura, no se ejecuta parcialmente ni se consume nada.

El coste es proporcional a la superficie que cambia: los cuadrados ya pintados con la misma identidad no consumen unidades. Repintar con otra sustituye la anterior al coste habitual. En [[Pintura en Creativo|creativo]] el flujo es el mismo, sin consumo.
