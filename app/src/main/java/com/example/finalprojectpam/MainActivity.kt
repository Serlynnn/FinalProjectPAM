package com.example.finalprojectpam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

import com.example.finalprojectpam.data.auth.AuthRepository
import com.example.finalprojectpam.data.supabase.SupabaseProvider
import com.example.finalprojectpam.ui.auth.AuthScreen
import com.example.finalprojectpam.ui.auth.AuthViewModel
import com.example.finalprojectpam.ui.auth.AuthViewModelFactory

import com.example.finalprojectpam.data.repository.CategoryRepository
import com.example.finalprojectpam.data.repository.StorageRepository
import com.example.finalprojectpam.ui.category.CategoryViewModel
import com.example.finalprojectpam.ui.category.CategoryViewModelFactory
import com.example.finalprojectpam.ui.category.CategoryScreen

import com.example.finalprojectpam.data.repository.NoteRepository
import com.example.finalprojectpam.ui.note.NoteViewModel
import com.example.finalprojectpam.ui.note.NoteViewModelFactory
import com.example.finalprojectpam.ui.note.NotesListScreen
import com.example.finalprojectpam.ui.note.NoteEntryScreen

import com.example.finalprojectpam.data.repository.StudyPlanRepository
import com.example.finalprojectpam.ui.studyplan.StudyPlanScreen
import com.example.finalprojectpam.ui.studyplan.StudyPlanViewModel

sealed class Screen(val route: String) {
	object Auth : Screen("auth_route")
	object Home : Screen("home_route")
	object Category : Screen("category_route")
	object NotesList : Screen("notes_list_route")
	object NoteEntry : Screen("note_entry_route/{noteId}") {
		fun createRoute(noteId: String) = "note_entry_route/$noteId"
	}
	object StudyPlan : Screen("study_plan_route")
}

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val authRepository = AuthRepository(SupabaseProvider.client)
		val startRoute = if (authRepository.isUserLoggedIn()) Screen.Home.route else Screen.Auth.route

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
	val supabaseClient = SupabaseProvider.client

	val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(authRepository))

	val categoryRepository = CategoryRepository(supabaseClient)
	val storageRepository = StorageRepository(supabaseClient)
	val categoryViewModel: CategoryViewModel = viewModel(
		factory = CategoryViewModelFactory(categoryRepository, storageRepository)
	)

	val noteRepository = NoteRepository(supabaseClient)
	val noteViewModel: NoteViewModel = viewModel(
		factory = NoteViewModelFactory(noteRepository, categoryRepository)
	)

	val studyPlanRepository = StudyPlanRepository(supabaseClient)
	val studyPlanViewModel: StudyPlanViewModel = viewModel(
		factory = StudyPlanViewModel.Factory(studyPlanRepository)
	)

	NavHost(navController = navController, startDestination = startDestination) {

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

		composable(Screen.Home.route) {
			MainScreen(
				onLogout = {
					authViewModel.logout()
					navController.navigate(Screen.Auth.route) {
						popUpTo(Screen.Home.route) { inclusive = true }
					}
				},
				onNavigateToCategory = { navController.navigate(Screen.Category.route) },
				onNavigateToNotes = { navController.navigate(Screen.NotesList.route) },
				onNavigateToStudyPlan = { navController.navigate(Screen.StudyPlan.route) }
			)
		}

		composable(Screen.Category.route) {
			CategoryScreen(
				viewModel = categoryViewModel,
				onNavigateBack = { navController.popBackStack() }
			)
		}

		// RUTE LIST NOTES
		composable(Screen.NotesList.route) {
			NotesListScreen(
				viewModel = noteViewModel,
				onNavigateToDetail = { noteId ->
					navController.navigate(Screen.NoteEntry.createRoute(noteId))
				},
				onNavigateBack = { navController.popBackStack() }
			)
		}

		// RUTE ENTRY NOTES (TAMBAH/EDIT)
		composable(
			route = Screen.NoteEntry.route,
			arguments = listOf(navArgument("noteId") { type = NavType.StringType })
		) { backStackEntry ->
			val noteId = backStackEntry.arguments?.getString("noteId") ?: "new"
			NoteEntryScreen(
				noteId = noteId,
				viewModel = noteViewModel,
				onSaveSuccess = { navController.popBackStack() }
			)
		}

		composable(Screen.StudyPlan.route) {
			StudyPlanScreen(
				viewModel = studyPlanViewModel,
				onNavigateBack = { navController.popBackStack() }
			)
		}
	}
}

@Composable
fun MainScreen(
	onLogout: () -> Unit,
	onNavigateToCategory: () -> Unit,
	onNavigateToNotes: () -> Unit,
	onNavigateToStudyPlan: () -> Unit
) {
	Column(
		modifier = Modifier.fillMaxSize(),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		Text("Halaman Utama", style = MaterialTheme.typography.headlineMedium)

		Spacer(modifier = Modifier.height(24.dp))

		// Tombol Catatan
		Button(
			onClick = onNavigateToNotes,
			modifier = Modifier.fillMaxWidth(0.7f)
		) {
			Text("Kelola Catatan")
		}

		Spacer(modifier = Modifier.height(16.dp))

		// Tombol Kategori
		Button(
			onClick = onNavigateToCategory,
			modifier = Modifier.fillMaxWidth(0.7f)
		) {
			Text("Kelola Kategori")
		}

		Spacer(modifier = Modifier.height(16.dp)) // Jarak antar tombol

		// ⭐ NAVIGASI KE STUDY PLAN
		Button(
			onClick = onNavigateToStudyPlan,
			colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
		) {
			Text("Rencana Belajar")
		}

		Spacer(modifier = Modifier.height(32.dp))

		Button(
			onClick = onLogout,
			colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
		) {
			Text("Logout")
		}
	}
}
