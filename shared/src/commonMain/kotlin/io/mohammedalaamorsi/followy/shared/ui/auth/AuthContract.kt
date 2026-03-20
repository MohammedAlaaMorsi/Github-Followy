package io.mohammedalaamorsi.followy.shared.ui.auth

import io.mohammedalaamorsi.followy.shared.data.models.GitHubUser
import io.mohammedalaamorsi.followy.shared.ui.base.UiEffect
import io.mohammedalaamorsi.followy.shared.ui.base.UiIntent
import io.mohammedalaamorsi.followy.shared.ui.base.UiState

sealed class AuthIntent : UiIntent {
    data class LoginWithToken(val token: String) : AuthIntent()
    data class ExchangeOAuthCode(
        val code: String, 
        val clientId: String, 
        val clientSecret: String, 
        val redirectUri: String
    ) : AuthIntent()
    data object Logout : AuthIntent()
    data object CheckSavedToken : AuthIntent()
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val loginError: String? = null,
    val isInitializing: Boolean = true,
    val user: GitHubUser? = null,
    val token: String? = null
) : UiState

sealed class AuthEffect : UiEffect {
    data class NavigateToDashboard(val user: GitHubUser) : AuthEffect()
    data class ShowError(val message: String) : AuthEffect()
}
