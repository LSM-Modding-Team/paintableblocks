# Bloques pintables

Bloques propios con geometría editable por subcubos y superficies pintables independientes. Las pinturas cambian solo la apariencia: todos conservan las propiedades físicas del bloque pintable.

## Organización

- [[Tipos]]: 1×1, 2×2, 4×4, 8×8 y 16×16, consumo por cuadrado y conversión.
- [[Reglas Generales]]: propiedades, piezas desconectadas, colisión, huecos y restricciones.
- [[Herramientas]]: recorte, limpieza, conservación y giro con vista previa.
- [[Fabricación]]: costes, rendimientos y almacenes de estaciones.
- [[Colocación y Rotación]]: colocación de escalera y acceso a 24 orientaciones.
- [[Recolección y Conservación]]: drops, tenazas, guantes, copia creativa y guardado.

Las superficies interiores descubiertas por recortes también se pintan y nacen con la textura original `unpainted.png`. Un diseño conservado muestra su forma y pinturas como ítem y las restaura al colocar. El 1×1 no se recorta; las otras cuatro resoluciones sí.

## Ampliaciones aceptadas pendientes de código

- [[Recorte Volumétrico]]: martillo sobre un volumen de hasta 256³ posiciones.
- [[Pendientes]]: superficie inclinada real, cinco subdivisiones y operaciones de hasta 16³ bloques.
- [[Formador de Pendientes]]: transformación por selección y creación configurable en creativo.

El 1×1 mantiene la prohibición de recorte por martillo/mazo/cincel, pero puede transformarse en pendiente mediante el formador.
