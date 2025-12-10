package com.example.finalprojectpam.data.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email // Cukup impor Email

class AuthRepository(private val supabase: SupabaseClient) {

	fun isUserLoggedIn(): Boolean {
		return supabase.auth.currentSessionOrNull() != null
	}

	suspend fun signUp(emailInput: String, passwordInput: String) {
		// Supabase secara default mengirim email verifikasi (bukan OTP login)
		// jika pengaturan verifikasi email diaktifkan.
		supabase.auth.signUpWith(Email) {
			email = emailInput
			password = passwordInput
		}
	}

	suspend fun signIn(emailInput: String, passwordInput: String) {
		// Menggunakan provider Email untuk Login dengan Password
		supabase.auth.signInWith(Email) {
			email = emailInput
			password = passwordInput
		}
	}

	suspend fun signOut() {
		supabase.auth.signOut()
	}
}
