package com.example.finalprojectpam.ui.favorite

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FavoriteScreen(
    viewModel: FavoriteViewModel,
    onNavigateBack: () -> Unit
) {
    val favoriteIds = viewModel.favoriteNoteIds.collectAsState()

    // Contoh data catatan (noteId, noteTitle)
    val notes = listOf(
        "1" to "Belajar Kotlin",
        "2" to "Belajar Jetpack Compose",
        "3" to "Implementasi Favorite"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catatan Favorit") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("Kembali")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(notes) { note ->
                val (noteId, title) = note
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleFavorite(noteId) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = title)
                        Icon(
                            imageVector = if (favoriteIds.value.contains(noteId)) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favorite Icon"
                        )
                    }
                }
            }
        }
    }
}
