# Bookshelf · puntos adicionales del GLAB-S10 (BPAREJA)

Proyecto «Bookshelf» del codelab de Android Basics con Compose (Unidad 5): busca libros con Retrofit + Gson y los muestra en una cuadrícula vertical con portadas descargadas por Coil (`AsyncImage`).

Programación en Móviles · Tecsup · Benjamin Pareja · 2026-01.

- **No usa Google Books.** Su API respondió `429` con cuota 0 a cualquier petición sin clave, así que uso **Open Library** (`search.json` y `works/{id}.json`), que es abierta. Es solo cambiar el servicio: la interfaz y el repositorio no cambian.
- Repositorio con interfaz (`BooksRepository`) + versión de red, e inyección de dependencias manual (`AppContainer`), como pide el codelab.
- Pruebas con un servicio falso, sin red: `./gradlew testDebugUnitTest` (5 pruebas).
- Estados de carga, éxito y error con botón Reintentar; `GridCells.Adaptive` para que sirva en vertical, horizontal y tabletas; detalle de cada obra.

Video: `demo/GLAB-S10-Bookshelf-demo.mp4`.
