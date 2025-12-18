package com.example.finalprojectpam.ui.note

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEntryScreen(
	noteId: String,
	viewModel: NoteViewModel,
	onSaveSuccess: () -> Unit
) {
	val uiState by viewModel.entryUiState.collectAsState()
	val coroutineScope = rememberCoroutineScope()

	var expanded by remember { mutableStateOf(false) }

	LaunchedEffect(noteId) {
		viewModel.loadNoteDetail(noteId)
	}

	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text(if (uiState.isEditMode) "Edit Catatan" else "Tambah Catatan Baru") },
				navigationIcon = {
					IconButton(onClick = onSaveSuccess) {
						Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
					}
				},
				actions = {
					if (uiState.isReady) {
						Button(
							onClick = {
								coroutineScope.launch {
									val isSuccess = viewModel.saveNote()
									if (isSuccess) {
										onSaveSuccess() // Ini akan menjalankan navController.popBackStack()
									}
								}
							},
							enabled = uiState.isSaveEnabled && !uiState.isSaving // Tambahkan cek !isSaving
						) {
							if (uiState.isSaving) {
								CircularProgressIndicator(
									modifier = Modifier.size(20.dp),
									strokeWidth = 2.dp,
									color = MaterialTheme.colorScheme.onPrimary
								)
							} else {
								Icon(Icons.Filled.Save, contentDescription = "Simpan")
								Spacer(Modifier.width(4.dp))
								Text("Simpan")
							}
						}
					}
				}
			)
		}
	) { paddingValues ->
		Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
			if (!uiState.isReady) {
				CircularProgressIndicator(Modifier.fillMaxSize().wrapContentSize())
			} else {
				Column(modifier = Modifier.padding(16.dp)) {

					// --- DROPDOWN KATEGORI ---
					Text("Kategori", style = MaterialTheme.typography.labelMedium)
					ExposedDropdownMenuBox(
						expanded = expanded,
						onExpandedChange = { expanded = !expanded },
						modifier = Modifier.fillMaxWidth()
					) {
						OutlinedTextField(
							value = uiState.categories.find { it.id == uiState.categoryId }?.name ?: "Tanpa Kategori",
							onValueChange = {},
							readOnly = true,
							trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
							modifier = Modifier.menuAnchor().fillMaxWidth()
						)

						ExposedDropdownMenu(
							expanded = expanded,
							onDismissRequest = { expanded = false }
						) {
							DropdownMenuItem(
								text = { Text("Tanpa Kategori") },
								onClick = {
									viewModel.updateCategoryId(null)
									expanded = false
								}
							)
							uiState.categories.forEach { category ->
								DropdownMenuItem(
									text = { Text(category.name) },
									onClick = {
										viewModel.updateCategoryId(category.id)
										expanded = false
									}
								)
							}
						}
					}

					Spacer(Modifier.height(16.dp))

					// --- INPUT JUDUL ---
					OutlinedTextField(
						value = uiState.title,
						onValueChange = viewModel::updateTitle,
						label = { Text("Judul Catatan") },
						modifier = Modifier.fillMaxWidth(),
						singleLine = true
					)

					Spacer(Modifier.height(16.dp))

					// --- INPUT KONTEN ---
					OutlinedTextField(
						value = uiState.content,
						onValueChange = viewModel::updateContent,
						label = { Text("Isi Catatan") },
						modifier = Modifier
							.fillMaxWidth()
							.weight(1f),
						singleLine = false
					)

					if (uiState.error != null) {
						Text(
							text = "Error: ${uiState.error}",
							color = MaterialTheme.colorScheme.error,
							modifier = Modifier.padding(top = 8.dp)
						)
					}
				}
			}
		}
	}
}
