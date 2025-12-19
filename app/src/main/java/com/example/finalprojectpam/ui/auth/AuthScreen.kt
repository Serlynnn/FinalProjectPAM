package com.example.finalprojectpam.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreen(
	viewModel: AuthViewModel,
	onNavigateToHome: () -> Unit
) {
	val state by viewModel.uiState.collectAsState()

	// Cek jika login sukses, pindah halaman
	LaunchedEffect(state.isSuccess) {
		if (state.isSuccess) {
			onNavigateToHome()
		}
	}

	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(16.dp),
		verticalArrangement = Arrangement.Center,
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Text(text = "Selamat Datang", style = MaterialTheme.typography.headlineMedium)
		Spacer(modifier = Modifier.height(24.dp))

		// Input Email
		OutlinedTextField(
			value = state.email,
			onValueChange = { viewModel.onEmailChange(it) },
			label = { Text("Email") },
			modifier = Modifier.fillMaxWidth()
		)

		Spacer(modifier = Modifier.height(8.dp))

		// Input Password
		OutlinedTextField(
			value = state.password,
			onValueChange = { viewModel.onPasswordChange(it) },
			label = { Text("Password") },
			visualTransformation = PasswordVisualTransformation(),
			modifier = Modifier.fillMaxWidth()
		)

		Spacer(modifier = Modifier.height(16.dp))

		if (state.isLoading) {
			CircularProgressIndicator()
		} else {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceEvenly
			) {
				Button(onClick = { viewModel.login() }) {
					Text("Login")
				}
				OutlinedButton(onClick = { viewModel.register() }) {
					Text("Register")
				}
			}
		}

		// Tampilkan Error jika ada
		state.error?.let { errorMsg ->
			Spacer(modifier = Modifier.height(16.dp))
			Text(text = errorMsg, color = MaterialTheme.colorScheme.error)
		}
	}
}
