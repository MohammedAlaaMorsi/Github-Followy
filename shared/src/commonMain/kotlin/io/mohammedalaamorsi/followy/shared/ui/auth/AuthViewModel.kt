package io.mohammedalaamorsi.followy.shared.ui.auth

import androidx.lifecycle.viewModelScope
import io.mohammedalaamorsi.followy.shared.data.local.AuthTokenStorage
import io.mohammedalaamorsi.followy.shared.data.models.AuthState
import io.mohammedalaamorsi.followy.shared.data.oauth.GitHubOAuthService
import io.mohammedalaamorsi.followy.shared.data.repository.GitHubRepository
import io.mohammedalaamorsi.followy.shared.ui.base.BaseMviViewModel
import io.mohammedalaamorsi.followy.shared.domain.usecase.ValidateAndAuthenticateUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: GitHubRepository,
    private val oauthService: GitHubOAuthService? = null,
    private val validateAndAuthenticateUseCase: ValidateAndAuthenticateUseCase
) : BaseMviViewModel<AuthUiState, AuthIntent, AuthEffect>(AuthUiState()) {

    init {
        sendIntent(AuthIntent.CheckSavedToken)
    }

    override suspend fun handleIntent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.LoginWithToken -> authenticate(intent.token)
            is AuthIntent.ExchangeOAuthCode -> exchangeCode(intent)
            is AuthIntent.Logout -> logout()
            is AuthIntent.CheckSavedToken -> checkToken()
        }
    }

    private suspend fun authenticate(token: String) {
        setState(uiState.value.copy(isLoading = true, loginError = null))
        
        validateAndAuthenticateUseCase(token).fold(
            onSuccess = { (user, token) ->
                setState(uiState.value.copy(
                    isLoading = false,
                    isInitializing = false,
                    user = user,
                    token = token
                ))
                setEffect(AuthEffect.NavigateToDashboard(user))
            },
            onFailure = { error ->
                setState(uiState.value.copy(
                    isLoading = false,
                    isInitializing = false,
                    loginError = error.message ?: "Authentication failed"
                ))
            }
        )
    }

    private suspend fun exchangeCode(intent: AuthIntent.ExchangeOAuthCode) {
        setState(uiState.value.copy(isLoading = true, loginError = null))
        
        oauthService?.exchangeCodeForToken(
            intent.code,
            intent.clientId,
            intent.clientSecret,
            intent.redirectUri
        )?.fold(
            onSuccess = { token ->
                authenticate(token)
            },
            onFailure = { error ->
                setState(uiState.value.copy(
                    isLoading = false,
                    loginError = "OAuth failed: ${error.message}"
                ))
            }
        ) ?: run {
            setState(uiState.value.copy(
                isLoading = false,
                loginError = "OAuth service unavailable"
            ))
        }
    }

    private suspend fun checkToken() {
        val savedToken = AuthTokenStorage.getToken()
        if (savedToken != null) {
            authenticate(savedToken)
        } else {
            setState(uiState.value.copy(isInitializing = false))
        }
    }

    fun logout() {
        viewModelScope.launch {
            AuthTokenStorage.clearToken()
            repository.clearAuthToken()
            setState(AuthUiState(isInitializing = false))
        }
    }
    
    // Legacy support for App.kt (can be removed once UI is refactored)
    val authStateFlow = uiState.map { state ->
        when {
            state.isInitializing -> AuthState.Initializing
            state.isLoading -> AuthState.Loading
            state.user != null && state.token != null -> AuthState.Success(state.user, state.token)
            state.loginError != null -> AuthState.Error(state.loginError)
            else -> AuthState.Idle
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AuthState.Initializing)

    // Bridge functions for legacy UI
    fun authenticateWithToken(token: String) = sendIntent(AuthIntent.LoginWithToken(token))
    fun exchangeOAuthCode(code: String, clientId: String, clientSecret: String, redirectUri: String) = 
        sendIntent(AuthIntent.ExchangeOAuthCode(code, clientId, clientSecret, redirectUri))
    fun logoutLegacy() = sendIntent(AuthIntent.Logout)
    fun setAuthState(state: AuthState) { /* No-op, managed by MVI */ }
}
