package com.example.finalprojectpam.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.finalprojectpam.data.model.Category
import com.example.finalprojectpam.data.repository.CategoryRepository
import com.example.finalprojectpam.data.repository.StorageRepository
import com.example.finalprojectpam.data.supabase.SupabaseProvider
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoryUiState(
	val categories: List<Category> = emptyList(),
	val isLoading: Boolean = false,
	val error: String? = null
)

class CategoryViewModel(
	private val repository: CategoryRepository,
	private val storageRepository: StorageRepository
) : ViewModel() {

	private val supabase = SupabaseProvider.client

	private val _uiState = MutableStateFlow(CategoryUiState())
	val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

	init {
		loadCategories()
	}

	fun loadCategories() {
		viewModelScope.launch {
			_uiState.update { it.copy(isLoading = true, error = null) }
			try {
				val data = repository.getCategories()
				_uiState.update { it.copy(categories = data, isLoading = false) }
			} catch (e: Exception) {
				_uiState.update { it.copy(isLoading = false, error = e.message) }
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

					val imagePath = "categories/${userId}/${System.currentTimeMillis()}.jpg"

					val uploadedPath = storageRepository.uploadImage(newImageByteArray, imagePath)
					finalImageUrl = storageRepository.getPublicUrl(uploadedPath)
				}
				// Jika newImageByteArray null, tapi di UI pengguna ingin menghapus gambar (finalImageUrl diset null)
				// maka finalImageUrl yang null akan dikirim ke repository

				// 2. Update data kategori ke PostgREST
				repository.updateCategory(id, name, finalImageUrl)

				loadCategories()
			} catch (e: Exception) {
				_uiState.update { it.copy(isLoading = false, error = e.message) }
			}
		}
	}

	fun deleteCategory(id: String) {
		viewModelScope.launch {
			_uiState.update { it.copy(isLoading = true) }
			try {
				repository.deleteCategory(id)
				loadCategories()
			} catch (e: Exception) {
				_uiState.update { it.copy(isLoading = false, error = e.message) }
			}
		}
	}

	fun resetErrorState() {
		_uiState.update { it.copy(error = null) }
	}
}

class CategoryViewModelFactory(
	private val repository: CategoryRepository,
	private val storageRepository: StorageRepository
) : ViewModelProvider.Factory {
	override fun <T : ViewModel> create(modelClass: Class<T>): T {
		if (modelClass.isAssignableFrom(CategoryViewModel::class.java)) {
			@Suppress("UNCHECKED_CAST")
			return CategoryViewModel(repository, storageRepository) as T
		}
		throw IllegalArgumentException("Unknown ViewModel class")
	}
}
