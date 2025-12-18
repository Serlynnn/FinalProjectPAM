package com.example.finalprojectpam.data.repository

import com.example.finalprojectpam.data.model.Note
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class NoteRepository(private val client: SupabaseClient) {

    private val tableName = "notes"

    private fun getCurrentUserId(): String? {
        val user = client.auth.currentUserOrNull()
        return user?.id
    }

    // FITUR ANGGOTA 1 (CRUD)

    // 1. READ
    fun getNotesStream(): Flow<List<Note>> = flow {
        val currentUserId = getCurrentUserId()
        if (currentUserId == null) {
            emit(emptyList())
            return@flow
        }

        val result = client.postgrest[tableName]
            .select(columns = Columns.list("id", "user_id", "title", "content", "date_created", "image_url", "category_id")) {
                filter {
                    eq("user_id", currentUserId)
                }
                order("date_created", Order.DESCENDING)
            }
            .decodeList<Note>()

        emit(result)
    }

    // 2. READ by ID
    suspend fun getNoteById(noteId: String): Note? {
        return client.postgrest[tableName]
            .select {
                filter {
                    eq("id", noteId)
                }
            }
            .decodeSingleOrNull()
    }

    // 3. INSERT
    suspend fun insertNote(note: Note): Note? {
        val currentUserId = getCurrentUserId() ?: throw IllegalStateException("User not logged in")
        val noteWithUserId = note.copy(userId = currentUserId)

        return client.postgrest[tableName]
            .insert(noteWithUserId) { select() }
            .decodeSingleOrNull()
    }

    // 4. UPDATE (Fixed syntax error)
    suspend fun updateNote(note: Note): Note? {
        val id = note.id ?: throw IllegalArgumentException("Note ID cannot be null")

        return client.postgrest[tableName]
            .update(note) {
                // Perhatikan: filter harus ada di dalam blok lambda ini
                filter {
                    eq("id", id)
                }
            }
            .decodeSingleOrNull()
    }

    // 5. DELETE (Fixed syntax error)
    suspend fun deleteNote(noteId: String) {
        client.postgrest[tableName]
            .delete {
                filter {
                    eq("id", noteId)
                }
            }
    }

    // FITUR ANGGOTA 2 (UPLOAD GAMBAR)

    // 6. Upload Image
    suspend fun uploadNoteImage(byteArray: ByteArray): String {
        val bucketName = "note-images"
        val fileName = "img_${System.currentTimeMillis()}.jpg"
        val bucket = client.storage.from(bucketName)
        bucket.upload(fileName, byteArray)
        return bucket.publicUrl(fileName)
    }
}