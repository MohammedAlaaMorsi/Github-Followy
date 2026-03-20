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
    
    actual fun checkForCallback(): String? = null
}
