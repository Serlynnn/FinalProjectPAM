// file: data/repository/StudyPlanRepository.kt

package com.example.finalprojectpam.data.repository

import com.example.finalprojectpam.data.model.StudyPlan
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

class StudyPlanRepository(private val supabase: SupabaseClient) {

    private val TABLE_NAME = "study_plan"

    // Fungsi CREATE: Tambah Jadwal Baru
    suspend fun addStudyPlan(title: String, date: String) {
        val currentUser = supabase.auth.currentUserOrNull() ?: throw Exception("No user logged in")

        val newPlan = StudyPlan(
            userId = currentUser.id,
            title = title,
            date = date
        )
        // Insert data ke Supabase
        supabase.postgrest[TABLE_NAME].insert(newPlan)
    }

    // Fungsi READ: Ambil semua jadwal milik user
    suspend fun getStudyPlans(): List<StudyPlan> {
        val currentUser = supabase.auth.currentUserOrNull() ?: return emptyList()

        return supabase.postgrest[TABLE_NAME]
            .select {
                // Filter hanya data milik user yang sedang login
                filter { eq("user_id", currentUser.id) }
            }
            .decodeList<StudyPlan>()
    }

    // Fungsi UPDATE: Ubah rencana belajar
    suspend fun updateStudyPlan(id: String, newTitle: String, newDate: String) {
        supabase.postgrest[TABLE_NAME].update(
            {
                set("title", newTitle)
                set("date", newDate)
            }
        ) {
            filter { eq("id", id) }
        }
    }

    // Fungsi DELETE: Hapus rencana belajar
    suspend fun deleteStudyPlan(id: String) {
        supabase.postgrest[TABLE_NAME].delete {
            filter { eq("id", id) }
        }
    }
}