package com.tecsup.bookshelf.data

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

/** Respuesta de https://openlibrary.org/search.json: solo los campos que uso (los pido con `fields=`). */
data class BusquedaResponse(
    @SerializedName("numFound") val numFound: Int,
    @SerializedName("docs") val docs: List<LibroDoc>
)

data class LibroDoc(
    @SerializedName("key") val key: String,
    @SerializedName("title") val title: String?,
    @SerializedName("author_name") val autores: List<String>?,
    @SerializedName("first_publish_year") val anio: Int?,
    @SerializedName("cover_i") val coverId: Long?
)

/**
 * Detalle de una obra (/works/{id}.json). `description` puede venir como texto plano o como un objeto
 * {"type": "...", "value": "..."}, por eso lo leo como JsonElement y lo normalizo aparte.
 */
data class DetalleObra(
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: JsonElement?,
    @SerializedName("subjects") val subjects: List<String>?
)

/** Lo que muestra la cuadrícula. */
data class Libro(
    val id: String,
    val titulo: String,
    val autores: String,
    val anio: Int?,
    val portada: String?
)

data class DetalleLibro(
    val titulo: String,
    val descripcion: String?,
    val temas: List<String>
)

fun LibroDoc.aLibro(): Libro = Libro(
    id = key.substringAfterLast('/'),
    titulo = title?.takeIf { it.isNotBlank() } ?: "Sin título",
    autores = autores?.takeIf { it.isNotEmpty() }?.joinToString(", ") ?: "Autor desconocido",
    anio = anio,
    portada = coverId?.let { "https://covers.openlibrary.org/b/id/$it-M.jpg" }
)

fun DetalleObra.aDetalle(): DetalleLibro {
    val d = description
    val texto = when {
        d == null || d.isJsonNull -> null
        d.isJsonPrimitive -> d.asString
        d.isJsonObject -> d.asJsonObject.get("value")?.asString
        else -> null
    }
    return DetalleLibro(title ?: "Sin título", texto?.trim()?.takeIf { it.isNotEmpty() }, subjects.orEmpty().take(6))
}
