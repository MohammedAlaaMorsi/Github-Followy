package io.mohammedalaamorsi.followy.shared.data.oauth

import kotlinx.browser.window
import kotlinx.datetime.Clock
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Web implementation of OAuth handler
 * Uses window redirect for OAuth flow
 */
actual class GitHubOAuthHandler actual constructor(context: Any?) {
    
    // Web handles callbacks via page reload and URL parsing, but we provide the flow for consistency
    private val _callbackFlow = MutableSharedFlow<String>(replay = 0, extraBufferCapacity = 1)
    actual val callbackFlow: SharedFlow<String> = _callbackFlow.asSharedFlow()
    
    actual fun startOAuthFlow(
        clientId: String,
        redirectUri: String,
        scopes: String,
        onSuccess: (code: String) -> Unit,
        onError: (String) -> Unit
    ) {
        val authUrl = buildAuthUrl(clientId, redirectUri, scopes)
        window.location.href = authUrl
    }
    
    actual fun isOAuthSupported(): Boolean = true
    
    private fun buildAuthUrl(clientId: String, redirectUri: String, scopes: String): String {
        return "${GitHubOAuthConfig.AUTHORIZE_URL}?" +
                "client_id=$clientId&" +
                "redirect_uri=${encodeURIComponent(redirectUri)}&" +
                "scope=${encodeURIComponent(scopes)}&" +
                "state=${generateState()}"
    }
    
    private fun generateState(): String {
        return Clock.System.now().toEpochMilliseconds().toString()
    }
    
    /**
     * Check if current URL contains OAuth callback
     */
    actual fun checkForCallback(): String? {
        val params = window.location.search
        println("GitHubOAuthHandler: Checking for callback in URL. Params: $params")
        
        if (params.contains("code=")) {
            val code = params.substringAfter("code=").substringBefore("&")
            println("GitHubOAuthHandler: Found code: $code")
            return code
        }
        
        return null
    }
}

private fun encodeURIComponent(str: String): String = js("encodeURIComponent(str)")
