package com.example.finalprojectpam.ui.note

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    viewModel: NoteViewModel,
    onNavigateToDetail: (noteId: String) -> Unit
) {
    val uiState by viewModel.listUiState.collectAsState()

    // Efek ini akan jalan setiap kali layar ini dibuka/muncul kembali
    LaunchedEffect(Unit) {
        viewModel.loadNotes()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daftar Catatan") },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToDetail("new") }) {
                Icon(Icons.Filled.Add, contentDescription = "Tambah Catatan")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Text(
                        text = "Error: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.notes.isEmpty() -> {
                    Text(
                        text = "Belum ada catatan. Tekan + untuk menambah.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(contentPadding = PaddingValues(16.dp)) {
                        // key = { it.id ?: "" } -> Menjaga agar tidak error jika id null
                        items(uiState.notes, key = { it.id ?: "" }) { note ->
                            NoteItem(
                                note = note,
                                onClick = {
                                    // Hanya navigasi jika ID tidak null
                                    note.id?.let { id -> onNavigateToDetail(id) }
                                },
                                onDelete = {
                                    // Hanya hapus jika ID tidak null
                                    note.id?.let { id -> viewModel.deleteNote(id) }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NoteItem(
    note: com.example.finalprojectpam.data.model.Note,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // --- KOLOM KIRI: TEXT ---
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = note.content ?: "", // Handle null content
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2, // Batasi 2 baris biar rapi
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    // REVISI: Gunakan dateCreated (CamelCase) dan take(10) agar aman
                    text = "Dibuat: ${note.dateCreated?.take(10) ?: "-"}",
                    style = MaterialTheme.typography.labelSmall
                )
            }

            // Thumbnail Gambar
            if (note.imageUrl != null) {
                Spacer(Modifier.width(8.dp))
                Image(
                    painter = rememberAsyncImagePainter(note.imageUrl),
                    contentDescription = "Thumbnail",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            // Tombol Delete
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Hapus",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}