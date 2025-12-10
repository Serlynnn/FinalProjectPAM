package com.example.finalprojectpam.ui.category

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Folder // Ikon Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.finalprojectpam.data.model.Category
import androidx.lifecycle.viewmodel.compose.viewModel // Diperlukan jika Anda memanggil viewModel() di sini

// Pastikan Anda sudah membuat CategoryViewModelFactory!

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
	viewModel: CategoryViewModel,
	onNavigateBack: () -> Unit
) {
	val state by viewModel.uiState.collectAsState()
	var showDialog by remember { mutableStateOf(false) }
	var categoryToEdit by remember { mutableStateOf<Category?>(null) }

	// ⭐ BARU: State untuk menampung error yang akan ditampilkan
	var displayError by remember { mutableStateOf<String?>(null) }

	Scaffold(
		topBar = {
			CategoryTopAppBar(onNavigateBack = onNavigateBack)
		},
		floatingActionButton = {
			FloatingActionButton(onClick = {
				categoryToEdit = null // Mode Tambah
				showDialog = true
			}) {
				// Asumsi ikon Add sudah terimpor
				Icon(Icons.Default.Add, contentDescription = "Tambah Kategori")
			}
		}
	) { padding ->
		Box(
			modifier = Modifier
				.padding(padding)
				.fillMaxSize()
		) {

			// Tampilan Loading (Animasi Fade)
			AnimatedVisibility(
				visible = state.isLoading,
				enter = fadeIn(),
				exit = fadeOut(),
				modifier = Modifier.align(Alignment.Center)
			) {
				CircularProgressIndicator()
			}

			// Tampilan Data / Kosong (Animasi Fade dan Slide)
			AnimatedVisibility(
				visible = state.isLoading,
				enter = fadeIn(),
				exit = fadeOut(),
				modifier = Modifier.align(Alignment.Center)
			) {
				CircularProgressIndicator()
			}

			AnimatedVisibility(
				visible = !state.isLoading,
				enter = fadeIn() + slideInVertically(),
				exit = fadeOut()
			) {
				if (state.categories.isEmpty()) {
					EmptyState(modifier = Modifier.align(Alignment.Center))
				} else {
					CategoryGrid(
						categories = state.categories,
						onEdit = { category ->
							categoryToEdit = category
							showDialog = true
						},
						onDelete = { categoryId ->
							viewModel.deleteCategory(categoryId)
						}
					)
				}
			}

			// Tampilkan Error di SnackBar
			LaunchedEffect(state.error) {
				if (state.error != null) {
					displayError = state.error
					// Reset error setelah beberapa detik
					kotlinx.coroutines.delay(4000L)
					displayError = null
					// PENTING: Panggil fungsi di ViewModel untuk mereset state.error
					// (Asumsi Anda menambahkan viewModel.resetErrorState())
				}
			}

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

	// Dialog Input (Bisa untuk Tambah atau Edit)
	if (showDialog) {
		CategoryDialog(
			category = categoryToEdit,
			onDismiss = { showDialog = false },
			onConfirm = { name ->
				if (categoryToEdit == null) {
					viewModel.addCategory(name)
				} else {
					categoryToEdit?.id?.let { viewModel.updateCategory(it, name) }
				}
				showDialog = false
			}
		)
	}
}

// ---------------------------------------------
// ## TopAppBar
// ---------------------------------------------

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

// ---------------------------------------------
// ## Grid List
// ---------------------------------------------

@Composable
fun CategoryGrid(
	categories: List<Category>,
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
			category.id?.let {
				CategoryItem(
					category = category,
					onEdit = { onEdit(category) },
					onDelete = { onDelete(it) }
				)
			}
		}
	}
}

// ---------------------------------------------
// ## Item Kategori (Card)
// ---------------------------------------------

@Composable
fun CategoryItem(
	category: Category,
	onEdit: () -> Unit,
	onDelete: () -> Unit
) {
	Card(
		shape = RoundedCornerShape(12.dp),
		elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
		modifier = Modifier.fillMaxWidth()
	) {
		Column(
			modifier = Modifier.padding(16.dp)
		) {
			// Ikon dan Nama
			Row(verticalAlignment = Alignment.CenterVertically) {
				Icon(
					Icons.Default.Folder, // Ikon Folder
					contentDescription = null,
					tint = MaterialTheme.colorScheme.primary,
					modifier = Modifier.size(24.dp)
				)
				Spacer(Modifier.width(8.dp))
				Text(
					text = category.name,
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.Bold
				)
			}
			Spacer(Modifier.height(12.dp))

			// Aksi Edit dan Hapus
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.End
			) {
				IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
					Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.secondary)
				}
				Spacer(Modifier.width(4.dp))
				IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
					Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
				}
			}
		}
	}
}

// ---------------------------------------------
// ## Empty State
// ---------------------------------------------

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
	Column(
		// ⭐ PERUBAHAN UTAMA: Tambahkan modifier.fillMaxSize()
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

// ---------------------------------------------
// ## Dialog Input (Tambah/Edit)
// ---------------------------------------------

@Composable
fun CategoryDialog(
	category: Category?,
	onDismiss: () -> Unit,
	onConfirm: (String) -> Unit
) {
	var text by remember { mutableStateOf(category?.name ?: "") }

	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text(text = if (category == null) "Tambah Kategori" else "Edit Kategori") },
		text = {
			OutlinedTextField(
				value = text,
				onValueChange = { text = it },
				label = { Text("Nama Kategori") },
				singleLine = true
			)
		},
		confirmButton = {
			Button(
				onClick = { if (text.isNotBlank()) onConfirm(text) }
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
