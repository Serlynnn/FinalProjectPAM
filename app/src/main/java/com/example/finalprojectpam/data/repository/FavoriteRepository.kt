package com.example.finalprojectpam.data.repository

import com.example.finalprojectpam.data.supabase.SupabaseProvider
import io.github.jan.supabase.SupabaseClient
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
}
