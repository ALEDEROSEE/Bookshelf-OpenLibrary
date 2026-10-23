package com.tecsup.bookshelf.data

/** Lo que ve la interfaz de usuario: no sabe si los datos vienen de la red o de un servicio falso. */
interface BooksRepository {
    suspend fun buscar(consulta: String): List<Libro>
    suspend fun detalle(id: String): DetalleLibro
}

class NetworkBooksRepository(private val servicio: OpenLibraryService) : BooksRepository {
    override suspend fun buscar(consulta: String): List<Libro> =
        servicio.buscar(consulta.trim()).docs.map { it.aLibro() }

    override suspend fun detalle(id: String): DetalleLibro = servicio.obra(id).aDetalle()
}
