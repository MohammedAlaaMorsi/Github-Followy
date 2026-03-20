package io.mohammedalaamorsi.followy.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.mohammedalaamorsi.followy.shared.ui.auth.AuthViewModel
import io.mohammedalaamorsi.followy.shared.ui.auth.LoginScreen
import io.mohammedalaamorsi.followy.shared.ui.auth.AuthIntent
import io.mohammedalaamorsi.followy.shared.ui.dashboard.DashboardScreen
import io.mohammedalaamorsi.followy.shared.ui.dashboard.DashboardViewModel
import io.mohammedalaamorsi.followy.shared.data.oauth.GitHubOAuthHandler
import io.mohammedalaamorsi.followy.shared.ui.profile.ProfileScreen
import io.mohammedalaamorsi.followy.shared.ui.profile.ProfileViewModel
import org.koin.compose.koinInject

@Composable
fun App() {
    val oauthHandler: GitHubOAuthHandler? = koinInject()
    val authViewModel: AuthViewModel = koinInject()
    val authState by authViewModel.uiState.collectAsState()
    
    var selectedUser by remember { mutableStateOf<Triple<String, Boolean, Int>?>(null) }
    
    when {
        authState.isInitializing || (authState.isLoading && !authState.isUserLoggedIn) -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        authState.isUserLoggedIn && authState.user != null -> {
            val userToView = selectedUser
            if (userToView != null) {
                val profileViewModel: ProfileViewModel = koinInject()
                ProfileScreen(
                    viewModel = profileViewModel,
                    username = userToView.first,
                    currentUsername = authState.user!!.login,
                    isRestricted = userToView.second,
                    onNavigateBack = { selectedUser = null }
                )
            } else {
                DashboardApp(
                    username = authState.user!!.login,
                    onLogout = { authViewModel.sendIntent(AuthIntent.Logout) },
                    onUserClick = { login, restricted, tab ->
                        selectedUser = Triple(login, restricted, tab)
                    }
                )
            }
        }
        
        else -> {
            LoginScreen(
                viewModel = authViewModel,
                oauthHandler = oauthHandler,
                onLoginSuccess = { _, _ -> /* Navigation handled by MVI effect in LoginScreen */ }
            )
        }
    }
}

@Composable
fun DashboardApp(
    username: String,
    onLogout: () -> Unit,
    onUserClick: (String, Boolean, Int) -> Unit = { _, _, _ -> },
    dashboardViewModel: DashboardViewModel = koinInject()
) {
    DashboardScreen(
        viewModel = dashboardViewModel,
        username = username,
        onLogout = onLogout,
        onSettingsClick = { /* TODO: Implement settings */ },
        onUserClick = onUserClick
    )
}
