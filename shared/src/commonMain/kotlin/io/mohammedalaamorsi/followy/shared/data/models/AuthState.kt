package io.mohammedalaamorsi.followy.shared.data.models

sealed class AuthState {
    data object Initializing : AuthState() // Checking for saved token
    data object Idle : AuthState() // No saved token, ready for login
    data object Loading : AuthState() // Authenticating
    data class Success(val user: GitHubUser, val token: String) : AuthState()
    data class Error(val message: String) : AuthState()
}
