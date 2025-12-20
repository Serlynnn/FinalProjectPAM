package com.example.finalprojectpam.ui.category

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.* import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.finalprojectpam.data.model.Category
import androidx.lifecycle.viewmodel.compose.viewModel
import android.content.Context
import android.net.Uri
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.request.ImageRequest
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
	viewModel: CategoryViewModel,
	onNavigateBack: () -> Unit
) {

	LaunchedEffect(Unit) {
		viewModel.loadCategories() // Pastikan nama fungsinya sesuai di CategoryViewModel
	}

	val state by viewModel.uiState.collectAsState()
	var showDialog by remember { mutableStateOf(false) }
	var categoryToEdit by remember { mutableStateOf<Category?>(null) }
	var displayError by remember { mutableStateOf<String?>(null) }

	val context = LocalContext.current
	var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

	// LAUNCHER UNTUK MEMILIH GAMBAR DARI GALERI
	val imagePickerLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.GetContent(),
		onResult = { uri: Uri? ->
			selectedImageUri = uri
			// Jika user memilih gambar, tampilkan dialog lagi
			if (uri != null) {
				showDialog = true
			}
			// Jika user membatalkan, dialog tetap tertutup
		}
	)

	// Logika Error Card
	LaunchedEffect(state.error) {
		if (state.error != null) {
			displayError = state.error
			delay(4000L)
			displayError = null
			viewModel.resetErrorState() // Panggil reset error di ViewModel
		}
	}


	Scaffold(
		topBar = {
			CategoryTopAppBar(onNavigateBack = onNavigateBack)
		},
		floatingActionButton = {
			FloatingActionButton(onClick = {
				categoryToEdit = null // Mode Tambah
				selectedImageUri = null // Pastikan URI direset
				showDialog = true
			}) {
				Icon(Icons.Default.Add, contentDescription = "Tambah Kategori")
			}
		}
	) { padding ->
		Box(
			modifier = Modifier
				.padding(padding)
				.fillMaxSize()
		) {

			// Tampilan Loading
			AnimatedVisibility(
				visible = state.isLoading,
				enter = fadeIn(),
				exit = fadeOut(),
				modifier = Modifier.align(Alignment.Center)
			) {
				CircularProgressIndicator()
			}

			// Tampilan Data / Kosong
			AnimatedVisibility(
				visible = !state.isLoading && state.categories.isNotEmpty(),
				enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
				exit = fadeOut()
			) {
				CategoryGrid(
					categories = state.categories,
					noteCounts = state.noteCounts,
					onEdit = { category ->
						categoryToEdit = category
						selectedImageUri = null // Reset URI sebelum edit
						showDialog = true
					},
					onDelete = { categoryId ->
						viewModel.deleteCategory(categoryId)
					}
				)
			}

			AnimatedVisibility(
				visible = !state.isLoading && state.categories.isEmpty(),
				enter = fadeIn(),
				exit = fadeOut(),
				modifier = Modifier.align(Alignment.Center)
			) {
				EmptyState(modifier = Modifier.align(Alignment.Center))
			}


			// Tampilkan Error Card
			displayError?.let { msg ->
				Card(
					colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
					modifier = Modifier
						.fillMaxWidth()
						.padding(16.dp)
						.align(Alignment.BottomCenter)
				) {
					Text(
						text = msg,
						color = MaterialTheme.colorScheme.onErrorContainer,
						modifier = Modifier.padding(12.dp)
					)
				}
			}
		}
	}

	// DIALOG INPUT KATEGORI DAN GAMBAR
	if (showDialog) {
		CategoryDialog(
			category = categoryToEdit,
			selectedImageUri = selectedImageUri,
			onDismiss = {
				showDialog = false
				selectedImageUri = null
				categoryToEdit = null
			},
			onSelectImage = {
				// Tutup dialog, lalu panggil Image Picker
				showDialog = false
				imagePickerLauncher.launch("image/*")
			},
			onDeleteImage = {
				// Hapus URI baru
				selectedImageUri = null
				// Hapus URL lama dari model (untuk update ke null di DB)
				categoryToEdit = categoryToEdit?.copy(imageUrl = null)
				showDialog = true // Buka dialog lagi untuk refresh preview
			},
			onConfirm = { name, bytes ->
				// Logika Konversi URI ke ByteArray

				if (categoryToEdit == null) {
					// Mode TAMBAH: Kirim byte array (bytes) yang baru dipilih
					viewModel.addCategoryWithImage(name, bytes)
				} else {
					// Mode EDIT:
					val currentImageUrl = categoryToEdit?.imageUrl

					// Kirim URL lama (yang bisa null jika dihapus di onDeleteImage)
					// dan bytes baru (jika user memilih gambar baru)
					viewModel.updateCategoryWithImage(categoryToEdit!!.id!!, name, currentImageUrl, bytes)
				}
				showDialog = false
				selectedImageUri = null
				categoryToEdit = null
			}
		)
	}
}


// TopAppBar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryTopAppBar(onNavigateBack: () -> Unit) {
	TopAppBar(
		title = { Text("Kelola Kategori") },
		navigationIcon = {
			IconButton(onClick = onNavigateBack) {
				Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
			}
		},
		colors = TopAppBarDefaults.topAppBarColors(
			containerColor = MaterialTheme.colorScheme.primaryContainer,
			titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
		)
	)
}


// Grid List


@Composable
fun CategoryGrid(
	categories: List<Category>,
	noteCounts: Map<String, Int>,
	onEdit: (Category) -> Unit,
	onDelete: (String) -> Unit
) {
	LazyVerticalGrid(
		columns = GridCells.Fixed(2),
		contentPadding = PaddingValues(16.dp),
		horizontalArrangement = Arrangement.spacedBy(16.dp),
		verticalArrangement = Arrangement.spacedBy(16.dp)
	) {
		items(categories, key = { it.id ?: it.name }) { category ->
			category.id?.let { id ->
				CategoryItem(
					category = category,
					noteCount = noteCounts[id] ?: 0,
					onEdit = { onEdit(category) },
					onDelete = { onDelete(id) }
				)
			}
		}
	}
}

// Item Kategori (Card)


@Composable
fun CategoryItem(
	category: Category,
	noteCount: Int,
	onEdit: () -> Unit,
	onDelete: () -> Unit
) {
	Card(
		shape = RoundedCornerShape(12.dp),
		elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
		modifier = Modifier.fillMaxWidth().clickable { /* TODO: Navigasi ke Catatan */ }
	) {
		Column(
			modifier = Modifier.fillMaxWidth()
		) {
			//  1. AREA GAMBAR (Cover)
			AsyncImage(
				model = ImageRequest.Builder(LocalContext.current)
					.data(category.imageUrl)
					.crossfade(true)
					.build(),
				contentDescription = "Cover Kategori: ${category.name}",
				contentScale = ContentScale.Crop,
				modifier = Modifier
					.fillMaxWidth()
					.height(100.dp)
			)

			//  2. AREA DETAIL DAN APLIKASI
			Column(
				modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
			) {
				// Nama Kategori (Judul Utama)
				Text(
					text = category.name,
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.Bold,
					maxLines = 1
				)

				// Tambahan info (Contoh statis)
				Row(verticalAlignment = Alignment.CenterVertically) {
					Icon(
						Icons.Default.Folder,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.primary,
						modifier = Modifier.size(16.dp)
					)
					Spacer(Modifier.width(4.dp))
					Text(
						text = "$noteCount Catatan",
						style = MaterialTheme.typography.bodySmall,
						color = Color.Gray
					)
				}

				Spacer(Modifier.height(8.dp))

				// Aksi Edit dan Hapus
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.End
				) {
					IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
						Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.secondary)
					}
					IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
						Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
					}
				}
			}
		}
	}
}


// Empty State


@Composable
fun EmptyState(modifier: Modifier = Modifier) {
	Column(
		modifier = modifier
			.fillMaxSize()
			.padding(16.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		Icon(
			Icons.Default.Search,
			contentDescription = null,
			modifier = Modifier.size(64.dp),
			tint = MaterialTheme.colorScheme.onSurfaceVariant
		)
		Spacer(Modifier.height(16.dp))
		Text(
			text = "Belum ada kategori yang ditambahkan.",
			style = MaterialTheme.typography.titleMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)
		Text(
			text = "Gunakan tombol (+) untuk memulai.",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)
	}
}


// Input (Tambah/Edit dengan Gambar)


@Composable
fun CategoryDialog(
	category: Category?,
	selectedImageUri: Uri?,
	onDismiss: () -> Unit,
	onConfirm: (String, ByteArray?) -> Unit,
	onSelectImage: () -> Unit,
	onDeleteImage: () -> Unit
) {
	var text by remember { mutableStateOf(category?.name ?: "") }
	val context = LocalContext.current

	val initialImageUrl = category?.imageUrl
	val imageUrlToDisplay = selectedImageUri?.toString() ?: initialImageUrl

	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text(text = if (category == null) "Tambah Kategori" else "Edit Kategori") },
		text = {
			Column {
				OutlinedTextField(
					value = text,
					onValueChange = { text = it },
					label = { Text("Nama Kategori") },
					singleLine = true,
					modifier = Modifier.fillMaxWidth()
				)
				Spacer(Modifier.height(16.dp))

				// Input Gambar Cover
				Text("Gambar Cover", style = MaterialTheme.typography.titleSmall)

				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(150.dp)
						.clip(RoundedCornerShape(8.dp))
						.background(Color.LightGray.copy(alpha = 0.3f))
						.clickable { onSelectImage() },
					contentAlignment = Alignment.Center
				) {
					if (imageUrlToDisplay.isNullOrBlank()) {
						Icon(Icons.Default.Add, contentDescription = "Pilih Gambar", modifier = Modifier.size(48.dp))
					} else {
						AsyncImage(
							model = ImageRequest.Builder(context)
								.data(imageUrlToDisplay)
								.crossfade(true)
								.build(),
							contentDescription = null,
							contentScale = ContentScale.Crop,
							modifier = Modifier.fillMaxSize()
						)
					}
				}
				Spacer(Modifier.height(8.dp))

				// Tombol Aksi Gambar
				Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
					OutlinedButton(onClick = onSelectImage) { Text(if(imageUrlToDisplay.isNullOrBlank()) "Pilih Gambar" else "Ganti Gambar") }
					if (!imageUrlToDisplay.isNullOrBlank()) {
						OutlinedButton(
							onClick = onDeleteImage,
							colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
						) {
							Text("Hapus Gambar")
						}
					}
				}
			}
		},
		confirmButton = {
			Button(
				onClick = {
					if (text.isNotBlank()) {
						val bytes = selectedImageUri?.let { uri -> getByteArrayFromUri(context, uri) }
						onConfirm(text, bytes)
					}
				},
				enabled = text.isNotBlank()
			) {
				Text("Simpan")
			}
		},
		dismissButton = {
			TextButton(onClick = onDismiss) {
				Text("Batal")
			}
		}
	)
}



fun getByteArrayFromUri(context: Context, uri: Uri): ByteArray? {
	return try {
		context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
	} catch (e: Exception) {
		e.printStackTrace()
		null
	}
}
