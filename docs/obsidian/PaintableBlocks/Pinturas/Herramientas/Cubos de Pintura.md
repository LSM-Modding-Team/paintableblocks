# Cubos de pintura

Se colocan en el suelo para cargar pinceles, recibir su pintura sobrante y gestionar su contenido mediante una interfaz.

| Material | Pinturas distintas | Capacidad por pintura en unidades |
|---|---:|---:|
| Cobre | 1 | 262 144 |
| Oro | 4 | 524 288 |
| Diamante | 8 | 262 144 |
| Netherite | 8 | 524 288 |

Los cubos con varias pinturas permiten seleccionar cuál cargar en el pincel. El de diamante prioriza variedad frente a la capacidad individual del de oro; el de netherite ofrece la mayor capacidad total.

## Interacción directa e interfaz

| Acción | Resultado |
|---|---|
| Clic derecho con pincel | Carga la pintura seleccionada hasta el límite del pincel o las existencias |
| Mayús + clic derecho con pincel | Devuelve sobrantes hasta la capacidad libre; el resto queda en el pincel |
| Clic derecho con ítem de pintura | Introduce los ítems completos que quepan de la pila sostenida |
| Clic derecho con mano vacía | Abre la interfaz |

La interfaz permite seleccionar pintura, introducir ítems y consultar cantidades. Se puede retirar pintura en ítems completos de 256 unidades; las fracciones quedan almacenadas. La selección de pintura es individual por jugador, no compartida entre usuarios del cubo.

Si no cabe un ítem completo, no se consume. Una identidad incompatible con el pincel no se transfiere. No se tiran sobrantes al suelo y se explica el motivo cuando una operación no puede continuar.

## Recogida y mejoras

Al romperlos en supervivencia entregan un único ítem con todas las identidades y cantidades. El pico es más rápido, pero no es obligatorio para conservar el contenido. Al colocarlos lo restauran; el tooltip muestra pinturas y cantidades. Los cubos con contenido no se apilan.

En creativo, romper uno con contenido conserva ese ítem; uno vacío no produce drop. Ctrl + clic central copia su contenido. Los pistones no mueven cubos colocados.

Las mejoras conservan contenido y componentes solo si el recipiente de destino admite todas las identidades y cantidades. Oro → diamante requiere comprobar la menor capacidad individual del diamante. Netherite usa herrería con plantilla. Véase [[Recetas]].

Si sobrevive como drop de una explosión, todo el contenido viaja en el ítem y no se suelta por separado.

## Datos y representación

Las cantidades se guardan en unidades de pintura, no en número de ítems: un ítem completo contiene 256 unidades. Capacidad individual y número máximo de identidades son límites independientes; una transferencia debe respetar ambos.

La interfaz y las interacciones directas consultan el mismo estado real; el servidor valida las transferencias. El ítem del cubo es 3D y las texturas propias son 16×16. El seguimiento de recursos y funciones se organiza en [[Plan de Fases]] y [[Modelos y Texturas]].
