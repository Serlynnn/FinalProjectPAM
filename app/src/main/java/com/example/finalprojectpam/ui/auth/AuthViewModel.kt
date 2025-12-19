package com.example.finalprojectpam.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finalprojectpam.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// State UI sederhana
data class AuthUiState(
	val email: String = "",
	val password: String = "",
	val isLoading: Boolean = false,
	val error: String? = null,
	val isSuccess: Boolean = false
)

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

	private val _uiState = MutableStateFlow(AuthUiState())
	val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

	fun onEmailChange(input: String) {
		_uiState.update { it.copy(email = input, error = null) }
	}

	fun onPasswordChange(input: String) {
		_uiState.update { it.copy(password = input, error = null) }
	}

	fun login() {
		viewModelScope.launch {
			_uiState.update { it.copy(isLoading = true, error = null) }
			try {
				repository.signIn(_uiState.value.email, _uiState.value.password)
				_uiState.update { it.copy(isLoading = false, isSuccess = true) }
			} catch (e: Exception) {
				// Pesan error dari Supabase biasanya cukup deskriptif
				_uiState.update { it.copy(isLoading = false, error = e.message ?: "Login gagal") }
			}
		}
	}

	fun register() {
		viewModelScope.launch {
			_uiState.update { it.copy(isLoading = true, error = null) }
			try {
				repository.signUp(_uiState.value.email, _uiState.value.password)
				_uiState.update { it.copy(isLoading = false, error = "Sukses! Cek email untuk verifikasi.") }
			} catch (e: Exception) {
				_uiState.update { it.copy(isLoading = false, error = e.message ?: "Register gagal") }
			}
		}
	}

	// ⭐ FUNGSI LOGOUT YANG DITAMBAHKAN ⭐
	fun logout() {
		viewModelScope.launch {
			try {
				// 1. Panggil Repository untuk menghapus sesi di Supabase dan lokal
				repository.signOut()

				// 2. Reset State UI
				// Penting: Reset isSuccess menjadi false agar Composable AuthScreen dimuat kembali
				_uiState.update { AuthUiState() }
			} catch (e: Exception) {
				// Walaupun jarang, tangani jika logout gagal
				_uiState.update { it.copy(error = e.message ?: "Logout gagal!") }
			}
		}
	}
}
