package io.mohammedalaamorsi.followy.shared

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.koin.compose.KoinContext
import org.koin.compose.getKoin
import io.mohammedalaamorsi.followy.shared.data.models.AuthState
import io.mohammedalaamorsi.followy.shared.data.oauth.provideOAuthHandler
import io.mohammedalaamorsi.followy.shared.ui.auth.AuthViewModel
import io.mohammedalaamorsi.followy.shared.ui.auth.HandleOAuthCallbacks
import io.mohammedalaamorsi.followy.shared.ui.auth.LoginScreen
import io.mohammedalaamorsi.followy.shared.ui.dashboard.DashboardScreen
import io.mohammedalaamorsi.followy.shared.ui.dashboard.DashboardViewModel
import io.mohammedalaamorsi.followy.shared.ui.profile.ProfileScreen
import io.mohammedalaamorsi.followy.shared.ui.profile.ProfileViewModel
import io.mohammedalaamorsi.followy.shared.ui.settings.SettingsScreen

// Type-safe navigation routes
@Serializable
object LoginRoute

@Serializable
data class DashboardRoute(val username: String)

@Serializable
data class ProfileRoute(
    val username: String,
    val currentUsername: String,
    val isRestricted: Boolean
)

@Serializable
object SettingsRoute

@Composable
fun App() {
    MaterialTheme {
        KoinContext {
            val koin = getKoin()
            val authViewModel: AuthViewModel = remember { koin.get() }
            val dashboardViewModel: DashboardViewModel = remember { koin.get() }
            val authState by authViewModel.authState.collectAsState()
            val oauthHandler = remember { provideOAuthHandler() }
            val navController = rememberNavController()

            // Handle OAuth callbacks
            HandleOAuthCallbacks(authViewModel)

            // Navigate based on auth state
            LaunchedEffect(authState) {
                when (authState) {
                    is AuthState.Success -> {
                        val successState = authState as AuthState.Success
                        navController.navigate(DashboardRoute(successState.user.login)) {
                            popUpTo<LoginRoute> { inclusive = true }
                        }
                    }

                    is AuthState.Idle -> {
                        navController.navigate(LoginRoute) {
                            popUpTo(0) { inclusive = true }
                        }
                    }

                    else -> {}
                }
            }

            NavHost(
                navController = navController,
                startDestination = if (authState is AuthState.Success) {
                    DashboardRoute((authState as AuthState.Success).user.login)
                } else {
                    LoginRoute
                }
            ) {
                composable<LoginRoute> {
                    LoginScreen(
                        viewModel = authViewModel,
                        oauthHandler = oauthHandler,
                        onLoginSuccess = { _, _ -> }
                    )
                }

                composable<DashboardRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<DashboardRoute>()

                    // Use saved state to preserve tab selection
                    val savedStateHandle = backStackEntry.savedStateHandle
                    val selectedTab = savedStateHandle.get<Int>("selectedTab") ?: 0

                    DashboardScreen(
                        viewModel = dashboardViewModel,
                        username = route.username,
                        initialTab = selectedTab,
                        onLogout = { authViewModel.logout() },
                        onSettingsClick = {
                            navController.navigate(SettingsRoute)
                        },
                        onUserClick = { clickedUsername, isRestricted, currentTab ->
                            // Save current tab before navigating
                            savedStateHandle["selectedTab"] = currentTab
                            navController.navigate(
                                ProfileRoute(
                                    username = clickedUsername,
                                    currentUsername = route.username,
                                    isRestricted = isRestricted
                                )
                            )
                        }
                    )
                }
                composable<ProfileRoute> { backStackEntry ->
                    val route = backStackEntry.toRoute<ProfileRoute>()
                    val profileViewModel: ProfileViewModel = remember { koin.get() }

                    ProfileScreen(
                        viewModel = profileViewModel,
                        username = route.username,
                        currentUsername = route.currentUsername,
                        isRestricted = route.isRestricted,
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onFollowToggle = { targetUsername, shouldFollow ->
                            if (shouldFollow) {
                                dashboardViewModel.followUser(targetUsername)
                            } else {
                                dashboardViewModel.unfollowUser(targetUsername)
                            }
                        }
                    )
                }

                composable<SettingsRoute> {
                    SettingsScreen(
                        appVersion = "1.0.0",
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onLogout = {
                            authViewModel.logout()
                        }
                    )
                }

            }
        }
    }
}
