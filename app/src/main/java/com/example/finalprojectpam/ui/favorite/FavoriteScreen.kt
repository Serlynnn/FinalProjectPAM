package com.example.finalprojectpam.ui.favorite

// Import yang mungkin perlu ditambahkan
import com.example.finalprojectpam.data.model.Note // Ganti dengan path model 'Note' Anda yang benar

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.isEmpty
import androidx.compose.ui.unit.dp

@Composable
fun FavoriteScreen(
    viewModel: FavoriteViewModel,
    onNavigateBack: () -> Unit
) {
    // --- PERUBAHAN 1: Ambil state UI lengkap dari ViewModel ---
    // Asumsi ViewModel Anda memiliki 'uiState' yang berisi daftar catatan dan status loading/error
    val uiState by viewModel.uiState.collectAsState()
    val favoriteIds by viewModel.favoriteNoteIds.collectAsState()

    // --- PERUBAHAN 2: HAPUS 'val notes = listOf(...)' YANG LAMA ---
    // Data sekarang diambil dari uiState

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
        // --- PERUBAHAN 3: Filter daftar catatan untuk hanya menampilkan yang favorit ---
        val favoriteNotes = uiState.notes.filter { note ->
            favoriteIds.contains(note.id)
        }

        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize()) {
            if (favoriteNotes.isEmpty()) {
                Text(
                    "Belum ada catatan favorit.",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // --- PERUBAHAN 4: Gunakan 'favoriteNotes' (data yang sudah difilter) ---
                    items(favoriteNotes, key = { it.id ?: "" }) { note ->
                        val noteId = note.id ?: return@items
                        val title = note.title

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                            // Aksi klik sekarang hanya pada ikon, bukan seluruh kartu
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                // Jadikan ikon sebagai tombol untuk toggle favorit
                                IconButton(onClick = { viewModel.toggleFavorite(noteId) }) {
                                    Icon(
                                        imageVector = Icons.Filled.Favorite, // Selalu 'Favorite' karena ini layar favorit
                                        contentDescription = "Hapus dari Favorit",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
