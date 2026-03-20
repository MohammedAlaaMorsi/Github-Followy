package io.mohammedalaamorsi.followy.shared.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import io.mohammedalaamorsi.followy.shared.data.api.GitHubApiClient
import io.mohammedalaamorsi.followy.shared.data.oauth.GitHubOAuthService
import io.mohammedalaamorsi.followy.shared.data.repository.GitHubRepository
import io.mohammedalaamorsi.followy.shared.domain.usecase.*
import io.mohammedalaamorsi.followy.shared.ui.auth.AuthViewModel
import io.mohammedalaamorsi.followy.shared.ui.dashboard.DashboardViewModel
import io.mohammedalaamorsi.followy.shared.ui.profile.ProfileViewModel

val appModule = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        val sanitized = message
                            .replace(Regex("Authorization: Bearer [^\\s]+"), "Authorization: Bearer ***")
                            .replace(Regex("token [a-zA-Z0-9_]+"), "token ***")
                        println("HTTP: $sanitized")
                    }
                }
                level = LogLevel.INFO
            }
        }
    }
    
    singleOf(::GitHubApiClient)
    singleOf(::GitHubOAuthService)
    singleOf(::GitHubRepository)
    
    // Use Cases
    single { ValidateAndAuthenticateUseCase(get()) }
    single { GetDashboardDataUseCase(get()) }
    single { FollowUserUseCase(get()) }
    single { UnfollowUserUseCase(get()) }
    single { ExchangeOAuthCodeUseCase(get(), get()) }
    single { LogoutUseCase(get()) }
    
    // ViewModels (MVI)
    factory { AuthViewModel(get(), get(), get()) }
    factory { DashboardViewModel(get(), get(), get(), get()) }
    factory { ProfileViewModel(get(), get(), get()) }
}
