package com.example.finalprojectpam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.finalprojectpam.data.auth.AuthRepository
import com.example.finalprojectpam.data.supabase.SupabaseProvider
import com.example.finalprojectpam.ui.auth.AuthScreen
import com.example.finalprojectpam.ui.auth.AuthViewModel
import com.example.finalprojectpam.ui.auth.AuthViewModelFactory

// --- ENUM untuk Rute Navigasi ---
// Gunakan sealed class/interface untuk mendefinisikan rute secara aman (type-safe)
sealed class Screen(val route: String) {
	object Auth : Screen("auth_route")
	object Home : Screen("home_route")
	object Category : Screen("category_route")
}

// ---------------------------------------------
// ## 1. Activity dan Inisialisasi Sesi Awal
// ---------------------------------------------

class MainActivity : ComponentActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val authRepository = AuthRepository(SupabaseProvider.client)

		val startRoute = if (authRepository.isUserLoggedIn()) {
			Screen.Home.route
		} else {
			Screen.Auth.route
		}

		setContent {
			AppNavigation(
				startDestination = startRoute,
				authRepository = authRepository
			)
		}
	}
}

// ---------------------------------------------
// ## 2. Composable Navigasi Utama (AppNavigation)
// ---------------------------------------------

@Composable
fun AppNavigation(
	startDestination: String,
	authRepository: AuthRepository
) {
	val navController = rememberNavController()

	// Membuat AuthViewModel menggunakan Factory (Injeksi Manual)
	// Pastikan AuthViewModelFactory sudah dibuat di folder ui/auth
	val authViewModel: AuthViewModel = viewModel(
		factory = AuthViewModelFactory(authRepository)
	)

	val categoryRepository = com.example.finalprojectpam.data.repository.CategoryRepository(com.example.finalprojectpam.data.supabase.SupabaseProvider.client)
	val categoryViewModel: com.example.finalprojectpam.ui.category.CategoryViewModel = viewModel(
		factory = com.example.finalprojectpam.ui.category.CategoryViewModelFactory(categoryRepository)
	)

	NavHost(navController = navController, startDestination = startDestination) {

		// Rute 1: Layar Login/Register
		composable(Screen.Auth.route) {
			AuthScreen(
				viewModel = authViewModel,
				onNavigateToHome = {
					// Pindah ke Home dan hapus layar Auth dari back stack
					navController.navigate(Screen.Home.route) {
						popUpTo(Screen.Auth.route) { inclusive = true }
					}
				}
			)
		}

		// Rute 2: Layar Utama (Catatan)
		composable(Screen.Home.route) {
			MainScreen(
				onLogout = {
					// 1. Panggil Logout ViewModel
					authViewModel.logout()

					// 2. Kembali ke layar Auth dan hapus Home dari back stack
					navController.navigate(Screen.Auth.route) {
						popUpTo(Screen.Home.route) { inclusive = true }
					}
				},
				// Tambahkan navigasi ke Kategori
				onNavigateToCategory = {
					navController.navigate(Screen.Category.route)
				}
			)
		}

		// Rute 3: Layar CRUD Kategori
		composable(Screen.Category.route) {
			com.example.finalprojectpam.ui.category.CategoryScreen(
				viewModel = categoryViewModel,
				onNavigateBack = { navController.popBackStack() }
			)
		}
	}
}

// ---------------------------------------------
// ## 3. Composable Layar Utama (Contoh MainScreen)
// ---------------------------------------------

@Composable
fun MainScreen(
	onLogout: () -> Unit,
	onNavigateToCategory: () -> Unit
) {
	Column(
		modifier = Modifier.fillMaxSize(),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		Text("Halaman Utama")

		Spacer(modifier = Modifier.height(16.dp))

		Button(onClick = onNavigateToCategory) {
			Text("Kelola Kategori (Anggota 3)")
		}

		Spacer(modifier = Modifier.height(16.dp))

		Button(onClick = onLogout, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
			Text("Logout")
		}
	}
}
