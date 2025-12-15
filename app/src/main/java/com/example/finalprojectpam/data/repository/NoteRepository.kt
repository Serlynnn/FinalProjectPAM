package com.example.finalprojectpam.data.repository

import com.example.finalprojectpam.data.model.Note
import io.github.janupranata.supabase.SupabaseClient
import io.github.janupranata.supabase.postgrest.postgrest
import io.github.janupranata.supabase.postgrest.query.Order
import io.github.janupranata.supabase.postgrest.query.PostgrestOrder
import io.github.janupranata.supabase.postgrest.query.Columns
import io.github.janupranata.supabase.realtime.realtime
import io.github.janupranata.supabase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class NoteRepository(private val client: SupabaseClient) {

	private val tableName = "notes"

	// Mendapatkan User ID yang sedang login
	private fun getCurrentUserId(): String? {
		return client.auth.currentUserOrNull()?.id
	}

	// 1. READ (Realtime Stream)
	// Menggunakan Postgrest Stream untuk mendapatkan pembaruan Realtime
	fun getNotesStream(): Flow<List<Note>> {
		val currentUserId = getCurrentUserId()
		if (currentUserId == null) {
			// Jika tidak ada user, kembalikan flow kosong
			return flowOf(emptyList())
		}

		return client.postgrest
			.from(tableName)
			.select(columns = Columns.List("id", "user_id", "title", "content", "date_created"))
			.filter { eq("user_id", currentUserId) } // Filter hanya catatan milik user ini
			.order("date_created", PostgrestOrder.DESCENDING)
			.stream<Note>()
			.map { it.getOrThrow().toList() }
	}

	// 2. READ by ID (Detail)
	suspend fun getNoteById(noteId: String): Note? {
		return client.postgrest[tableName]
			.select { eq("id", noteId) }
			.decodeSingleOrNull()
	}

	// 3. INSERT
	suspend fun insertNote(note: Note): Note? {
		// Pastikan user_id terisi dari user yang sedang login
		val noteWithUserId = note.copy(user_id = getCurrentUserId() ?: throw IllegalStateException("User not logged in"))
		return client.postgrest[tableName].insert(noteWithUserId) { select() }.decodeSingleOrNull()
	}

	// 4. UPDATE
	suspend fun updateNote(note: Note): Note? {
		return client.postgrest[tableName]
			.update(note) { eq("id", note.id) }
			.decodeSingleOrNull()
	}

	// 5. DELETE
	suspend fun deleteNote(noteId: String) {
		client.postgrest[tableName].delete { eq("id", noteId) }
	}
}