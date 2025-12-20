package com.example.finalprojectpam.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.finalprojectpam.data.repository.CategoryRepository
import com.example.finalprojectpam.data.repository.NoteRepository
import com.example.finalprojectpam.data.repository.StorageRepository

class CategoryViewModelFactory(
	private val repository: CategoryRepository,
	private val storageRepository: StorageRepository,
	private val noteRepository: NoteRepository
) : ViewModelProvider.Factory {
	override fun <T : ViewModel> create(modelClass: Class<T>): T {
		if (modelClass.isAssignableFrom(CategoryViewModel::class.java)) {
			@Suppress("UNCHECKED_CAST")
			return CategoryViewModel(repository, storageRepository, noteRepository) as T
		}
		throw IllegalArgumentException("Unknown ViewModel class")
	}
}
