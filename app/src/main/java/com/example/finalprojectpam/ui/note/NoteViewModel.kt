package com.example.finalprojectpam.ui.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalprojectpam.data.model.Note
import com.example.finalprojectpam.data.model.Category
import com.example.finalprojectpam.data.repository.NoteRepository
import com.example.finalprojectpam.data.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// State untuk daftar catatan (List Screen)
data class NotesListUiState(
	val notes: List<Note> = emptyList(),
	val isLoading: Boolean = true,
	val error: String? = null,
	val categoryId: String? = null,
	val categories: List<Category> = emptyList()
)

// State untuk layar tambah/edit (Entry Screen)
data class NoteEntryUiState(
	val currentNote: Note? = null,
	val title: String = "",
	val content: String = "",
	val categoryId: String? = null, // Tambah ini
	val categories: List<Category> = emptyList(), // Tambah ini untuk list dropdown
	val isSaving: Boolean = false,
	val isReady: Boolean = false,
	val error: String? = null
) {
	val isEditMode: Boolean = currentNote != null
	val isSaveEnabled: Boolean = title.isNotBlank() && content.isNotBlank() && !isSaving
}

class NoteViewModel(
	private val noteRepository: NoteRepository,
	private val categoryRepository: CategoryRepository
) : ViewModel() {

	// State untuk List Notes
	private val _listUiState = MutableStateFlow(NotesListUiState())
	val listUiState: StateFlow<NotesListUiState> = _listUiState.asStateFlow()

	// State untuk Entry/Detail Note
	private val _entryUiState = MutableStateFlow(NoteEntryUiState())
	val entryUiState: StateFlow<NoteEntryUiState> = _entryUiState.asStateFlow()

	fun loadNotes() {
		viewModelScope.launch {
			_listUiState.update { it.copy(isLoading = true) }
			try {
				noteRepository.getNotesStream().collect { notes ->
					_listUiState.update { it.copy(notes = notes, isLoading = false) }
				}
			} catch (e: Exception) {
				_listUiState.update { it.copy(error = e.localizedMessage, isLoading = false) }
			}
		}
	}

	fun updateCategoryId(id: String?) {
		_entryUiState.update { it.copy(categoryId = id) }
	}

	fun loadCategoriesForNote() {
		viewModelScope.launch {
			try {
				// Karena repo kamu suspend fun (List), tidak perlu .collect
				val categories = categoryRepository.getCategories()
				_entryUiState.update { it.copy(categories = categories) }
			} catch (e: Exception) {
				// handle error
			}
		}
	}

//	private fun getNotes() {
//		viewModelScope.launch {
//			_listUiState.update { it.copy(isLoading = true, error = null) }
//			try {
//				// Mengumpulkan stream data dari repository (Realtime)
//				noteRepository.getNotesStream().collect { notes ->
//					_listUiState.update { it.copy(notes = notes, isLoading = false) }
//				}
//			} catch (e: Exception) {
//				_listUiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
//			}
//		}
//	}

	fun deleteNote(id: String) {
		viewModelScope.launch {
			noteRepository.deleteNote(id)
			loadNotes()
		}
	}

	// --- CRUD ENTRY/DETAIL FUNCTION ---

	// 1. Ambil data saat masuk ke layar Entry
	fun loadNoteDetail(noteId: String) {
		loadCategories()

		if (noteId == "new") {
			_entryUiState.value = NoteEntryUiState(isReady = true)
			return
		}

		viewModelScope.launch {
			try {
				val note = noteRepository.getNoteById(noteId)
				if (note != null) {
					_entryUiState.update {
						it.copy(
							currentNote = note,
							title = note.title,
							content = note.content ?: "",
							categoryId = note.categoryId, // Set kategori yang tersimpan
							isReady = true
						)
					}
					loadCategoriesForNote()
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

		return try {
			val success = if (state.isEditMode && state.currentNote != null) {
				// MODE EDIT
				val updatedNote = state.currentNote.copy(
					title = state.title,
					content = state.content,
					categoryId = state.categoryId // Pastikan categoryId juga ikut diupdate
				)
				noteRepository.updateNote(updatedNote) != null
			} else {
				// MODE INSERT
				val newNote = Note(
					userId = "",
					title = state.title,
					content = state.content,
					categoryId = state.categoryId
				)
				noteRepository.insertNote(newNote) != null
			}

			if (success) {
				loadNotes() // Refresh list agar data terbaru muncul di screen utama
			}

			// Matikan loading sebelum return
			_entryUiState.update { it.copy(isSaving = false) }
			success
		} catch (e: Exception) {
			_entryUiState.update { it.copy(isSaving = false, error = e.localizedMessage) }
			false
		}
	}

	fun loadCategories() { // Hapus parameter jika sudah diinject di constructor
		viewModelScope.launch {
			try {
				// Karena repo kamu suspend fun (List), langsung ambil datanya
				val list = categoryRepository.getCategories()
				_entryUiState.update { it.copy(categories = list) }
			} catch (e: Exception) {
				_entryUiState.update { it.copy(error = "Gagal memuat kategori") }
			}
		}
	}
}
