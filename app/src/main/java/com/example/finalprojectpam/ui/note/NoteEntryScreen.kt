package com.example.finalprojectpam.ui.notes

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

    // 1. Load data saat composable pertama kali dibuka
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
                    // Tombol Save
                    if (uiState.isReady) {
                        Button(
                            onClick = {
                                if (uiState.isSaveEnabled) {
                                    launch {
                                        if (viewModel.saveNote()) {
                                            onSaveSuccess()
                                        }
                                    }
                                }
                            },
                            enabled = uiState.isSaveEnabled
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
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
            if (!uiState.isReady && !uiState.isSaving) {
                // Tampilkan loading saat loading detail atau pertama kali masuk
                CircularProgressIndicator(Modifier.fillMaxSize().wrapContentSize())
            } else {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Input Judul
                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = viewModel::updateTitle,
                        label = { Text("Judul Catatan") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    // Input Konten
                    OutlinedTextField(
                        value = uiState.content,
                        onValueChange = viewModel::updateContent,
                        label = { Text("Isi Catatan") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f), // Agar mengisi sisa ruang
                        singleLine = false
                    )

                    // Tampilkan Error (jika ada)
                    if (uiState.error != null) {
                        Text(
                            text = "Gagal Simpan: ${uiState.error}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}