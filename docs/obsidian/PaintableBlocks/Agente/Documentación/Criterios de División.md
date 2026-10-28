# Criterios de división

## Unidad de trabajo

Dividir por un resultado observable, con dependencias mínimas y límites concretos. Evitar tareas como «todas las herramientas» o «todo el renderizado». Una tarea debe indicar qué comportamiento habilita y cómo se comprobará, sin exigir cerrar antes sistemas independientes.

Cada unidad registra:

| Campo | Contenido |
|---|---|
| Objetivo | Resultado concreto disponible al terminar |
| Dependencias mínimas | Datos, operaciones o integraciones que necesita |
| Alcance | Comportamientos y recursos incluidos |
| Estado | Pendiente, disponible, en desarrollo, implementada o verificada |
| Criterio de cierre | Condiciones observables para considerarla completa |
| Evidencia | Revisiones realizadas, resultados y límites de lo comprobado |
| Desbloquea | Alternativas que pasan a ser elegibles |

## Función antes que apariencia

Para todas las herramientas, primero se registra el ítem y se implementan sus funciones: interacción, reglas, durabilidad o consumo cuando corresponda, persistencia, selección y mensajes necesarios. Después se crean el modelo 3D y la textura definitiva de 16×16.

Durante la etapa funcional se utiliza una representación provisional identificable, sin generar sus texturas definitivas. La dependencia es individual: terminar la función del martillo habilita sus recursos visuales aunque el cincel aún no esté terminado. Incluye herramientas de recorte, limpieza, conservación, giro, rasquetas y pinceles.

## Ejemplo: martillo

1. Ítem registrado y operación que retira el subcubo apuntado.
2. Integración con selección, validación del servidor, actualización, superficies nuevas y guardado.
3. Vista previa exacta de lo que se retirará.
4. Modelo 3D y textura definitiva, una vez completada la función.
5. Verificación en Minecraft cuando el usuario autorice su ejecución.

Estas unidades no forman una secuencia global para el mod. Entre ellas pueden desarrollarse otros trabajos disponibles. La etapa funcional completa incluye sus integraciones: un recorte visible que no se guarda al descargar el mundo no se considera terminado.

## Cierre y comprobación

Una tarea implementada cumple su alcance en código y recursos y tiene revisiones adecuadas documentadas. Una tarea verificada tiene evidencia explícita del criterio de cierre; las afirmaciones sobre interacción, representación o sincronización dentro del juego requieren comprobación en ejecución.

Las revisiones estáticas pueden comprobar datos, referencias, fórmulas, enlaces, recursos y consistencia de las reglas. No acreditan compilación, visualización ni comportamiento en Minecraft. Los nombres y resultados de comprobaciones deben indicar qué se ejecutó y qué sigue sin comprobarse.

No compilar ni iniciar juego, clientes o servidores sin petición expresa; conocer los efectos de scripts o pruebas antes de ejecutarlos. Si la ejecución no está autorizada, avanzar con código y revisión estática y registrar la limitación, sin declarar verificado el comportamiento en juego.

## Elección del siguiente trabajo

Presentar alternativas realmente disponibles, su resultado y lo que desbloquean. No bloquear el catálogo por geometría ni los cubos por texturas de herramientas ajenas. Una dependencia parcial solo bloquea la integración que la necesita, no todos los componentes preparatorios de ese frente.

A medida que crezca el mod, combinar nuevas funciones, recursos visuales habilitados, interfaces, correcciones y verificación autorizada. Repetir una comprobación solo cuando haya cambios, fallos o dudas que lo justifiquen.

Los commits se crean online en `26.2`, sin commits locales; véase [[Agente]].
