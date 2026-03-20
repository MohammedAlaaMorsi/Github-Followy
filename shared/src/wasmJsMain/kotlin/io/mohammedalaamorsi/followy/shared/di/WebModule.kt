package io.mohammedalaamorsi.followy.shared.di

import io.mohammedalaamorsi.followy.shared.data.local.DatabaseDriverFactory
import io.mohammedalaamorsi.followy.shared.data.local.WebDatabaseDriverFactory
import io.mohammedalaamorsi.followy.shared.data.local.SecureTokenStorage
import io.mohammedalaamorsi.followy.shared.data.oauth.GitHubOAuthHandler
import io.mohammedalaamorsi.followy.shared.data.oauth.GitHubAuthConfigProvider
import io.mohammedalaamorsi.followy.shared.data.oauth.BrowserWindowAuthConfigProvider
import org.koin.dsl.module

/**
 * Koin module for web-specific dependencies.
 */
val webModule = module {
    single<DatabaseDriverFactory> { WebDatabaseDriverFactory() }
    single { SecureTokenStorage() }
    single<GitHubAuthConfigProvider> { BrowserWindowAuthConfigProvider() }
    single { GitHubOAuthHandler() }
}
