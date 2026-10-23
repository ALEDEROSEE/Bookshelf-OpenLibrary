package com.tecsup.bookshelf.data

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Servicio de Retrofit del Bookshelf. La guía pide Google Books, pero su API ya no atiende
 * peticiones sin clave (responde 429 con cuota 0), así que uso Open Library, que sí es abierta.
 */
interface OpenLibraryService {
    @GET("search.json")
    suspend fun buscar(
        @Query("q") consulta: String,
        @Query("limit") limite: Int = 30,
        @Query("fields") campos: String = "key,title,author_name,first_publish_year,cover_i"
    ): BusquedaResponse

    @GET("works/{id}.json")
    suspend fun obra(@Path("id") id: String): DetalleObra
}
