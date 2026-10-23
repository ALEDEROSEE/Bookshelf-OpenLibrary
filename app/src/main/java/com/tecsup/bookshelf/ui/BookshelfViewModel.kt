package com.tecsup.bookshelf.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tecsup.bookshelf.data.BooksRepository
import com.tecsup.bookshelf.data.DetalleLibro
import com.tecsup.bookshelf.data.Libro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface UiState {
    data object Cargando : UiState
    data class Exito(val consulta: String, val libros: List<Libro>) : UiState
    data class Error(val mensaje: String) : UiState
}

sealed interface DetalleState {
    data object Ninguno : DetalleState
    data class Cargando(val libro: Libro) : DetalleState
    data class Listo(val libro: Libro, val detalle: DetalleLibro) : DetalleState
    data class Error(val libro: Libro, val mensaje: String) : DetalleState
}

class BookshelfViewModel(private val repo: BooksRepository) : ViewModel() {
    private val _estado = MutableStateFlow<UiState>(UiState.Cargando)
    val estado: StateFlow<UiState> = _estado.asStateFlow()

    private val _detalle = MutableStateFlow<DetalleState>(DetalleState.Ninguno)
    val detalle: StateFlow<DetalleState> = _detalle.asStateFlow()

    private var ultima = "jazz history"

    init {
        buscar(ultima)
    }

    fun buscar(consulta: String) {
        val q = consulta.trim()
        if (q.isEmpty()) return
        ultima = q
        _estado.value = UiState.Cargando
        viewModelScope.launch {
            _estado.value = try {
                UiState.Exito(q, repo.buscar(q))
            } catch (e: Exception) {
                UiState.Error(describir(e))
            }
        }
    }

    fun reintentar() = buscar(ultima)

    fun abrir(libro: Libro) {
        _detalle.value = DetalleState.Cargando(libro)
        viewModelScope.launch {
            _detalle.value = try {
                DetalleState.Listo(libro, repo.detalle(libro.id))
            } catch (e: Exception) {
                DetalleState.Error(libro, describir(e))
            }
        }
    }

    fun cerrar() {
        _detalle.value = DetalleState.Ninguno
    }

    private fun describir(e: Exception): String = when (e) {
        is java.net.UnknownHostException -> "Sin conexión a internet"
        is java.net.SocketTimeoutException -> "El servidor tardó demasiado en responder"
        else -> e.message ?: e.javaClass.simpleName
    }
}
