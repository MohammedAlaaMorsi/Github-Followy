package io.mohammedalaamorsi.followy.shared.domain.usecase

import io.mohammedalaamorsi.followy.shared.data.repository.GitHubRepository

/**
 * Use case to follow a user on GitHub.
 */
class FollowUserUseCase(private val repository: GitHubRepository) {
    suspend operator fun invoke(username: String): Result<Boolean> {
        return repository.followUser(username)
    }
}

/**
 * Use case to unfollow a user on GitHub.
 */
class UnfollowUserUseCase(private val repository: GitHubRepository) {
    suspend operator fun invoke(username: String): Result<Boolean> {
        return repository.unfollowUser(username)
    }
}
