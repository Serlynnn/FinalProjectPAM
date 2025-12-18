package com.example.finalprojectpam.ui.note

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
import androidx.compose.runtime.LaunchedEffect
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

	LaunchedEffect(Unit) {
		viewModel.loadNotes()
	}


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
					LazyColumn(contentPadding = PaddingValues(8.dp)) {
						// PERBAIKAN 1: Handle Key agar tidak null (gunakan elvis operator ?:)
						items(uiState.notes, key = { it.id ?: "unknown" }) { note ->
							NoteItem(
								note = note,
								// PERBAIKAN 2: Cek jika ID null, jangan lakukan apa-apa
								onClick = { note.id?.let { onNavigateToDetail(it) } },
								onDelete = { note.id?.let { viewModel.deleteNote(it) } }
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

				// PERBAIKAN 3: content bisa null, gunakan elvis operator
				Text(
					note.content ?: "",
					style = MaterialTheme.typography.bodySmall,
					maxLines = 1,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)

				// PERBAIKAN 4: Gunakan dateCreated (camelCase), bukan date_created
				// Dan handle null safety
				Text(
					"Dibuat: ${note.dateCreated?.take(10) ?: "-"}",
					style = MaterialTheme.typography.labelSmall
				)
			}
			IconButton(onClick = onDelete) {
				Icon(Icons.Filled.Delete, contentDescription = "Hapus")
			}
		}
	}
}
