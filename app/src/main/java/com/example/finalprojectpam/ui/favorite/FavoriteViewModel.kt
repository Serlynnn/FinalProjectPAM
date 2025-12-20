package com.example.finalprojectpam.ui.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
<<<<<<< HEAD
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
=======
// --- PERBAIKAN 1: Import semua yang dibutuhkan ---
import com.example.finalprojectpam.data.model.Note
import com.example.finalprojectpam.data.repository.FavoriteRepository
import com.example.finalprojectpam.data.repository.NoteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// --- PERBAIKAN 2: Buat data class untuk UI State ---
// Ini akan menampung daftar catatan, status loading, dan error.
data class FavoriteUiState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// --- PERBAIKAN 3: Perbarui constructor ViewModel ---
class FavoriteViewModel(
    private val noteRepository: NoteRepository, // Butuh ini untuk dapat detail catatan
    private val favoriteRepository: FavoriteRepository // Butuh ini untuk dapat status favorit
) : ViewModel() {

    // --- PERBAIKAN 4: Sediakan UI State yang mengambil data dari NoteRepository ---
    // Ini akan secara otomatis meng-update dirinya sendiri setiap kali data di repository berubah.
    val uiState: StateFlow<FavoriteUiState> =
        noteRepository.getNotesStream() // Ambil stream dari NoteRepository
            .map { FavoriteUiState(notes = it) } // Konversi List<Note> menjadi FavoriteUiState
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = FavoriteUiState(isLoading = true) // Tampilkan loading pada awalnya
            )

    // --- PERBAIKAN 5: Sediakan ID favorit langsung dari FavoriteRepository ---
    // Cukup teruskan StateFlow dari repository. Tidak perlu logika tambahan.
    val favoriteNoteIds: StateFlow<Set<String>> = favoriteRepository.favoriteStatus

    // --- PERBAIKAN 6: Sederhanakan fungsi toggleFavorite ---
    // ViewModel tidak perlu tahu logikanya. Cukup perintahkan repository.
    fun toggleFavorite(noteId: String) {
        viewModelScope.launch {
            try {
                favoriteRepository.toggleFavoriteStatus(noteId)
            } catch (e: Exception) {
                // Tangani error jika perlu
                // Misalnya, Anda bisa memperbarui uiState dengan pesan error
>>>>>>> local-edit
            }
        }
    }

<<<<<<< HEAD
    fun isFavorite(noteId: String): Boolean {
        return _favoriteNoteIds.value.contains(noteId)
    }
=======
    // Fungsi loadFavorites() dan isFavorite() tidak diperlukan lagi karena
    // semuanya sudah ditangani secara reaktif oleh StateFlow.
>>>>>>> local-edit
}
