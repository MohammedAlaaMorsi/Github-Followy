package io.mohammedalaamorsi.followy.shared.data.oauth

import io.mohammedalaamorsi.followy.shared.data.oauth.GitHubOAuthConfig
import java.awt.Desktop
import java.net.URI
import java.net.URLEncoder

/**
 * Desktop implementation of OAuth handler
 * Opens system browser for OAuth flow
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
        
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI(authUrl))
                
                // For desktop, we need a local server to handle callback
                // For now, show instruction to user
                println("OAuth URL: $authUrl")
                println("After authorizing, you'll be redirected. Copy the 'code' parameter from the URL.")
            } else {
                onError("Browser not supported on this system")
            }
        } catch (e: Exception) {
            onError("Failed to open browser: ${e.message}")
        }
    }
    
    actual fun isOAuthSupported(): Boolean {
        return Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)
    }
    
    private fun buildAuthUrl(clientId: String, redirectUri: String, scopes: String): String {
        return "${GitHubOAuthConfig.AUTHORIZE_URL}?" +
                "client_id=$clientId&" +
                "redirect_uri=${URLEncoder.encode(redirectUri, "UTF-8")}&" +
                "scope=${URLEncoder.encode(scopes, "UTF-8")}&" +
                "state=${generateState()}"
    }
    
    private fun generateState(): String {
        return System.currentTimeMillis().toString()
    }
}
