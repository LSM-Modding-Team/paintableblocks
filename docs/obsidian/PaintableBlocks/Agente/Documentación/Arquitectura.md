# Arquitectura

La arquitectura separa reglas de diseño, interacción, producción y presentación. Define responsabilidades sin imponer una lista prematura de clases. Las elecciones de organización interna deben servir a las reglas ya aceptadas.

## Responsabilidades

| Sistema | Responsabilidad y datos |
|---|---|
| Diseño del bloque | Subdivisión, ocupación de subcubos, pintura de superficies expuestas y orientación compartida |
| Geometría | Superficies seleccionables, colisión de las piezas restantes y transformación conjunta del diseño |
| Operaciones | Calcular recorte, limpieza, pintura, giro y conservación con sus condiciones y resultados |
| Pinturas | Catálogo definitivo de 337 identidades, unidad base, capacidad, consumo y acceso en creativo |
| Producción y almacenamiento | Recetas, rendimientos, estaciones, máquinas, cubos y transferencias |
| Presentación | Modelos, texturas, interfaces, ítems conservados y vistas previas |
| Guardado y sincronización | Conservar datos al descargar o recoger y publicar actualizaciones a clientes |

## Datos del diseño

Las resoluciones son 1, 2, 4, 8 y 16 subcubos por eje. El diseño debe distinguir ocupación de pintura: puede haber geometría sin pintar y superficies nuevas tras un recorte. Se admiten piezas desconectadas, sin caída. La colisión corresponde a los subcubos existentes, no a su caja envolvente.

La orientación es común a geometría y pinturas. La colocación habitual utiliza la lógica de escaleras; los giros posteriores permiten 24 orientaciones sin reflejos. El mismo diseño debe poder guardarse en el bloque, copiarse o recogerse como ítem y restaurarse al colocar.

Las superficies sin pintura usan `unpainted.png`; no se representa como una identidad adicional de pintura del catálogo. El ítem de pintura comparte tipo y distingue las 337 variantes mediante sus datos de identidad.

## Operaciones y autoridad

La vista previa y la acción real usan las mismas reglas de cálculo. Para una herramienta, el conjunto destacado debe coincidir con las superficies o subcubos que finalmente cambian. La selección no sustituye las comprobaciones necesarias al ejecutar.

El servidor valida las modificaciones; el cliente representa la selección y el resultado previsto. La presentación no decide materiales disponibles, drops, cantidades ni cambios persistentes. Los menús y las transferencias usan el estado real de los inventarios y capacidades.

Una operación rechazada no consume recursos. Pintar exige unidades suficientes para toda la superficie que cambia; fabricar exige espacio para el lote completo. Las transferencias que permiten cantidades parciales mueven solo lo que cabe y conservan el sobrante, según [[Cubos de Pintura]] y [[Fabricación]].

## Unidades y balance compartidos

Capacidad, consumo y rendimiento consultan una única definición de sus valores, sin copiar constantes independientes en interfaces, recetas y lógica.

- Unidad mínima: un cuadrado de 16×16.
- Ítem de pintura: 256 unidades.
- Cuadrado de resolución n: 256 / n² unidades; cara completa: 256.
- Capacidades: [[Pinceles]], [[Cubos de Pintura]], [[Banco]], [[Cortadora]] y [[Ensambladora]].
- Rendimientos: [[Fabricación]] y [[Obtención]].
- Duración: solo las rasquetas se desgastan entre las herramientas definidas; molino y prensa procesan durante cuatro segundos, sin combustible.

## Guardado y representación

El guardado conserva subdivisión, recortes y pinturas; recoger estaciones, máquinas o cubos conserva su contenido en un único ítem, sin soltarlo otra vez por separado. Las mejoras respetan los datos y límites del resultado, y las que exigen una estación vacía no consumen contenido almacenado.

La representación de mundo e inventario debe interpretar el mismo diseño. Las texturas se fragmentan según la posición del cuadrado dentro de la cara completa y conservan la orientación compartida; no se repiten en miniatura. El catálogo aporta apariencia, nunca dureza, resistencia, sonido o funciones.

## Límites del alcance

No simular agua por subcubo ni movimiento por pistones. No añadir ejes de pintura independientes, reflejos, reparación de rasquetas o combustible a la prensa. Los documentos adjuntos anteriores no autorizan importar reglas incompatibles, clases o recetas ausentes en la documentación acordada.

El orden de construcción se decide mediante [[Plan de Fases]], y la evidencia de cierre mediante [[Criterios de División]].

## Ampliaciones aceptadas pendientes de implementación

- Diseño: añadir plano inclinado opcional y superficies de fragmentos, manteniendo ocupación, subdivisión, pinturas y orientación conjunta. Una pieza admite un solo plano; rotarlo no añade cortes nuevos.
- Geometría: selección y render de pendiente real, colisión mediante cajas a 1/16 independiente de resolución, eliminación de superficies internas entre piezas contiguas y límites compartidos sin solapes.
- Operaciones: [[Recorte Volumétrico]] recorre bloques por lotes; una operación activa por jugador y presupuesto global de servidor. No materializar todos los minicubos ni cargar chunks. Las interrupciones conservan lo ya ejecutado y descartan lo pendiente.
- Pintura: mantener costes actuales en cuadrados y aplicar techo(256 × área) por fragmento inclinado. Usar el mismo cálculo en vista previa y servidor.
- Desgaste: el [[Formador de Pendientes]] se suma a las rasquetas como herramienta con durabilidad; un punto por bloque realmente modificado, suficiente para toda la transformación antes de empezar.
- Guardado: versionar el formato para el plano; leer diseños anteriores como piezas sin plano y preparar pruebas de compatibilidad. Conservar plano y pinturas al recoger/copiar/colocar. Las operaciones activas son temporales y no se reanudan al abrir mundos.
- Presentación: tres [[Pestañas del Creativo]], configuración de pendientes y vistas previas simplificadas para volúmenes grandes. No declarar disponible una función solo por documentarla.

Esta ampliación modifica la afirmación anterior de que solo se desgastan las rasquetas y de que toda geometría es un conjunto de cubos completos. Las demás reglas continúan vigentes.

## Implementación de las ampliaciones

Formato 2 con migración desde 1, plano entero normalizado, cara inclinada independiente, selección por polígonos, colisión a 1/16 y protocolo 5 preparados. Ejecución volumétrica con presupuesto compartido y tres pestañas propias. La representación resta contactos completos y parciales entre piezas y mantiene cachés acotadas por geometría. Ver [informe](../../../../technical/slopes-and-volumes.md); sin ejecución Java/Minecraft.
