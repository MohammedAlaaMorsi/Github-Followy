package io.mohammedalaamorsi.followy.shared.di

import io.mohammedalaamorsi.followy.shared.data.local.DatabaseDriverFactory
import io.mohammedalaamorsi.followy.shared.data.local.DesktopDatabaseDriverFactory
import io.mohammedalaamorsi.followy.shared.data.preferences.createDesktopDataStore
import io.mohammedalaamorsi.followy.shared.data.local.SecureTokenStorage
import io.mohammedalaamorsi.followy.shared.data.oauth.GitHubAuthConfigProvider
import io.mohammedalaamorsi.followy.shared.data.oauth.GitHubAuthConfig
import io.mohammedalaamorsi.followy.shared.data.oauth.GitHubOAuthConfig
import io.mohammedalaamorsi.followy.shared.data.oauth.GitHubOAuthHandler
import io.mohammedalaamorsi.followy.shared.Platform
import io.mohammedalaamorsi.followy.shared.JVMPlatform
import org.koin.dsl.module

/**
 * Koin module for desktop-specific dependencies.
 */
val desktopModule = module {
    single { createDesktopDataStore() }
    factory { GitHubOAuthHandler() }

    single<DatabaseDriverFactory> { DesktopDatabaseDriverFactory() }
    single { SecureTokenStorage().apply { setDataStore(get()) } }
    single<GitHubAuthConfigProvider> { 
        GitHubAuthConfig(
            clientId = GitHubOAuthConfig.clientId,
            clientSecret = GitHubOAuthConfig.clientSecret,
            redirectUri = GitHubOAuthConfig.redirectUri
        )
    }
    
    // Register Platform implementation for Desktop
    single<Platform> { JVMPlatform() }
}
