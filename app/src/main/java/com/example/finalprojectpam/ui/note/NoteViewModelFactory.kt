package com.example.finalprojectpam.ui.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.finalprojectpam.data.repository.NoteRepository
import com.example.finalprojectpam.data.repository.CategoryRepository
import com.example.finalprojectpam.data.repository.FavoriteRepository

class NoteViewModelFactory(
	private val noteRepository: NoteRepository,
	private val categoryRepository: CategoryRepository,
	private val favoriteRepository: FavoriteRepository
) : ViewModelProvider.Factory {

	override fun <T : ViewModel> create(modelClass: Class<T>): T {
		if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
			@Suppress("UNCHECKED_CAST")
			return NoteViewModel(
				noteRepository,
				categoryRepository,
				favoriteRepository
			) as T
		}
		throw IllegalArgumentException("Unknown ViewModel class")
	}
}