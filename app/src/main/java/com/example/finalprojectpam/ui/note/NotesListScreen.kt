package com.example.finalprojectpam.ui.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    viewModel: NoteViewModel,
    onNavigateToDetail: (noteId: String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.listUiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daftar Catatan (Realtime)") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
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
                    // Tampilkan Loading
                    CircularProgressIndicator(Modifier.fillMaxSize().wrapContentSize())
                }
                uiState.error != null -> {
                    // Tampilkan Error
                    Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                }
                uiState.notes.isEmpty() -> {
                    // Tampilkan Kosong
                    Text("Belum ada catatan. Tekan + untuk menambah.", modifier = Modifier.padding(16.dp))
                }
                else -> {
                    // Tampilkan List Catatan (Realtime)
                    LazyColumn(contentPadding = PaddingValues(8.dp)) {
                        items(uiState.notes, key = { it.id }) { note ->
                            NoteItem(
                                note = note,
                                onClick = { onNavigateToDetail(note.id) },
                                onDelete = { viewModel.deleteNote(note.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NoteItem(note: com.example.finalprojectpam.data.model.Note, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(note.title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    note.content,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Dibuat: ${note.date_created?.substring(0, 10) ?: "-"}", // Format tanggal sederhana
                    style = MaterialTheme.typography.labelSmall
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Hapus")
            }
        }
    }
}