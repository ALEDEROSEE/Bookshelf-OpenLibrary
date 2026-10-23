package com.tecsup.bookshelf

import android.app.Application
import com.tecsup.bookshelf.data.BooksRepository
import com.tecsup.bookshelf.data.NetworkBooksRepository
import com.tecsup.bookshelf.data.OpenLibraryService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Inyección de dependencias manual: aquí se decide qué repositorio usa la app. */
class AppContainer {
    private val cliente = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { cadena ->
            // Open Library pide identificar a quien consulta
            cadena.proceed(
                cadena.request().newBuilder().header("User-Agent", "Bookshelf-Lab10/1.0 (laboratorio Tecsup)").build()
            )
        }
        .build()

    private val servicio: OpenLibraryService = Retrofit.Builder()
        .baseUrl("https://openlibrary.org/")
        .client(cliente)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(OpenLibraryService::class.java)

    val repositorio: BooksRepository = NetworkBooksRepository(servicio)
}

class BookshelfApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer()
    }
}
