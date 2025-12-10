package followy

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import io.mohammedalaamorsi.followy.shared.App
import io.mohammedalaamorsi.followy.shared.data.oauth.GitHubOAuthConfig
import io.mohammedalaamorsi.followy.shared.usecase.ValidateAndAuthenticateUseCase
import io.mohammedalaamorsi.followy.shared.ui.auth.AuthViewModel
import io.mohammedalaamorsi.followy.shared.data.models.AuthState
import io.mohammedalaamorsi.followy.shared.data.oauth.initOAuth

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by inject()
    private val validateAndAuthenticateUseCase: ValidateAndAuthenticateUseCase by inject()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        // Load OAuth credentials from BuildConfig
        GitHubOAuthConfig.clientId = BuildConfig.GITHUB_CLIENT_ID
        GitHubOAuthConfig.clientSecret = BuildConfig.GITHUB_CLIENT_SECRET
        
        // Initialize OAuth with context
        initOAuth(this)
        
        // Check for saved token BEFORE setting content
        runBlocking {
            println("MainActivity: Starting token check...")
            val result = validateAndAuthenticateUseCase.checkSavedToken()
            if (result != null) {
                result.fold(
                    onSuccess = { (user, token) ->
                        println("MainActivity: Auto-login successful for ${user.login}")
                        authViewModel.setAuthState(AuthState.Success(user, token))
                        println("MainActivity: AuthState set to Success")
                    },
                    onFailure = { error ->
                        println("MainActivity: Auto-login failed: ${error.message}")
                    }
                )
            } else {
                println("MainActivity: No saved token found")
            }
        }

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
            println("MainActivity: Received intent with URI = $uri")
            if (uri.scheme == "githubfollowy" && uri.host == "oauth") {
                println("MainActivity: Emitting OAuth callback to flow")
                // Emit callback to shared flow for the UI to handle
                io.mohammedalaamorsi.followy.shared.data.oauth.handleOAuthCallback(uri)
            }
        }
    }
}
