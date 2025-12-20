package com.example.finalprojectpam.ui.note

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
	viewModel: NoteViewModel,
	onNavigateToDetail: (noteId: String) -> Unit,
	onNavigateBack: () -> Unit
) {

	// Menggunakan 'uiState' dari ViewModel yang sudah diperbarui
	val uiState by viewModel.uiState.collectAsState()

	// Baris ini sekarang akan berfungsi karena ViewModel sudah punya 'favoriteIds'
	val favoriteIds by viewModel.favoriteIds.collectAsState()

	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text("Daftar Catatan") },
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
				uiState.isLoading && uiState.notes.isEmpty() -> {
					CircularProgressIndicator(Modifier.align(Alignment.Center))
				}
				uiState.error != null -> {
					Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
				}
				uiState.notes.isEmpty() -> {
					Text("Belum ada catatan. Tekan + untuk menambah.", modifier = Modifier.align(Alignment.Center).padding(16.dp))
				}
				else -> {
					LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
						items(uiState.notes, key = { it.id ?: "unknown" }) { note ->
							// Panggil NoteItem dengan semua parameter yang dibutuhkan
							NoteItem(
								note = note,
								isFavorite = favoriteIds.contains(note.id),
								onClick = { note.id?.let { onNavigateToDetail(it) } },
								onDelete = { note.id?.let { viewModel.deleteNote(it) } },
								onToggleFavorite = { note.id?.let { viewModel.toggleFavorite(it) } }
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
	isFavorite: Boolean,         // Parameter untuk status favorit
	onClick: () -> Unit,
	onDelete: () -> Unit,
	onToggleFavorite: () -> Unit // Parameter untuk aksi klik favorit
) {
	Card(
		modifier = Modifier
			.fillMaxWidth()
			.clickable(onClick = onClick),
		elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
	) {
		Row(
			modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			// Kolom untuk Teks
			Column(modifier = Modifier.weight(1f)) {
				Text(note.title, style = MaterialTheme.typography.titleMedium)
				Spacer(Modifier.height(4.dp))
				Text(
					note.content ?: "",
					style = MaterialTheme.typography.bodySmall,
					maxLines = 2,
					overflow = TextOverflow.Ellipsis,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
				Spacer(Modifier.height(8.dp))
				Text(
					"Dibuat: ${note.dateCreated?.take(10) ?: "-"}",
					style = MaterialTheme.typography.labelSmall
				)
			}

			// Kolom untuk Tombol Aksi (Favorit dan Hapus)
			Row {
				// Tombol Favorit
				IconButton(onClick = onToggleFavorite) {
					Icon(
						imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
						contentDescription = "Toggle Favorite",
						tint = if (isFavorite) MaterialTheme.colorScheme.primary else Color.Gray
					)
				}
				// Tombol Hapus
				IconButton(onClick = onDelete) {
					Icon(Icons.Filled.Delete, contentDescription = "Hapus")
				}
			}
		}
	}
}
