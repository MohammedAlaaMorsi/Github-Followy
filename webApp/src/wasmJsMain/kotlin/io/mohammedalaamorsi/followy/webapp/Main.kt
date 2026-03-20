package io.mohammedalaamorsi.followy.webapp

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import io.mohammedalaamorsi.followy.shared.App
import io.mohammedalaamorsi.followy.shared.data.local.AuthTokenStorage
import io.mohammedalaamorsi.followy.shared.data.local.SecureTokenStorage
import io.mohammedalaamorsi.followy.shared.di.appModule
import io.mohammedalaamorsi.followy.shared.di.webModule
import kotlinx.browser.document
import org.koin.core.context.startKoin

import io.mohammedalaamorsi.followy.shared.data.oauth.GitHubOAuthConfig
import kotlinx.browser.window
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val koin = startKoin {
        modules(appModule, webModule)
    }.koin

    // Use a proxy for tokens to bypass CORS issues on Web
    GitHubOAuthConfig.TOKEN_URL = "/github-oauth/login/oauth/access_token"

    // Initialize AuthTokenStorage with web-specific secure storage
    val secureStorage: SecureTokenStorage = koin.get()
    AuthTokenStorage.initialize(secureStorage)

    ComposeViewport(document.body!!) {
        // Initialize Coil for web image loading within @Composable context
        setSingletonImageLoaderFactory { context ->
            ImageLoader.Builder(context)
                .components {
                    add(KtorNetworkFetcherFactory())
                }
                .build()
        }
        App()
    }
}
