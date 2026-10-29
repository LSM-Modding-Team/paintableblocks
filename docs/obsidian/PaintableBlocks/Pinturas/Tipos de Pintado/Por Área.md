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

## Integración

La vista previa y la acción efectiva utilizan las mismas reglas para enumerar las superficies y calcular unidades. La confirmación se valida contra la geometría real; no se aplica una selección cancelada por un cambio del diseño.

La pintura resultante se guarda con el diseño y se representa en su ítem cuando se conserva. El desarrollo funcional del pincel incluye esta selección antes de sus recursos definitivos.

## Superficies inclinadas

[[Pintura de Pendientes]] extiende esta selección a planos inclinados del mismo bloque, tomando los fragmentos cuyos centros estén dentro del rectángulo. No amplía el pintado a varios bloques. El límite volumétrico de 256³ pertenece al martillo; la comparación inicial con 256×256 no sustituye la regla vigente de pintura limitada a un bloque y plano.
