package io.mohammedalaamorsi.followy.shared.domain.usecase

import io.mohammedalaamorsi.followy.shared.data.local.AuthTokenStorage
import io.mohammedalaamorsi.followy.shared.data.repository.GitHubRepository

class LogoutUseCase(
    private val repository: GitHubRepository
) {
    suspend operator fun invoke() {
        AuthTokenStorage.clearToken()
        repository.clearAuthToken()
    }
}
