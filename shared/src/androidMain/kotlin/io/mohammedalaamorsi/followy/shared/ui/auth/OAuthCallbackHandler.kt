package io.mohammedalaamorsi.followy.shared.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.collectLatest
import io.mohammedalaamorsi.followy.shared.data.oauth.GitHubOAuthConfig
import io.mohammedalaamorsi.followy.shared.ui.auth.AuthViewModel
import io.mohammedalaamorsi.followy.shared.data.oauth.oauthCallbackFlow

@Composable
actual fun HandleOAuthCallbacks(authViewModel: AuthViewModel) {
    LaunchedEffect(Unit) {
        oauthCallbackFlow.collectLatest { uri ->
            println("OAuth Callback: Received URI = $uri")
            
            // Extract the authorization code from the callback URI
            val code = uri.getQueryParameter("code")
            val error = uri.getQueryParameter("error")
            
            when {
                code != null -> {
                    println("OAuth Callback: Got code = $code")
                    // Exchange the code for a token
                    authViewModel.exchangeOAuthCode(
                        code = code,
                        clientId = GitHubOAuthConfig.clientId,
                        clientSecret = GitHubOAuthConfig.clientSecret,
                        redirectUri = GitHubOAuthConfig.redirectUri
                    )
                }
                error != null -> {
                    // Handle OAuth error
                    println("OAuth Callback: Error = $error")
                }
                else -> {
                    println("OAuth Callback: No code or error in URI")
                }
            }
        }
    }
}
