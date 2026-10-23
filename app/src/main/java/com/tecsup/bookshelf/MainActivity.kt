package com.tecsup.bookshelf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tecsup.bookshelf.ui.BookshelfApp
import com.tecsup.bookshelf.ui.BookshelfViewModel
import com.tecsup.bookshelf.ui.theme.BookshelfTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val contenedor = (application as BookshelfApplication).container
        setContent {
            BookshelfTheme {
                val vm: BookshelfViewModel = viewModel(
                    factory = viewModelFactory { initializer { BookshelfViewModel(contenedor.repositorio) } }
                )
                BookshelfApp(vm)
            }
        }
    }
}
