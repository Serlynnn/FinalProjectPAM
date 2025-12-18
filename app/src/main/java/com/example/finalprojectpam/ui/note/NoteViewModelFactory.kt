package com.example.finalprojectpam.ui.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.finalprojectpam.data.repository.NoteRepository
import com.example.finalprojectpam.data.repository.CategoryRepository

class NoteViewModelFactory(
	private val noteRepository: NoteRepository,
	private val categoryRepository: CategoryRepository // Tambah ini
) : ViewModelProvider.Factory {
	override fun <T : ViewModel> create(modelClass: Class<T>): T {
		return NoteViewModel(noteRepository, categoryRepository) as T
	}
}
