package io.mohammedalaamorsi.followy.shared.data.oauth

import java.awt.Desktop
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.URI
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Desktop implementation of OAuth handler
 * Opens system browser for OAuth flow and captures the callback automatically.
 */
actual class GitHubOAuthHandler actual constructor(context: Any?) {
    
    private var serverSocket: ServerSocket? = null
    private val port = 8080
    
    // For desktop, we primarily use the direct onSuccess callback, but bridge to flow for consistency
    private val _callbackFlow = MutableSharedFlow<String>(replay = 0, extraBufferCapacity = 1)
    actual val callbackFlow: SharedFlow<String> = _callbackFlow.asSharedFlow()

    actual fun startOAuthFlow(
        clientId: String,
        redirectUri: String,
        scopes: String,
        onSuccess: (code: String) -> Unit,
        onError: (String) -> Unit
    ) {
        // Desktop uses a custom loopback redirect URI
        val desktopRedirectUri = "http://127.0.0.1:$port/oauth/callback"
        val authUrl = buildAuthUrl(clientId, desktopRedirectUri, scopes)
        
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                // Start local server to capture callback
                startLocalServer(onSuccess, onError)
                
                // Open browser
                Desktop.getDesktop().browse(URI(authUrl))
                
                println("Opening browser for OAuth: $authUrl")
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
    
    actual fun checkForCallback(): String? = null
    
    private fun startLocalServer(onSuccess: (code: String) -> Unit, onError: (String) -> Unit) {
        // Close existing server if any
        serverSocket?.close()
        
        GlobalScope.launch(Dispatchers.IO) {
            try {
                serverSocket = ServerSocket(port)
                println("Local server started on port $port, waiting for callback...")
                
                val client = serverSocket?.accept() ?: return@launch
                val reader = BufferedReader(InputStreamReader(client.getInputStream()))
                val out = PrintWriter(client.getOutputStream())
                
                val line = reader.readLine()
                if (line != null && line.contains("GET")) {
                    val code = line.substringAfter("code=").substringBefore(" ").substringBefore("&")
                    
                    // Send success response to browser
                    out.println("HTTP/1.1 200 OK")
                    out.println("Content-Type: text/html")
                    out.println()
                    out.println("<html><body><h1>Authentication Successful!</h1><p>You can close this window now.</p></body></html>")
                    out.flush()
                    
                    _callbackFlow.emit(code)
                    
                    withContext(Dispatchers.Main) {
                        onSuccess(code)
                    }
                }
                
                client.close()
                serverSocket?.close()
                serverSocket = null
                
            } catch (e: Exception) {
                if (serverSocket != null) {
                    withContext(Dispatchers.Main) {
                        onError("Failed to capture callback: ${e.message}")
                    }
                }
            }
        }
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
