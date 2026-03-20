package io.mohammedalaamorsi.followy.shared.domain.usecase

import io.mohammedalaamorsi.followy.shared.data.models.GitHubUser
import io.mohammedalaamorsi.followy.shared.data.repository.GitHubRepository

data class DashboardData(
    val followersNotFollowedBack: List<GitHubUser>,
    val followingNotFollowingBack: List<GitHubUser>,
    val allFollowing: List<GitHubUser>
)

/**
 * Use case to fetch and analyze follow relationships for the dashboard.
 */
class GetDashboardDataUseCase(private val repository: GitHubRepository) {
    suspend operator fun invoke(username: String): Result<DashboardData> {
        return try {
            val followersResult = repository.analyzeFollowRelationships(username)
            val allFollowingResult = repository.getAllFollowing(username)
            
            if (followersResult.isFailure) return Result.failure(followersResult.exceptionOrNull()!!)
            if (allFollowingResult.isFailure) return Result.failure(allFollowingResult.exceptionOrNull()!!)
            
            val (followersNotFollowedBack, followingNotFollowingBack) = followersResult.getOrNull()!!
            val allFollowing = allFollowingResult.getOrNull()!!
            
            Result.success(
                DashboardData(
                    followersNotFollowedBack = followersNotFollowedBack,
                    followingNotFollowingBack = followingNotFollowingBack,
                    allFollowing = allFollowing
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
