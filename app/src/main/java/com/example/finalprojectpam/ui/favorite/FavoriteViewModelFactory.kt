package com.example.finalprojectpam.ui.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.finalprojectpam.data.repository.FavoriteRepository
<<<<<<< HEAD

class FavoriteViewModelFactory(
    private val repository: FavoriteRepository
=======
// --- PERBAIKAN 1: Tambahkan import untuk NoteRepository ---
import com.example.finalprojectpam.data.repository.NoteRepository

// --- PERBAIKAN 2: Tambahkan NoteRepository ke dalam constructor ---
class FavoriteViewModelFactory(
    private val noteRepository: NoteRepository, // Tambahkan ini
    private val favoriteRepository: FavoriteRepository // Ubah nama variabel agar lebih jelas
>>>>>>> local-edit
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoriteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
<<<<<<< HEAD
            return FavoriteViewModel(repository) as T
=======
            // --- PERBAIKAN 3: Kirim kedua repository ke FavoriteViewModel ---
            return FavoriteViewModel(noteRepository, favoriteRepository) as T
>>>>>>> local-edit
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
