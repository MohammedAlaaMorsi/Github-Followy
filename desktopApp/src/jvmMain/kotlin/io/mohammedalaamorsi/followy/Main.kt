package io.mohammedalaamorsi.followy

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.mohammedalaamorsi.followy.shared.App
import io.mohammedalaamorsi.followy.shared.data.local.AuthTokenStorage
import io.mohammedalaamorsi.followy.shared.data.local.SecureTokenStorage
import io.mohammedalaamorsi.followy.shared.data.oauth.GitHubOAuthConfig
import io.mohammedalaamorsi.followy.shared.di.appModule
import io.mohammedalaamorsi.followy.shared.di.desktopModule
import org.koin.core.context.startKoin
import java.io.File
import java.util.Properties

fun main() = application {
    // Load OAuth credentials
    val oauthProperties = Properties()
    var propertiesFile = File("oauth.properties")
    if (!propertiesFile.exists()) {
        propertiesFile = File("../oauth.properties")
    }
    
    if (propertiesFile.exists()) {
        propertiesFile.inputStream().use { oauthProperties.load(it) }
        GitHubOAuthConfig.clientId = oauthProperties.getProperty("GITHUB_CLIENT_ID", "")
        GitHubOAuthConfig.clientSecret = oauthProperties.getProperty("GITHUB_CLIENT_SECRET", "")
        GitHubOAuthConfig.redirectUri = "http://127.0.0.1:8080/oauth/callback"
    }
    
    val koin = startKoin {
        modules(appModule, desktopModule)
    }.koin
    
    // Initialize AuthTokenStorage with platform-specific secure storage
    val secureStorage: SecureTokenStorage = koin.get()
    AuthTokenStorage.initialize(secureStorage)
    
    val windowState = rememberWindowState(width = 1000.dp, height = 800.dp)
    
    Window(
        onCloseRequest = ::exitApplication,
        title = "GitHub Followy",
        state = windowState,
        icon = painterResource("icon.png")
    ) {
        App()
    }
}
