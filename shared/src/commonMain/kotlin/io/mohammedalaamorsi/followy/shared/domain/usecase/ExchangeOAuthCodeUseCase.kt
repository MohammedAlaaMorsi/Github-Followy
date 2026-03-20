package io.mohammedalaamorsi.followy.shared.domain.usecase

import io.mohammedalaamorsi.followy.shared.data.models.GitHubUser
import io.mohammedalaamorsi.followy.shared.data.oauth.GitHubOAuthService

class ExchangeOAuthCodeUseCase(
    private val oauthService: GitHubOAuthService,
    private val validateAndAuthenticateUseCase: ValidateAndAuthenticateUseCase
) {
    suspend operator fun invoke(
        code: String,
        clientId: String,
        clientSecret: String,
        redirectUri: String
    ): Result<Pair<GitHubUser, String>> {
        return oauthService.exchangeCodeForToken(code, clientId, clientSecret, redirectUri).fold(
            onSuccess = { token ->
                validateAndAuthenticateUseCase(token)
            },
            onFailure = { error ->
                Result.failure(error)
            }
        )
    }
}
