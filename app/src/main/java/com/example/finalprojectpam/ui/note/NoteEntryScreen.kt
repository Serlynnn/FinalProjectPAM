package com.example.finalprojectpam.ui.note

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEntryScreen(
    noteId: String,
    viewModel: NoteViewModel,
    onSaveSuccess: () -> Unit
) {
    // 1. Ambil state dari ViewModel
    val uiState by viewModel.entryUiState.collectAsState()
    val context = LocalContext.current

    // 2. State Lokal
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showFullImage by remember { mutableStateOf(false) }

    // 3. Launcher Galeri
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        if (uri != null) {
            viewModel.updateSelectedImage(uri)
        }
    }

    // 4. Load data saat dibuka
    LaunchedEffect(noteId) {
        viewModel.loadNoteDetail(noteId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) "Edit Catatan" else "Tambah Catatan Baru") },
                navigationIcon = {
                    IconButton(onClick = onSaveSuccess) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Gunakan ini
                            contentDescription = "Kembali"
                        )
                    }
                },
                actions = {
                    if (uiState.isReady) {
                        Button(
                            onClick = {
                                if (uiState.isSaveEnabled) {
                                    viewModel.saveNote(context) { success ->
                                        if (success) onSaveSuccess()
                                    }
                                }
                            },
                            enabled = uiState.isSaveEnabled
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
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
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // --- AREA GAMBAR ---
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clickable { showFullImage = true }, // Klik untuk Full Screen
                        shape = MaterialTheme.shapes.medium,
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            val imagePainter = when {
                                selectedImageUri != null -> rememberAsyncImagePainter(selectedImageUri)
                                uiState.existingImageUrl != null -> rememberAsyncImagePainter(uiState.existingImageUrl)
                                else -> null
                            }

                            if (imagePainter != null) {
                                Image(
                                    painter = imagePainter,
                                    contentDescription = "Note Image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text("Gambar catatan belum tersedia", color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = { launcher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Image, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Pilih Gambar")
                    }

                    Spacer(Modifier.height(16.dp))

                    // --- INPUT TEXT ---
                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = viewModel::updateTitle,
                        label = { Text("Judul Catatan") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = uiState.content,
                        onValueChange = viewModel::updateContent,
                        label = { Text("Isi Catatan") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
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

    // Full Screen Image
    if (showFullImage && (selectedImageUri != null || uiState.existingImageUrl != null)) {
        Dialog(onDismissRequest = { showFullImage = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.large,
                color = Color.Black
            ) {
                Box(contentAlignment = Alignment.TopEnd) {
                    Image(
                        painter = rememberAsyncImagePainter(selectedImageUri ?: uiState.existingImageUrl),
                        contentDescription = "Full Image Preview",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentScale = ContentScale.Fit
                    )

                    IconButton(
                        onClick = { showFullImage = false },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }
        }
    }
}