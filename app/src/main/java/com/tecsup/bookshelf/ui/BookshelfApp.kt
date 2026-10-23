package com.tecsup.bookshelf.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tecsup.bookshelf.data.Libro

@Composable
fun BookshelfApp(vm: BookshelfViewModel) {
    val estado by vm.estado.collectAsState()
    val detalle by vm.detalle.collectAsState()
    var texto by rememberSaveable { mutableStateOf("jazz history") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Bookshelf", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { pv ->
        Column(Modifier.fillMaxSize().padding(pv)) {
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it },
                label = { Text("Buscar libros") },
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { vm.buscar(texto) }) {
                        Icon(Icons.Outlined.Search, contentDescription = "Buscar")
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { vm.buscar(texto) }),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
            )
            when (val e = estado) {
                UiState.Cargando -> Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(12.dp))
                    Text("Buscando en Open Library…")
                }

                is UiState.Error -> Column(
                    Modifier.fillMaxSize().padding(24.dp), Arrangement.Center, Alignment.CenterHorizontally
                ) {
                    Text("No pude cargar los libros", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(e.mensaje, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = vm::reintentar) { Text("Reintentar") }
                }

                is UiState.Exito ->
                    if (e.libros.isEmpty()) {
                        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("Sin resultados para «${e.consulta}»")
                        }
                    } else {
                        // Adaptive: el número de columnas depende del ancho, sirve igual en teléfono, horizontal y tableta
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 128.dp),
                            contentPadding = PaddingValues(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(e.libros, key = { it.id }) { libro -> TarjetaLibro(libro) { vm.abrir(libro) } }
                        }
                    }
            }
        }
    }

    DetalleDialogo(detalle, vm::cerrar)
}

@Composable
private fun TarjetaLibro(libro: Libro, alTocar: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = alTocar)) {
        Column {
            Box(
                Modifier.fillMaxWidth().aspectRatio(2f / 3f).background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (libro.portada != null) {
                    AsyncImage(
                        model = libro.portada,
                        contentDescription = libro.titulo,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Outlined.MenuBook, contentDescription = null, modifier = Modifier.height(48.dp))
                }
            }
            Column(Modifier.padding(8.dp)) {
                Text(
                    libro.titulo, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold,
                    maxLines = 2, overflow = TextOverflow.Ellipsis
                )
                Text(
                    libro.autores, style = MaterialTheme.typography.bodySmall, maxLines = 1,
                    overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DetalleDialogo(estado: DetalleState, alCerrar: () -> Unit) {
    if (estado is DetalleState.Ninguno) return
    val libro = when (estado) {
        is DetalleState.Cargando -> estado.libro
        is DetalleState.Listo -> estado.libro
        is DetalleState.Error -> estado.libro
        DetalleState.Ninguno -> return
    }
    AlertDialog(
        onDismissRequest = alCerrar,
        title = { Text(libro.titulo) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(libro.autores + (libro.anio?.let { " · $it" } ?: ""), style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                when (estado) {
                    is DetalleState.Cargando -> {
                        CircularProgressIndicator()
                        Text("GET works/${libro.id}.json")
                    }

                    is DetalleState.Listo -> {
                        Text(estado.detalle.descripcion ?: "Esta obra no tiene descripción en Open Library.")
                        if (estado.detalle.temas.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text("Temas: " + estado.detalle.temas.joinToString(", "), style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    is DetalleState.Error -> Text(estado.mensaje, color = MaterialTheme.colorScheme.error)
                    DetalleState.Ninguno -> {}
                }
            }
        },
        confirmButton = { TextButton(onClick = alCerrar) { Text("Cerrar") } }
    )
}
