# Modelos y texturas

Las texturas propias serán de **16×16 píxeles**. Los modelos de ítems serán **3D**; no se les impone una resolución en píxeles.

## Apariencia original

Se utiliza exactamente el archivo `unpainted.png` elegido por el usuario, sin recrearlo ni reinterpretarlo. Es la textura original del bloque, no una pintura: no figura en el catálogo ni existe un ítem para aplicarla.

Es común a las cinco subdivisiones y se proyecta de forma continua sobre cada cara completa. Las superficies nuevas expuestas por recortes muestran su fragmento correspondiente. El raspador y la esponja restauran esta apariencia al borrar pintura.

Antes de integrar el archivo original se comprueba que sea 16×16; si tiene otro tamaño, se comunica antes de alterarlo. Esta documentación no presupone que el recurso ya esté integrado.

## Representación del diseño

Los ítems conservados muestran visualmente su geometría y pinturas en el inventario y las restauran al colocarse. La selección anticipa las superficies o los subcubos afectados por cada herramienta; los giros muestran el resultado previsto.

Los ítems de pintura usan un bote 3D que muestra la pintura correspondiente. Los packs de recursos pueden modificar las texturas utilizadas, manteniendo las reglas de fragmentación y orientación de [[Texturas y Orientación]].
