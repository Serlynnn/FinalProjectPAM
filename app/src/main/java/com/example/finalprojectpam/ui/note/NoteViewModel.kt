package com.example.finalprojectpam.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalprojectpam.data.model.Note
import com.example.finalprojectpam.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// State untuk daftar catatan (List Screen)
data class NotesListUiState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

// State untuk layar tambah/edit (Entry Screen)
data class NoteEntryUiState(
    val currentNote: Note? = null,
    val title: String = "",
    val content: String = "",
    val isSaving: Boolean = false,
    val isReady: Boolean = false,
    val error: String? = null
) {
    val isEditMode: Boolean = currentNote != null
    val isSaveEnabled: Boolean = title.isNotBlank() && content.isNotBlank() && !isSaving
}

class NoteViewModel(private val noteRepository: NoteRepository) : ViewModel() {

    // State untuk List Notes
    private val _listUiState = MutableStateFlow(NotesListUiState())
    val listUiState: StateFlow<NotesListUiState> = _listUiState.asStateFlow()

    // State untuk Entry/Detail Note
    private val _entryUiState = MutableStateFlow(NoteEntryUiState())
    val entryUiState: StateFlow<NoteEntryUiState> = _entryUiState.asStateFlow()

    init {
        // Mulai Realtime Stream saat ViewModel diinisialisasi
        getNotes()
    }

    // --- CRUD LIST FUNCTION ---

    private fun getNotes() {
        viewModelScope.launch {
            _listUiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Mengumpulkan stream data dari repository (Realtime)
                noteRepository.getNotesStream().collect { notes ->
                    _listUiState.update { it.copy(notes = notes, isLoading = false) }
                }
            } catch (e: Exception) {
                _listUiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            try {
                noteRepository.deleteNote(noteId)
                // UI akan otomatis diperbarui karena Realtime Stream
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    // --- CRUD ENTRY/DETAIL FUNCTION ---

    // 1. Ambil data saat masuk ke layar Entry
    fun loadNoteDetail(noteId: String) {
        if (noteId == "new") {
            _entryUiState.value = NoteEntryUiState(isReady = true) // Mode Tambah Baru
            return
        }

        viewModelScope.launch {
            _entryUiState.update { it.copy(isReady = false, error = null) }
            try {
                val note = noteRepository.getNoteById(noteId)
                if (note != null) {
                    _entryUiState.update {
                        it.copy(
                            currentNote = note,
                            title = note.title,
                            content = note.content,
                            isReady = true
                        )
                    }
                } else {
                    _entryUiState.update { it.copy(error = "Catatan tidak ditemukan", isReady = true) }
                }
            } catch (e: Exception) {
                _entryUiState.update { it.copy(error = e.localizedMessage, isReady = true) }
            }
        }
    }

    // 2. Update input dari UI
    fun updateTitle(newTitle: String) {
        _entryUiState.update { it.copy(title = newTitle) }
    }

    fun updateContent(newContent: String) {
        _entryUiState.update { it.copy(content = newContent) }
    }

    // 3. Simpan (Insert/Update)
    suspend fun saveNote(): Boolean {
        if (!_entryUiState.value.isSaveEnabled) return false

        _entryUiState.update { it.copy(isSaving = true, error = null) }
        val state = _entryUiState.value

        val success = try {
            if (state.isEditMode && state.currentNote != null) {
                // Update Existing Note
                val updatedNote = state.currentNote.copy(
                    title = state.title,
                    content = state.content
                )
                noteRepository.updateNote(updatedNote) != null
            } else {
                // Insert New Note
                val newNote = Note(
                    user_id = "", // user_id akan diisi di Repository
                    title = state.title,
                    content = state.content
                )
                noteRepository.insertNote(newNote) != null
            }
        } catch (e: Exception) {
            _entryUiState.update { it.copy(error = e.localizedMessage, isSaving = false) }
            false
        }

        if (success) {
            _entryUiState.update { NoteEntryUiState() } // Reset state setelah sukses
        }
        return success
    }
}