package io.mohammedalaamorsi.followy.shared.usecase

import io.mohammedalaamorsi.followy.shared.data.local.AuthTokenStorage
import io.mohammedalaamorsi.followy.shared.data.models.GitHubUser
import io.mohammedalaamorsi.followy.shared.data.repository.GitHubRepository
import io.mohammedalaamorsi.followy.shared.utils.SecurityUtils

class ValidateAndAuthenticateUseCase(
    private val repository: GitHubRepository
) {
    suspend operator fun invoke(token: String): Result<Pair<GitHubUser, String>> {
        // Validate token format first
        if (!SecurityUtils.isValidGitHubToken(token)) {
            return Result.failure(Exception("Invalid token format"))
        }
        
        // Set token in repository
        repository.setAuthToken(token)
        
        // Try to authenticate with GitHub API
        return repository.getAuthenticatedUser().fold(
            onSuccess = { user ->
                // Save token on successful authentication
                AuthTokenStorage.saveToken(token)
                Result.success(Pair(user, token))
            },
            onFailure = { error ->
                // Clear invalid token
                AuthTokenStorage.clearToken()
                repository.clearAuthToken()
                Result.failure(error)
            }
        )
    }
    
    suspend fun checkSavedToken(): Result<Pair<GitHubUser, String>>? {
        val savedToken = AuthTokenStorage.getToken()
        return if (savedToken != null) {
            invoke(savedToken)
        } else {
            null
        }
    }
}
