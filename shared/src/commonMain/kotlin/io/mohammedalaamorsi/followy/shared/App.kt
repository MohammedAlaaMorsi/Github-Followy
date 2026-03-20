package io.mohammedalaamorsi.followy.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import io.mohammedalaamorsi.followy.shared.data.models.AuthState
import io.mohammedalaamorsi.followy.shared.data.local.AuthTokenStorage
import io.mohammedalaamorsi.followy.shared.ui.auth.AuthViewModel
import io.mohammedalaamorsi.followy.shared.ui.auth.LoginScreen
import io.mohammedalaamorsi.followy.shared.ui.dashboard.DashboardScreen
import io.mohammedalaamorsi.followy.shared.ui.dashboard.DashboardViewModel
import io.mohammedalaamorsi.followy.shared.data.oauth.GitHubOAuthHandler
import org.koin.compose.koinInject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Composable
fun App() {
    val oauthHandler: GitHubOAuthHandler? = koinInject()
    val authViewModel: AuthViewModel = koinInject()
    
    // Check for existing token on app start
    LaunchedEffect(Unit) {
        val savedToken = AuthTokenStorage.getToken()
        if (savedToken != null) {
            authViewModel.authenticateWithToken(savedToken)
        } else {
            authViewModel.setAuthState(AuthState.Idle)
        }
    }
    
    val authState by authViewModel.authState.collectAsState()
    var currentUser by remember { mutableStateOf<String?>(null) }
    val currentState = authState
    
    when (currentState) {
        is AuthState.Initializing -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        is AuthState.Idle -> {
            LoginScreen(
                viewModel = authViewModel,
                oauthHandler = oauthHandler,
                onLoginSuccess = { username, _ ->
                    currentUser = username
                }
            )
        }
        
        is AuthState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        is AuthState.Success -> {
            currentUser = currentState.user.login
            DashboardApp(
                username = currentState.user.login,
                onLogout = { authViewModel.logout() }
            )
        }
        
        is AuthState.Error -> {
            LoginScreen(
                viewModel = authViewModel,
                oauthHandler = oauthHandler,
                onLoginSuccess = { username, _ ->
                    currentUser = username
                }
            )
        }
    }
}

@Composable
fun DashboardApp(
    username: String,
    onLogout: () -> Unit,
    dashboardViewModel: DashboardViewModel = koinInject()
) {
    DashboardScreen(
        viewModel = dashboardViewModel,
        username = username,
        onLogout = onLogout,
        onSettingsClick = { /* TODO: Implement settings */ }
    )
}
