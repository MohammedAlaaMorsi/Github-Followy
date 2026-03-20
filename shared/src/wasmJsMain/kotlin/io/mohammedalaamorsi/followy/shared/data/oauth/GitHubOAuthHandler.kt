package io.mohammedalaamorsi.followy.shared.data.oauth

import io.mohammedalaamorsi.followy.shared.data.oauth.GitHubOAuthConfig
import kotlinx.browser.window
import kotlinx.datetime.Clock

/**
 * Web implementation of OAuth handler
 * Uses window redirect for OAuth flow
 */
actual class GitHubOAuthHandler actual constructor() {
    
    actual fun startOAuthFlow(
        clientId: String,
        redirectUri: String,
        scopes: String,
        onSuccess: (code: String) -> Unit,
        onError: (String) -> Unit
    ) {
        val authUrl = buildAuthUrl(clientId, redirectUri, scopes)
        
        // Store callbacks in session storage for retrieval after redirect
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
        
        if (params.contains("code=")) {
            return params.substringAfter("code=").substringBefore("&")
        }
        
        return null
    }
}

private fun encodeURIComponent(str: String): String = js("encodeURIComponent(str)")
