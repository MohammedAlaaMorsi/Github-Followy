package io.mohammedalaamorsi.followy.shared.data.oauth

import kotlinx.coroutines.flow.SharedFlow

/**
 * Platform-specific OAuth handler
 */
expect class GitHubOAuthHandler(context: Any? = null) {
    /**
     * Shared flow for OAuth callbacks (especially for deep links on mobile)
     */
    val callbackFlow: SharedFlow<String>

    /**
     * Initiate OAuth flow - opens browser/webview for authentication
     * Returns the authorization code via callback
     */
    fun startOAuthFlow(
        clientId: String,
        redirectUri: String,
        scopes: String,
        onSuccess: (code: String) -> Unit,
        onError: (String) -> Unit
    )
    
    /**
     * Check if OAuth is supported on this platform
     */
    fun isOAuthSupported(): Boolean
    
    /**
     * Check for any authorization code already present (e.g., in URL for web)
     */
    fun checkForCallback(): String?
}
