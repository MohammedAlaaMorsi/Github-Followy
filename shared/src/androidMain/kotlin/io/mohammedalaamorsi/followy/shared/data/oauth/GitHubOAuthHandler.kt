package io.mohammedalaamorsi.followy.shared.data.oauth

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Android implementation of OAuth handler
 * Uses Chrome Custom Tabs for OAuth flow
 */
actual class GitHubOAuthHandler(private val context: Context) {
    
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
        
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            onError("Failed to open browser: ${e.message}")
        }
    }
    
    actual fun isOAuthSupported(): Boolean = true
    
    actual fun checkForCallback(): String? = null
    
    private fun buildAuthUrl(clientId: String, redirectUri: String, scopes: String): String {
        return "${GitHubOAuthConfig.AUTHORIZE_URL}?" +
                "client_id=$clientId&" +
                "redirect_uri=${Uri.encode(redirectUri)}&" +
                "scope=${Uri.encode(scopes)}&" +
                "state=${generateState()}"
    }
    
    private fun generateState(): String {
        return System.currentTimeMillis().toString()
    }
    
    /**
     * Call this from your Activity's onNewIntent to handle the OAuth callback
     */
    fun handleCallback(uri: Uri) {
        val code = uri.getQueryParameter("code")
        val error = uri.getQueryParameter("error")
        
        when {
            code != null -> onSuccessCallback?.invoke(code)
            error != null -> onErrorCallback?.invoke("OAuth error: $error")
            else -> onErrorCallback?.invoke("Invalid OAuth callback")
        }
    }
}
