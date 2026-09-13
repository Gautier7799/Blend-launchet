package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.di.AppContainer
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.auth.AuthViewModel
import com.example.ui.screens.chat.ChatScreen
import com.example.ui.screens.chat.ChatViewModel
import com.example.ui.screens.editor.EditorScreen
import com.example.ui.screens.editor.EditorViewModel
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.home.HomeViewModel
import com.example.ui.screens.viewer.ViewerScreen
import com.example.ui.screens.viewer.ViewerViewModel

@Composable
fun AppNavigation(appContainer: AppContainer) {
    val navController = rememberNavController()
    val isUserLoggedIn = appContainer.auth.currentUser != null
    val startDestination = if (isUserLoggedIn) "home" else "auth"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("auth") {
            val authViewModel: AuthViewModel = viewModel(
                factory = AuthViewModel.provideFactory(appContainer.authRepository)
            )
            AuthScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }
        
        composable("home") {
            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModel.provideFactory(
                    appContainer.documentRepository,
                    appContainer.authRepository
                )
            )
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToEditor = { navController.navigate("editor") },
                onNavigateToViewer = { docId -> navController.navigate("viewer/$docId") },
                onNavigateToChat = { navController.navigate("chat") },
                onLogout = {
                    navController.navigate("auth") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        composable("chat") {
            val chatViewModel: ChatViewModel = viewModel(
                factory = ChatViewModel.provideFactory(appContainer.geminiHelper)
            )
            ChatScreen(
                viewModel = chatViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable("editor") {
            val editorViewModel: EditorViewModel = viewModel(
                factory = EditorViewModel.provideFactory(
                    appContainer.documentRepository,
                    appContainer.geminiHelper
                )
            )
            EditorScreen(
                viewModel = editorViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable("viewer/{docId}") { backStackEntry ->
            val docId = backStackEntry.arguments?.getString("docId") ?: ""
            val viewerViewModel: ViewerViewModel = viewModel(
                factory = ViewerViewModel.provideFactory(
                    appContainer.documentRepository,
                    appContainer.noteRepository,
                    appContainer.geminiHelper
                )
            )
            ViewerScreen(
                viewModel = viewerViewModel,
                documentId = docId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
