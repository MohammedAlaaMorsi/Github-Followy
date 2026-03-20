package io.mohammedalaamorsi.followy.shared.di

import io.mohammedalaamorsi.followy.shared.data.local.DatabaseDriverFactory
import io.mohammedalaamorsi.followy.shared.data.local.DesktopDatabaseDriverFactory
import io.mohammedalaamorsi.followy.shared.data.preferences.createDesktopDataStore
import io.mohammedalaamorsi.followy.shared.data.local.SecureTokenStorage
import io.mohammedalaamorsi.followy.shared.data.oauth.GitHubAuthConfigProvider
import io.mohammedalaamorsi.followy.shared.data.oauth.GitHubAuthConfig
import io.mohammedalaamorsi.followy.shared.data.oauth.GitHubOAuthConfig
import org.koin.dsl.module

/**
 * Koin module for desktop-specific dependencies.
 */
val desktopModule = module {
    single { createDesktopDataStore() }
    single<DatabaseDriverFactory> { DesktopDatabaseDriverFactory() }
    single { SecureTokenStorage().apply { setDataStore(get()) } }
    single<GitHubAuthConfigProvider> { 
        GitHubAuthConfig(
            clientId = GitHubOAuthConfig.clientId,
            clientSecret = GitHubOAuthConfig.clientSecret,
            redirectUri = GitHubOAuthConfig.redirectUri
        )
    }
}
