package com.example.finalprojectpam.ui.note

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalprojectpam.data.model.Note
import com.example.finalprojectpam.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


// 1. DEFINISI STATE (Gaya Anggota 1, tapi ditambah field Gambar)

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

    // Field Tambahan untuk Gambar
    val selectedImageUri: Uri? = null,
    val existingImageUrl: String? = null,

    val isSaving: Boolean = false,
    val isReady: Boolean = false,
    val error: String? = null
) {
    val isEditMode: Boolean = currentNote != null
    // Tombol save aktif jika judul & konten tidak kosong, dan tidak sedang loading
    val isSaveEnabled: Boolean = title.isNotBlank() && content.isNotBlank() && !isSaving
}

// 2. VIEW MODEL UTAMA

class NoteViewModel(private val noteRepository: NoteRepository) : ViewModel() {

    // State untuk List Notes
    private val _listUiState = MutableStateFlow(NotesListUiState())
    val listUiState: StateFlow<NotesListUiState> = _listUiState.asStateFlow()

    // State untuk Entry/Detail Note
    private val _entryUiState = MutableStateFlow(NoteEntryUiState())
    val entryUiState: StateFlow<NoteEntryUiState> = _entryUiState.asStateFlow()

    init {
        // Mulai Realtime Stream saat ViewModel diinisialisasi
        loadNotes()
    }

    // FITUR LIST (READ & DELETE

    fun loadNotes() {
        viewModelScope.launch {
            _listUiState.update { it.copy(isLoading = true, error = null) }
            try {
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
            } catch (e: Exception) {
                Log.e("Error", e.message ?: "Unknown Error")
            }
        }
    }

    // FITUR ENTRY (CREATE, UPDATE & UPLOAD GAMBAR)

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
                            content = note.content ?: "",
                            existingImageUrl = note.imageUrl, // Load gambar lama jika ada
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

    // 2. Update Inputan Text dari UI
    fun updateTitle(newTitle: String) {
        _entryUiState.update { it.copy(title = newTitle) }
    }

    fun updateContent(newContent: String) {
        _entryUiState.update { it.copy(content = newContent) }
    }

    // 3. Update Inputan Gambar (Saat user pilih dari galeri)
    fun updateSelectedImage(uri: Uri) {
        _entryUiState.update { it.copy(selectedImageUri = uri) }
    }

    // 4. LOGIKA UTAMA: Simpan (Upload Gambar Dulu -> Baru Simpan Data)
    // butuh Context untuk membaca file Uri menjadi ByteArray
    fun saveNote(context: Context, onResult: (Boolean) -> Unit) {
        val state = _entryUiState.value
        if (!state.isSaveEnabled) return

        viewModelScope.launch {
            _entryUiState.update { it.copy(isSaving = true, error = null) }

            try {
                // A. Cek apakah ada gambar baru yang dipilih?
                var finalImageUrl: String? = state.existingImageUrl // Default pakai gambar lama

                if (state.selectedImageUri != null) {
                    // Logic Anggota 2: Konversi Uri ke ByteArray
                    val inputStream = context.contentResolver.openInputStream(state.selectedImageUri)
                    val byteArray = inputStream?.readBytes()

                    if (byteArray != null) {
                        // Upload ke Supabase Storage
                        finalImageUrl = noteRepository.uploadNoteImage(byteArray)
                    }
                }

                // B. Simpan data ke Database (Insert / Update)
                val success = if (state.isEditMode && state.currentNote != null) {
                    // --- UPDATE ---
                    val updatedNote = state.currentNote.copy(
                        title = state.title,
                        content = state.content,
                        imageUrl = finalImageUrl
                    )
                    noteRepository.updateNote(updatedNote) != null
                } else {
                    // --- INSERT ---
                    // userId diisi "" dulu, nanti Repository yang isi pakai ID asli dari Auth
                    val newNote = Note(
                        userId = "",
                        title = state.title,
                        content = state.content,
                        imageUrl = finalImageUrl,
                        categoryId = null
                    )
                    noteRepository.insertNote(newNote) != null
                }

                if (success) {
                    Toast.makeText(context, "Berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    onResult(true)
                } else {
                    _entryUiState.update { it.copy(error = "Gagal menyimpan ke database", isSaving = false) }
                    onResult(false)
                }

            } catch (e: Exception) {
                _entryUiState.update { it.copy(error = "Error: ${e.message}", isSaving = false) }
                onResult(false)
            }
        }
    }
}