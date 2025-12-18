package com.example.finalprojectpam.data.repository

import com.example.finalprojectpam.data.model.Note
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class NoteRepository(private val client: SupabaseClient) {

	private val tableName = "notes"

	private fun getCurrentUserId(): String? {
		return client.auth.currentUserOrNull()?.id
	}

	// 1. READ LIST (Tanpa Realtime - Fetch Manual)
	fun getNotesStream(): Flow<List<Note>> = flow {
		val currentUserId = getCurrentUserId()
		if (currentUserId != null) {
			try {
				val result = client.postgrest.from(tableName)
					.select {
						filter {
							eq("user_id", currentUserId)
						}
						order("date_created", order = Order.DESCENDING)
					}
					.decodeList<Note>()
				emit(result)
			} catch (e: Exception) {
				emit(emptyList())
			}
		} else {
			emit(emptyList())
		}
	}

	// 2. READ by ID
	suspend fun getNoteById(noteId: String): Note? {
		return try {
			client.postgrest.from(tableName)
				.select {
					filter {
						eq("id", noteId)
					}
				}
				.decodeSingleOrNull<Note>()
		} catch (e: Exception) {
			null
		}
	}

	// 3. INSERT
	suspend fun insertNote(note: Note): Note? {
		val userId = getCurrentUserId() ?: throw IllegalStateException("User not logged in")
		val noteWithUserId = note.copy(userId = userId)

		return client.postgrest.from(tableName)
			.insert(noteWithUserId) {
				select()
			}
			.decodeSingleOrNull<Note>()
	}

	// 4. UPDATE
	suspend fun updateNote(note: Note): Note? {
		return client.postgrest.from(tableName)
			.update(note) {
				filter { eq("id", note.id ?: "") }
				select()
			}
			.decodeSingleOrNull<Note>()
	}

	// 5. DELETE
	suspend fun deleteNote(noteId: String) {
		client.postgrest.from(tableName)
			.delete {
				filter {
					eq("id", noteId)
				}
			}
	}
}
