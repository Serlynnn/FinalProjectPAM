package com.example.finalprojectpam.ui.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalprojectpam.data.repository.FavoriteRepository
import com.example.finalprojectpam.data.supabase.SupabaseProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import io.github.jan.supabase.auth.auth


class FavoriteViewModel(
    private val repository: FavoriteRepository
) : ViewModel() {

    private val _favoriteNoteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteNoteIds: StateFlow<Set<String>> = _favoriteNoteIds

    private val userId: String?
        get() = SupabaseProvider.client.auth.currentUserOrNull()?.id

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        val uid = userId ?: return
        viewModelScope.launch {
            val ids = repository.getFavoriteNoteIds(uid)
            _favoriteNoteIds.value = ids.toSet()
        }
    }

    fun toggleFavorite(noteId: String) {
        val uid = userId ?: return
        viewModelScope.launch {
            if (_favoriteNoteIds.value.contains(noteId)) {
                repository.removeFavorite(uid, noteId)
                _favoriteNoteIds.value -= noteId
            } else {
                repository.addFavorite(uid, noteId)
                _favoriteNoteIds.value += noteId
            }
        }
    }

    fun isFavorite(noteId: String): Boolean {
        return _favoriteNoteIds.value.contains(noteId)
    }
}
