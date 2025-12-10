package io.mohammedalaamorsi.followy.shared.data.oauth

import io.mohammedalaamorsi.followy.shared.data.oauth.GitHubOAuthConfig
import kotlinx.browser.window

/**
 * Web implementation of OAuth handler
 * Uses window redirect for OAuth flow
 */
actual class GitHubOAuthHandler {
    
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
        return js("Date.now()").toString()
    }
    
    private fun encodeURIComponent(str: String): String {
        return js("encodeURIComponent(str)") as String
    }
    
    /**
     * Check if current URL contains OAuth callback
     */
    fun checkForCallback(): String? {
        val url = window.location.href
        val params = window.location.search
        
        if (params.contains("code=")) {
            return params.substringAfter("code=").substringBefore("&")
        }
        
        return null
    }
}
