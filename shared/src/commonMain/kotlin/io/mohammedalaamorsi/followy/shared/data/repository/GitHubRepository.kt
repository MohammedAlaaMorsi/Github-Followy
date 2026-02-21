package io.mohammedalaamorsi.followy.shared.data.repository

import io.mohammedalaamorsi.followy.shared.data.api.GitHubApiClient
import io.mohammedalaamorsi.followy.shared.data.models.GitHubUser

class GitHubRepository(private val apiClient: GitHubApiClient) {
    
    fun setAuthToken(token: String) {
        apiClient.setAuthToken(token)
    }
    
    fun clearAuthToken() {
        apiClient.clearAuthToken()
    }
    
    suspend fun getAuthenticatedUser(): Result<GitHubUser> {
        return apiClient.getAuthenticatedUser()
    }
    
    suspend fun getUser(username: String): Result<GitHubUser> {
        return apiClient.getUser(username)
    }
    
    suspend fun isFollowing(follower: String, following: String): Result<Boolean> {
        return apiClient.isFollowing(follower, following)
    }
    
    suspend fun analyzeFollowRelationships(username: String): Result<Pair<List<GitHubUser>, List<GitHubUser>>> {
        return try {
            val followersResult = apiClient.getAllFollowers(username)
            val followingResult = apiClient.getAllFollowing(username)
            
            if (followersResult.isFailure) return Result.failure(followersResult.exceptionOrNull()!!)
            if (followingResult.isFailure) return Result.failure(followingResult.exceptionOrNull()!!)
            
            val followers = followersResult.getOrNull() ?: emptyList()
            val following = followingResult.getOrNull() ?: emptyList()
            
            val followersSet = followers.map { it.login }.toSet()
            val followingSet = following.map { it.login }.toSet()
            
            // Users who follow you but you don't follow back
            val followersNotFollowedBack = followers.filter { it.login !in followingSet }
            
            // Users you follow but they don't follow back
            val followingNotFollowingBack = following.filter { it.login !in followersSet }
            
            Result.success(followersNotFollowedBack to followingNotFollowingBack)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun followUser(username: String): Result<Boolean> {
        return apiClient.followUser(username)
    }
    
    suspend fun unfollowUser(username: String): Result<Boolean> {
        return apiClient.unfollowUser(username)
    }
    
    suspend fun hasPrivateActivity(username: String): Boolean {
        return apiClient.hasPrivateActivity(username)
    }
    
    suspend fun getAllFollowing(username: String): Result<List<GitHubUser>> {
        return apiClient.getAllFollowing(username)
    }
}
