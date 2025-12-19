package com.example.finalprojectpam.ui.studyplan

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.finalprojectpam.data.model.StudyPlan
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// ---------------------------------------------
// ## 1. Layar Utama (StudyPlanScreen)
// ---------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyPlanScreen(
	viewModel: StudyPlanViewModel,
	onNavigateBack: () -> Unit
) {
	val state by viewModel.uiState.collectAsState()
	var showAddEditDialog by remember { mutableStateOf(false) }
	var planToEdit by remember { mutableStateOf<StudyPlan?>(null) }

	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text("Rencana Belajar") },
				navigationIcon = {
					IconButton(onClick = onNavigateBack) {
						Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
					}
				}
			)
		},
		floatingActionButton = {
			FloatingActionButton(onClick = {
				planToEdit = null
				showAddEditDialog = true
			}) {
				Icon(Icons.Default.Add, contentDescription = "Tambah Jadwal")
			}
		}
	) { paddingValues ->
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(paddingValues)
		) {
			when {
				state.isLoading -> {
					Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
						CircularProgressIndicator()
					}
				}
				state.studyPlans.isEmpty() && state.error == null -> {
					EmptyPlanMessage()
				}
				else -> {
					StudyPlanList(
						plans = state.studyPlans,
						onEditClick = { plan ->
							planToEdit = plan
							showAddEditDialog = true
						},
						onDeleteClick = { plan ->
							plan.id?.let { viewModel.deleteStudyPlan(it) }
						}
					)
				}
			}

			AnimatedVisibility(
				visible = state.error != null,
				enter = fadeIn(),
				exit = fadeOut()
			) {
				state.error?.let {
					Snackbar(
						modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally),
						action = {
							TextButton(onClick = { viewModel.clearError() }) {
								Text("TUTUP")
							}
						}
					) {
						Text(it)
					}
				}
			}
		}
	}

	if (showAddEditDialog) {
		AddEditPlanDialog(
			planToEdit = planToEdit,
			onDismiss = { showAddEditDialog = false },
			onSave = { id, title, date ->
				if (id == null) {
					viewModel.addStudyPlan(title, date)
				} else {
					viewModel.updateStudyPlan(id, title, date)
				}
				showAddEditDialog = false
			}
		)
	}
}

// ---------------------------------------------
// ## 2. Daftar Rencana Belajar (StudyPlanList)
// ---------------------------------------------

@Composable
fun StudyPlanList(
	plans: List<StudyPlan>,
	onEditClick: (StudyPlan) -> Unit,
	onDeleteClick: (StudyPlan) -> Unit
) {
	LazyColumn(
		modifier = Modifier.fillMaxSize(),
		contentPadding = PaddingValues(16.dp),
		verticalArrangement = Arrangement.spacedBy(12.dp)
	) {
		items(plans, key = { it.id ?: it.title + it.date }) { plan ->
			StudyPlanItem(
				plan = plan,
				onEditClick = { onEditClick(plan) },
				onDeleteClick = { onDeleteClick(plan) }
			)
		}
	}
}

@Composable
fun StudyPlanItem(
	plan: StudyPlan,
	onEditClick: () -> Unit,
	onDeleteClick: () -> Unit
) {
	Card(
		modifier = Modifier.fillMaxWidth(),
		elevation = CardDefaults.cardElevation(2.dp)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			Column(
				modifier = Modifier.weight(1f)
			) {
				Text(
					text = plan.title,
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.Bold
				)
				Spacer(modifier = Modifier.height(4.dp))
				Row(verticalAlignment = Alignment.CenterVertically) {
					Icon(
						Icons.Default.DateRange,
						contentDescription = "Tanggal",
						modifier = Modifier.size(16.dp).padding(end = 4.dp)
					)
					Text(
						text = formatDateDisplay(plan.date),
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
			}

			Row {
				IconButton(onClick = onEditClick) {
					Icon(Icons.Default.Edit, contentDescription = "Edit")
				}
				IconButton(onClick = onDeleteClick) {
					Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
				}
			}
		}
	}
}

// ---------------------------------------------
// ## 3. Dialog Input (Tambah/Edit) dengan DatePicker
// ---------------------------------------------

@Composable
fun AddEditPlanDialog(
	planToEdit: StudyPlan?,
	onDismiss: () -> Unit,
	onSave: (id: String?, title: String, date: String) -> Unit
) {
	val context = LocalContext.current
	var title by remember { mutableStateOf(planToEdit?.title ?: "") }
	var dateString by remember { mutableStateOf(planToEdit?.date ?: getTodayDateString()) }

	// Inisialisasi kalender untuk DatePickerDialog
	val calendar = Calendar.getInstance()

	// Jika sedang edit, set kalender ke tanggal yang sudah ada
	try {
		val currentPlanDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString)
		if (currentPlanDate != null) calendar.time = currentPlanDate
	} catch (e: Exception) { }

	val datePickerDialog = DatePickerDialog(
		context,
		{ _, year, month, dayOfMonth ->
			// Simpan dalam format yyyy-MM-dd agar sesuai dengan repository
			dateString = "$year-${(month + 1).toString().padStart(2, '0')}-${dayOfMonth.toString().padStart(2, '0')}"
		},
		calendar.get(Calendar.YEAR),
		calendar.get(Calendar.MONTH),
		calendar.get(Calendar.DAY_OF_MONTH)
	)

	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text(text = if (planToEdit == null) "Tambah Rencana" else "Edit Rencana") },
		text = {
			Column {
				OutlinedTextField(
					value = title,
					onValueChange = { title = it },
					label = { Text("Judul/Materi Belajar") },
					leadingIcon = { Icon(Icons.Default.Book, contentDescription = null) },
					singleLine = true,
					modifier = Modifier.fillMaxWidth()
				)
				Spacer(Modifier.height(8.dp))

				Row(
					modifier = Modifier.fillMaxWidth(),
					verticalAlignment = Alignment.CenterVertically
				) {
					OutlinedTextField(
						value = formatDateDisplay(dateString),
						onValueChange = { },
						label = { Text("Tanggal Target") },
						readOnly = true,
						leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
						modifier = Modifier.weight(1f)
					)
					Spacer(Modifier.width(8.dp))

					Button(onClick = { datePickerDialog.show() }) {
						Text("Pilih")
					}
				}
			}
		},
		confirmButton = {
			Button(
				onClick = {
					if (title.isNotBlank()) {
						onSave(planToEdit?.id, title, dateString)
					}
				},
				enabled = title.isNotBlank()
			) {
				Text(if (planToEdit == null) "SIMPAN" else "PERBARUI")
			}
		},
		dismissButton = {
			TextButton(onClick = onDismiss) {
				Text("BATAL")
			}
		}
	)
}

// ---------------------------------------------
// ## 4. Komponen Pesan Kosong & Utilitas
// ---------------------------------------------

@Composable
fun EmptyPlanMessage() {
	Column(
		modifier = Modifier.fillMaxSize().padding(16.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		Icon(
			Icons.Default.Book,
			contentDescription = "Belum Ada Rencana",
			modifier = Modifier.size(64.dp),
			tint = MaterialTheme.colorScheme.outline
		)
		Spacer(Modifier.height(16.dp))
		Text(
			text = "Belum ada rencana belajar.",
			style = MaterialTheme.typography.titleMedium
		)
		Text(
			text = "Tekan tombol '+' untuk menambah jadwal baru.",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)
	}
}

fun formatDateDisplay(dateString: String): String {
	return try {
		val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
		val date: Date? = inputFormat.parse(dateString)
		val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
		date?.let { outputFormat.format(it) } ?: "Tanggal tidak valid"
	} catch (e: Exception) {
		"Format Error"
	}
}

fun getTodayDateString(): String {
	val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
	return dateFormat.format(Date())
}
