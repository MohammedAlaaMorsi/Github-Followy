package io.mohammedalaamorsi.followy.shared.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import io.mohammedalaamorsi.followy.shared.data.local.AuthTokenStorage
import io.mohammedalaamorsi.followy.shared.data.models.AuthState
import io.mohammedalaamorsi.followy.shared.data.oauth.GitHubOAuthService
import io.mohammedalaamorsi.followy.shared.data.repository.GitHubRepository
import io.mohammedalaamorsi.followy.shared.usecase.ValidateAndAuthenticateUseCase

class AuthViewModel(
    private val repository: GitHubRepository,
    private val oauthService: GitHubOAuthService? = null,
    private val validateAndAuthenticateUseCase: ValidateAndAuthenticateUseCase
) : ViewModel() {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    fun setAuthState(state: AuthState) {
        _authState.value = state
    }
    
    fun authenticateWithToken(token: String) {
        println("AuthViewModel: authenticateWithToken called with token: ${token.take(10)}...")
        
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            validateAndAuthenticateUseCase(token).fold(
                onSuccess = { (user, token) ->
                    println("AuthViewModel: Successfully got user: ${user.login}")
                    _authState.value = AuthState.Success(user, token)
                    println("AuthViewModel: AuthState set to Success")
                },
                onFailure = { error ->
                    println("AuthViewModel: Failed to get user: ${error.message}")
                    _authState.value = AuthState.Error(
                        error.message ?: "Authentication failed"
                    )
                }
            )
        }
    }
    
    fun exchangeOAuthCode(
        code: String,
        clientId: String,
        clientSecret: String,
        redirectUri: String
    ) {
        viewModelScope.launch {
            println("AuthViewModel: Starting OAuth code exchange...")
            _authState.value = AuthState.Loading
            
            oauthService?.exchangeCodeForToken(code, clientId, clientSecret, redirectUri)
                ?.fold(
                    onSuccess = { token ->
                        println("AuthViewModel: Got token, authenticating...")
                        // Now authenticate with the token
                        authenticateWithToken(token)
                    },
                    onFailure = { error ->
                        println("AuthViewModel: Failed to exchange code: ${error.message}")
                        _authState.value = AuthState.Error(
                            "Failed to exchange OAuth code: ${error.message}"
                        )
                    }
                ) ?: run {
                println("AuthViewModel: OAuth service not available")
                _authState.value = AuthState.Error("OAuth service not available")
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            AuthTokenStorage.clearToken()
            repository.clearAuthToken()
            _authState.value = AuthState.Idle
        }
    }
}
