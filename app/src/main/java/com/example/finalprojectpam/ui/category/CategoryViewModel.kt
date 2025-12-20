package com.example.finalprojectpam.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalprojectpam.data.model.Category
import com.example.finalprojectpam.data.repository.CategoryRepository
import com.example.finalprojectpam.data.repository.StorageRepository
import com.example.finalprojectpam.data.repository.NoteRepository
import com.example.finalprojectpam.data.supabase.SupabaseProvider
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoryUiState(
	val categories: List<Category> = emptyList(),
	val noteCounts: Map<String, Int> = emptyMap(),
	val isLoading: Boolean = false,
	val error: String? = null
)

class CategoryViewModel(
	private val repository: CategoryRepository,
	private val storageRepository: StorageRepository,
	private val noteRepository: NoteRepository
) : ViewModel() {

	private val supabase = SupabaseProvider.client
	private val BUCKET_NAME = "materials" // Definisikan nama bucket di sini

	private val _uiState = MutableStateFlow(CategoryUiState())
	val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

	init {
		loadCategories()
	}

	// --- HELPER FUNCTION UNTUK MENGURAI URL (MENANGANI URL YANG SALAH DAN BENAR) ---
	private fun extractPathFromUrl(imageUrl: String): String? {
		val prefixA = "/object/public/$BUCKET_NAME/$BUCKET_NAME/" // Pola URL GANDA (yang salah)
		val prefixB = "/object/public/$BUCKET_NAME/"              // Pola URL BENAR

		return when {
			imageUrl.contains(prefixA) -> {
				// Jika mengandung gandaan (URL lama/salah)
				imageUrl.substringAfter(prefixA)
			}
			imageUrl.contains(prefixB) -> {
				// Jika mengandung URL yang benar
				imageUrl.substringAfter(prefixB)
			}
			else -> {
				null // Tidak bisa diurai
			}
		}
	}
	// --- AKHIR HELPER FUNCTION ---

	fun loadCategories() {
		viewModelScope.launch {
			_uiState.update { it.copy(isLoading = true) }
			try {
				// 1. Ambil Kategori
				val categories = repository.getCategories()

				// 2. Ambil Semua Note untuk menghitung jumlahnya
				// Kita gunakan getNotesStream atau buat fungsi suspend getNotes() di repo
				noteRepository.getNotesStream().collect { notes ->
					// Hitung jumlah note per kategori
					// Result: { "cat_id_1": 5, "cat_id_2": 3 }
					val counts = notes.filter { it.categoryId != null }
						.groupBy { it.categoryId!! }
						.mapValues { it.value.size }

					_uiState.update {
						it.copy(
							categories = categories,
							noteCounts = counts,
							isLoading = false
						)
					}
				}
			} catch (e: Exception) {
				_uiState.update { it.copy(error = e.localizedMessage, isLoading = false) }
			}
		}
	}

	fun addCategoryWithImage(name: String, imageByteArray: ByteArray?) {
		viewModelScope.launch {
			_uiState.update { it.copy(isLoading = true, error = null) }
			try {
				var imageUrl: String? = null

				// 1. Jika ada gambar, upload dulu
				if (imageByteArray != null) {
					val userId = supabase.auth.currentUserOrNull()?.id
						?: throw Exception("Pengguna tidak terautentikasi.")


					val imagePath = "categories/${userId}/${System.currentTimeMillis()}.jpg"

					val uploadedPath = storageRepository.uploadImage(imageByteArray, imagePath)
					imageUrl = storageRepository.getPublicUrl(uploadedPath)
				}

				// 2. Simpan data kategori ke PostgREST
				repository.addCategory(name, imageUrl)

				loadCategories()
			} catch (e: Exception) {
				_uiState.update { it.copy(isLoading = false, error = e.message) }
			}
		}
	}

	fun updateCategoryWithImage(
		id: String,
		name: String,
		currentImagePath: String?,
		newImageByteArray: ByteArray?
	) {
		viewModelScope.launch {
			_uiState.update { it.copy(isLoading = true, error = null) }
			try {
				var finalImageUrl = currentImagePath

				if (newImageByteArray != null) {
					val userId = supabase.auth.currentUserOrNull()?.id
						?: throw Exception("Pengguna tidak terautentikasi.")

					// ⭐ KOREKSI URL GANDA: imagePath HANYA berisi subfolder (tanpa 'materials/')
					val imagePath = "categories/${userId}/${System.currentTimeMillis()}.jpg"

					val uploadedPath = storageRepository.uploadImage(newImageByteArray, imagePath)
					finalImageUrl = storageRepository.getPublicUrl(uploadedPath)

					// Catatan: Penghapusan gambar lama di sini bersifat opsional dan lebih kompleks.
					// Saat ini, kita biarkan gambar lama (jika diganti) tetap ada di Storage untuk simplifikasi.
				}

				// 2. Update data kategori ke PostgREST
				repository.updateCategory(id, name, finalImageUrl)

				loadCategories()
			} catch (e: Exception) {
				_uiState.update { it.copy(isLoading = false, error = e.message) }
			}
		}
	}

	// ⭐ FUNGSI DELETE KATEGORI BARU DENGAN LOGIKA HAPUS GAMBAR
	fun deleteCategory(id: String) {
		viewModelScope.launch {
			_uiState.update { it.copy(isLoading = true, error = null) }
			try {
				// 1. Ambil data kategori yang akan dihapus dari state saat ini
				val categoryToDelete = _uiState.value.categories.find { it.id == id }

				val imageUrl = categoryToDelete?.imageUrl
				if (!imageUrl.isNullOrBlank()) {
					// 2. Jika ada gambar, ekstrak path dan hapus dari Storage
					val path = extractPathFromUrl(imageUrl)

					if (path != null) {
						storageRepository.deleteImage(path)
					}
				}

				// 3. Hapus baris data dari PostgREST/Database
				repository.deleteCategory(id)

				loadCategories() // Refresh list setelah hapus
			} catch (e: Exception) {
				_uiState.update { it.copy(isLoading = false, error = e.message) }
			}
		}
	}

	fun resetErrorState() {
		_uiState.update { it.copy(error = null) }
	}
}
