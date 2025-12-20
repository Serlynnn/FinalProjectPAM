package com.example.finalprojectpam.ui.favorite

<<<<<<< HEAD
=======
// Import yang mungkin perlu ditambahkan
import com.example.finalprojectpam.data.model.Note // Ganti dengan path model 'Note' Anda yang benar

>>>>>>> local-edit
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
<<<<<<< HEAD
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
=======
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.isEmpty
>>>>>>> local-edit
import androidx.compose.ui.unit.dp

@Composable
fun FavoriteScreen(
    viewModel: FavoriteViewModel,
    onNavigateBack: () -> Unit
) {
<<<<<<< HEAD
    val favoriteIds = viewModel.favoriteNoteIds.collectAsState()

    // Contoh data catatan (noteId, noteTitle)
    val notes = listOf(
        "1" to "Belajar Kotlin",
        "2" to "Belajar Jetpack Compose",
        "3" to "Implementasi Favorite"
    )
=======
    // --- PERUBAHAN 1: Ambil state UI lengkap dari ViewModel ---
    // Asumsi ViewModel Anda memiliki 'uiState' yang berisi daftar catatan dan status loading/error
    val uiState by viewModel.uiState.collectAsState()
    val favoriteIds by viewModel.favoriteNoteIds.collectAsState()

    // --- PERUBAHAN 2: HAPUS 'val notes = listOf(...)' YANG LAMA ---
    // Data sekarang diambil dari uiState
>>>>>>> local-edit

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
<<<<<<< HEAD
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
=======
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
>>>>>>> local-edit
                    }
                }
            }
        }
    }
}
