package com.tecsup.bookshelf

import com.google.gson.Gson
import com.tecsup.bookshelf.data.BusquedaResponse
import com.tecsup.bookshelf.data.DetalleObra
import com.tecsup.bookshelf.data.LibroDoc
import com.tecsup.bookshelf.data.NetworkBooksRepository
import com.tecsup.bookshelf.data.OpenLibraryService
import com.tecsup.bookshelf.data.aDetalle
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Servicio falso: no hace ninguna petición real, solo devuelve datos fijos. */
private class ServicioFalso : OpenLibraryService {
    var ultimaConsulta: String? = null

    override suspend fun buscar(consulta: String, limite: Int, campos: String): BusquedaResponse {
        ultimaConsulta = consulta
        return BusquedaResponse(
            2,
            listOf(
                LibroDoc("/works/OL2925893W", "Introduction to jazz history", listOf("Donald D. Megill", "Richard S. Demory"), 1984, 81514),
                LibroDoc("/works/OL1W", "Libro sin portada", null, null, null)
            )
        )
    }

    override suspend fun obra(id: String): DetalleObra =
        Gson().fromJson("""{"title":"Jazz","description":"Texto plano","subjects":["Jazz"]}""", DetalleObra::class.java)
}

class BooksRepositoryTest {

    @Test
    fun `el repositorio convierte la respuesta en libros con portada https`() = runBlocking {
        val servicio = ServicioFalso()
        val libros = NetworkBooksRepository(servicio).buscar("  jazz history ")

        assertEquals("jazz history", servicio.ultimaConsulta)   // se recorta antes de pedir
        assertEquals(2, libros.size)
        assertEquals("OL2925893W", libros[0].id)
        assertEquals("Donald D. Megill, Richard S. Demory", libros[0].autores)
        assertEquals("https://covers.openlibrary.org/b/id/81514-M.jpg", libros[0].portada)
    }

    @Test
    fun `sin autores ni portada la tarjeta usa valores de reemplazo`() = runBlocking {
        val libro = NetworkBooksRepository(ServicioFalso()).buscar("x")[1]
        assertEquals("Autor desconocido", libro.autores)
        assertNull(libro.portada)
        assertNull(libro.anio)
    }

    @Test
    fun `el detalle acepta la descripcion como texto`() = runBlocking {
        val d = NetworkBooksRepository(ServicioFalso()).detalle("OL1W")
        assertEquals("Texto plano", d.descripcion)
    }

    @Test
    fun `el detalle acepta la descripcion como objeto con value`() {
        val json = """{"title":"Jazz","description":{"type":"/type/text","value":"Desde un objeto"}}"""
        val d = Gson().fromJson(json, DetalleObra::class.java).aDetalle()
        assertEquals("Desde un objeto", d.descripcion)
    }

    @Test
    fun `el detalle sin descripcion no rompe`() {
        val d = Gson().fromJson("""{"title":"Jazz"}""", DetalleObra::class.java).aDetalle()
        assertNull(d.descripcion)
        assertEquals(emptyList<String>(), d.temas)
    }
}
