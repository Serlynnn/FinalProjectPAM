package com.example.finalprojectpam.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.finalprojectpam.data.model.Category
import com.example.finalprojectpam.data.repository.CategoryRepository
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

class CategoryViewModel(private val repository: CategoryRepository) : ViewModel() {

	private val _uiState = MutableStateFlow(CategoryUiState())
	val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

	// Load data saat ViewModel dibuat
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

	fun addCategory(name: String) {
		viewModelScope.launch {
			_uiState.update { it.copy(isLoading = true) }
			try {
				repository.addCategory(name)
				loadCategories() // Refresh list setelah tambah
			} catch (e: Exception) {
				_uiState.update { it.copy(isLoading = false, error = e.message) }
			}
		}
	}

	fun updateCategory(id: String, name: String) {
		viewModelScope.launch {
			_uiState.update { it.copy(isLoading = true) }
			try {
				repository.updateCategory(id, name)
				loadCategories() // Refresh list setelah update
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
				loadCategories() // Refresh list setelah hapus
			} catch (e: Exception) {
				_uiState.update { it.copy(isLoading = false, error = e.message) }
			}
		}
	}
}

// Factory untuk ViewModel
class CategoryViewModelFactory(private val repository: CategoryRepository) : ViewModelProvider.Factory {
	override fun <T : ViewModel> create(modelClass: Class<T>): T {
		if (modelClass.isAssignableFrom(CategoryViewModel::class.java)) {
			@Suppress("UNCHECKED_CAST")
			return CategoryViewModel(repository) as T
		}
		throw IllegalArgumentException("Unknown ViewModel class")
	}
}
