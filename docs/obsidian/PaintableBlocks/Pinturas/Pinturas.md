# Pinturas

Cada pintura representa una textura. Se utiliza un único tipo de ítem con una identidad de pintura guardada en sus datos. Su acabado es únicamente visual: no transmite las propiedades del bloque de origen.

La unidad base corresponde a un cuadrado de un bloque de 16×16. Un ítem de pintura contiene **256 unidades**, suficientes para una cara completa de un bloque, independientemente de su subdivisión.

Aplicar pintura sobre otra la sustituye directamente y consume las unidades correspondientes a la superficie pintada.

- [[Obtención]]
- [[Rasquetas]]
- [[Cubos de Pintura]]
- [[Tipos de Pintado]]
- [[Pinceles]]
- [[Catálogo de Pinturas]]
- [[Texturas y Orientación]]

## Consumo e identidad

Aplicar la misma pintura no consume unidades. En áreas mixtas solo se cobra la superficie que cambia. Si falta pintura, no se ejecuta una aplicación parcial ni se consume nada.

Cada identidad tiene nombre y aspecto propios, pero comparte el tipo de ítem con el resto del catálogo. El estado decorativo de cada pintura es fijo, según [[Texturas y Orientación]].

[[Pintura en Creativo]]

## Apariencia y recursos

Todas las pinturas utilizan las propiedades del bloque pintable: dureza 1,5, resistencia 6 y sonido de piedra. No aportan funciones, luz ni transparencia física. Los estados decorativos son fijos y la orientación compartida se transforma con el diseño.

El bote de pintura será 3D; las texturas propias del mod son 16×16. La textura original `unpainted.png` no es una pintura del catálogo y reaparece al limpiar. Véase [[Modelos y Texturas]].
