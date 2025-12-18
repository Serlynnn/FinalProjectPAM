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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// Import Auth
import com.example.finalprojectpam.data.auth.AuthRepository
import com.example.finalprojectpam.data.supabase.SupabaseProvider
import com.example.finalprojectpam.ui.auth.AuthScreen
import com.example.finalprojectpam.ui.auth.AuthViewModel
import com.example.finalprojectpam.ui.auth.AuthViewModelFactory

// Import Notes (Fitur Anggota 1 & 2 Gabungan)
import com.example.finalprojectpam.data.repository.NoteRepository
import com.example.finalprojectpam.ui.note.NoteEntryScreen
import com.example.finalprojectpam.ui.note.NoteViewModel
import com.example.finalprojectpam.ui.note.NoteViewModelFactory
import com.example.finalprojectpam.ui.note.NotesListScreen

//// Import Category (Fitur Anggota 3)
//import com.example.finalprojectpam.data.repository.CategoryRepository
//import com.example.finalprojectpam.ui.category.CategoryScreen
//import com.example.finalprojectpam.ui.category.CategoryViewModel
//import com.example.finalprojectpam.ui.category.CategoryViewModelFactory


// Rute Navigasi
sealed class Screen(val route: String) {
	object Auth : Screen("auth_route")
	object Home : Screen("home_route")
	object Category : Screen("category_route")

	// Rute untuk Fitur Catatan (Gabungan Text + Gambar)
	object NotesList : Screen("notes_list_route")
	object NoteEntry : Screen("note_entry_route/{noteId}") {
		fun createRoute(noteId: String) = "note_entry_route/$noteId"
	}
}

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val authRepository = AuthRepository(SupabaseProvider.client)

		// Cek Login state untuk menentukan halaman awal
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

@Composable
fun AppNavigation(
	startDestination: String,
	authRepository: AuthRepository
) {
	val navController = rememberNavController()

	// 1. Setup ViewModel Auth
	val authViewModel: AuthViewModel = viewModel(
		factory = AuthViewModelFactory(authRepository)
	)

//	// 2. Setup ViewModel Category
//	val categoryRepository = CategoryRepository(SupabaseProvider.client)
//	val categoryViewModel: CategoryViewModel = viewModel(
//		factory = CategoryViewModelFactory(categoryRepository)
//	)

	// 3. Setup ViewModel Notes
	val noteRepository = NoteRepository(SupabaseProvider.client)
	val noteViewModel: NoteViewModel = viewModel(
		factory = NoteViewModelFactory(noteRepository)
	)

	NavHost(navController = navController, startDestination = startDestination) {

		// Auth
		composable(Screen.Auth.route) {
			AuthScreen(
				viewModel = authViewModel,
				onNavigateToHome = {
					navController.navigate(Screen.Home.route) {
						popUpTo(Screen.Auth.route) { inclusive = true }
					}
				}
			)
		}

		// Home
		composable(Screen.Home.route) {
			MainScreen(
				onLogout = {
					authViewModel.logout()
					navController.navigate(Screen.Auth.route) {
						popUpTo(Screen.Home.route) { inclusive = true }
					}
				},
				onNavigateToCategory = {
					navController.navigate(Screen.Category.route)
				},
				onNavigateToNotes = {
					// Masuk ke List Catatan
					navController.navigate(Screen.NotesList.route)
				}
			)
		}

//		// Category
//		composable(Screen.Category.route) {
//			CategoryScreen(
//				viewModel = categoryViewModel,
//				onNavigateBack = { navController.popBackStack() }
//			)
//		}

		// Notes List (Layar Daftar Catatan)
		composable(Screen.NotesList.route) {
			NotesListScreen(
				viewModel = noteViewModel,
				onNavigateToDetail = { noteId ->
					// Klik item atau tombol tambah -> Pindah ke NoteEntry
					navController.navigate(Screen.NoteEntry.createRoute(noteId))
				}
			)
		}

		// Note Entry (Layar Tambah/Edit + Upload Gambar)
		composable(Screen.NoteEntry.route) { backStackEntry ->
			val noteId = backStackEntry.arguments?.getString("noteId") ?: "new"
			NoteEntryScreen(
				noteId = noteId,
				viewModel = noteViewModel,
				onSaveSuccess = { navController.popBackStack() }
			)
		}
	}
}

@Composable
fun MainScreen(
	onLogout: () -> Unit,
	onNavigateToCategory: () -> Unit,
	onNavigateToNotes: () -> Unit
) {
	Column(
		modifier = Modifier.fillMaxSize(),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		Text("Halaman Utama", style = MaterialTheme.typography.headlineMedium)

		Spacer(modifier = Modifier.height(32.dp))

		// Tombol ke Fitur Anggota 3
		Button(
			onClick = onNavigateToCategory,
			modifier = Modifier.fillMaxWidth(0.7f)
		) {
			Text("Kelola Kategori")
		}

		Spacer(modifier = Modifier.height(16.dp))

		// Tombol ke Fitur Anggota 1 & 2 (Gabungan)
		Button(
			onClick = onNavigateToNotes,
			modifier = Modifier.fillMaxWidth(0.7f),
			colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009688))
		) {
			Text("Catatan Saya (Teks & Gambar)")
		}

		Spacer(modifier = Modifier.height(32.dp))

		// Tombol Logout
		Button(
			onClick = onLogout,
			colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
			modifier = Modifier.fillMaxWidth(0.5f)
		) {
			Text("Logout")
		}
	}
}