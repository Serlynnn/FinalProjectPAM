package com.example.finalprojectpam.data.repository

import com.example.finalprojectpam.data.supabase.SupabaseProvider
import io.github.jan.supabase.SupabaseClient
<<<<<<< HEAD
import io.github.jan.supabase.postgrest.from

class FavoriteRepository(
    private val supabase: SupabaseClient
) {

    suspend fun getFavoriteNoteIds(userId: String): List<String> {
        return supabase
            .from("favorites")
            .select {
                filter {
                    eq("user_id", userId)
                }
            }
            .decodeList<Map<String, String>>()
            .map { it["note_id"] ?: "" }
    }

    suspend fun addFavorite(userId: String, noteId: String) {
        supabase.from("favorites").insert(
            mapOf(
                "user_id" to userId,
                "note_id" to noteId
            )
        )
    }

    suspend fun removeFavorite(userId: String, noteId: String) {
        supabase.from("favorites").delete {
            filter {
                eq("user_id", userId)
                eq("note_id", noteId)
            }
        }
    }
=======
// Import yang benar untuk auth dan postgrest di versi library Anda
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FavoriteEntry(
    @SerialName("user_id")
    val userId: String,
    @SerialName("note_id")
    val noteId: String
)

// Hapus parameter dari constructor
class FavoriteRepository() {

    // Gunakan client terpusat dari SupabaseProvider
    private val supabase: SupabaseClient = SupabaseProvider.client

    // Ini adalah 'favoriteStatus' yang dicari oleh ViewModel Anda
    private val _favoriteStatus = MutableStateFlow<Set<String>>(emptySet())
    val favoriteStatus: StateFlow<Set<String>> = _favoriteStatus

    init {
        // Langsung ambil status favorit saat repository dibuat
        fetchFavoriteStatus()
    }

    private fun fetchFavoriteStatus() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Dapatkan user yang sedang login
                val currentUser = supabase.auth.currentUserOrNull() ?: return@launch

                val response = supabase.from("favorites")
                    .select {
                        filter {
                            eq("user_id", currentUser.id)
                        }
                    }
                    .decodeList<FavoriteEntry>()

                // Update StateFlow, yang akan otomatis memperbarui UI
                _favoriteStatus.value = response.map { it.noteId }.toSet()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Ini adalah 'toggleFavoriteStatus' yang dicari oleh ViewModel Anda
    suspend fun toggleFavoriteStatus(noteId: String) {
        val currentUser = supabase.auth.currentUserOrNull()
            ?: throw IllegalStateException("User tidak login, tidak bisa mengubah favorit.")

        val isCurrentlyFavorite = _favoriteStatus.value.contains(noteId)

        if (isCurrentlyFavorite) {
            // Jika sudah favorit, HAPUS
            supabase.from("favorites").delete {
                filter {
                    eq("user_id", currentUser.id)
                    eq("note_id", noteId)
                }
            }
        } else {
            // Jika belum favorit, TAMBAHKAN
            val newFavorite = FavoriteEntry(userId = currentUser.id, noteId = noteId)
            supabase.from("favorites").insert(newFavorite)
        }

        // Ambil ulang data terbaru untuk memastikan UI selalu sinkron
        fetchFavoriteStatus()
    }
>>>>>>> local-edit
}
