package followy

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import io.mohammedalaamorsi.followy.shared.App
import io.mohammedalaamorsi.followy.shared.data.oauth.GitHubOAuthConfig
import io.mohammedalaamorsi.followy.shared.data.oauth.initOAuth

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        // Load OAuth credentials from BuildConfig
        GitHubOAuthConfig.clientId = BuildConfig.GITHUB_CLIENT_ID
        GitHubOAuthConfig.clientSecret = BuildConfig.GITHUB_CLIENT_SECRET
        
        // Initialize OAuth with context
        initOAuth(this)
        
        // Handle OAuth callback if this is a callback intent
        handleOAuthCallback(intent)

        setContent {
            enableEdgeToEdge()
            App()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleOAuthCallback(intent)
    }

    private fun handleOAuthCallback(intent: Intent?) {
        intent?.data?.let { uri ->
            if (uri.scheme == "githubfollowy" && uri.host == "oauth") {
                // Emit callback to shared flow for the UI to handle
                io.mohammedalaamorsi.followy.shared.data.oauth.handleOAuthCallback(uri)
            }
        }
    }
}
