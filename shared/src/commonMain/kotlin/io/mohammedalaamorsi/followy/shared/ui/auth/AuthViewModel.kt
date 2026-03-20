package io.mohammedalaamorsi.followy.shared.ui.auth

import io.mohammedalaamorsi.followy.shared.domain.usecase.*
import io.mohammedalaamorsi.followy.shared.ui.base.BaseMviViewModel

class AuthViewModel(
    private val validateAndAuthenticateUseCase: ValidateAndAuthenticateUseCase,
    private val exchangeOAuthCodeUseCase: ExchangeOAuthCodeUseCase,
    private val logoutUseCase: LogoutUseCase
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
                    isUserLoggedIn = true,
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
        
        exchangeOAuthCodeUseCase(
            intent.code,
            intent.clientId,
            intent.clientSecret,
            intent.redirectUri
        ).fold(
            onSuccess = { (user, token) ->
                setState(uiState.value.copy(
                    isLoading = false,
                    isInitializing = false,
                    isUserLoggedIn = true,
                    user = user,
                    token = token
                ))
                setEffect(AuthEffect.NavigateToDashboard(user))
            },
            onFailure = { error ->
                setState(uiState.value.copy(
                    isLoading = false,
                    loginError = "Exchange failed: ${error.message}"
                ))
            }
        )
    }

    private suspend fun checkToken() {
        validateAndAuthenticateUseCase.checkSavedToken()?.fold(
            onSuccess = { (user, token) ->
                setState(uiState.value.copy(
                    isLoading = false,
                    isInitializing = false,
                    isUserLoggedIn = true,
                    user = user,
                    token = token
                ))
                setEffect(AuthEffect.NavigateToDashboard(user))
            },
            onFailure = {
                setState(uiState.value.copy(isInitializing = false))
            }
        ) ?: run {
            setState(uiState.value.copy(isInitializing = false))
        }
    }

    private suspend fun logout() {
        logoutUseCase()
        setState(AuthUiState(isInitializing = false, isUserLoggedIn = false))
    }
}
