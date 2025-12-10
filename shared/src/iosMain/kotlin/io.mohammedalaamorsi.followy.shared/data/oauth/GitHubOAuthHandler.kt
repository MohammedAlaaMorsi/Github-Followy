package template.shared.data.oauth

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

/**
 * iOS implementation of OAuth handler
 * Uses Safari for OAuth flow
 */
actual class GitHubOAuthHandler {
    
    private var onSuccessCallback: ((String) -> Unit)? = null
    private var onErrorCallback: ((String) -> Unit)? = null
    
    actual fun startOAuthFlow(
        clientId: String,
        redirectUri: String,
        scopes: String,
        onSuccess: (code: String) -> Unit,
        onError: (String) -> Unit
    ) {
        this.onSuccessCallback = onSuccess
        this.onErrorCallback = onError
        
        val authUrl = buildAuthUrl(clientId, redirectUri, scopes)
        
        NSURL.URLWithString(authUrl)?.let { url ->
            UIApplication.sharedApplication.openURL(url)
        } ?: run {
            onError("Failed to create OAuth URL")
        }
    }
    
    actual fun isOAuthSupported(): Boolean = true
    
    private fun buildAuthUrl(clientId: String, redirectUri: String, scopes: String): String {
        return "${GitHubOAuthConfig.AUTHORIZE_URL}?" +
                "client_id=$clientId&" +
                "redirect_uri=${redirectUri.encodeURLParameter()}&" +
                "scope=${scopes.encodeURLParameter()}&" +
                "state=${generateState()}"
    }
    
    private fun generateState(): String {
        return System.currentTimeMillis().toString()
    }
    
    private fun String.encodeURLParameter(): String {
        return this.replace(" ", "%20")
            .replace(":", "%3A")
            .replace("/", "%2F")
    }
    
    /**
     * Call this from your AppDelegate to handle the OAuth callback
     */
    fun handleCallback(url: String) {
        val code = url.substringAfter("code=").substringBefore("&")
        if (code.isNotEmpty() && !code.contains("http")) {
            onSuccessCallback?.invoke(code)
        } else {
            onErrorCallback?.invoke("Invalid OAuth callback")
        }
    }
}
