package io.mohammedalaamorsi.followy.shared.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.JsonArray
import io.mohammedalaamorsi.followy.shared.data.models.GitHubUser
import io.mohammedalaamorsi.followy.shared.utils.SecurityUtils

class GitHubApiClient(private val httpClient: HttpClient) {
    
    private var authToken: String? = null
    
    fun setAuthToken(token: String) {
        // Validate token before setting
        if (!SecurityUtils.isValidGitHubToken(token)) {
            println("Warning: Invalid GitHub token format")
        }
        authToken = token
    }
    
    fun clearAuthToken() {
        authToken = null
    }
    
    private fun getAuthHeader(): String {
        return "Bearer ${authToken ?: ""}"
    }
    
    suspend fun getAuthenticatedUser(): Result<GitHubUser> {
        return try {
            val response = httpClient.get("https://api.github.com/user") {
                header("Authorization", getAuthHeader())
                header("Accept", "application/vnd.github+json")
            }
            Result.success(response.body())
        } catch (e: Exception) {
            val sanitizedMessage = SecurityUtils.sanitizeErrorMessage(
                e.message ?: "Unknown error", 
                authToken
            )
            Result.failure(Exception(sanitizedMessage))
        }
    }
    
    suspend fun getUser(username: String): Result<GitHubUser> {
        return try {
            val response = httpClient.get("https://api.github.com/users/$username") {
                header("Authorization", getAuthHeader())
                header("Accept", "application/vnd.github+json")
            }
            Result.success(response.body())
        } catch (e: Exception) {
            val sanitizedMessage = SecurityUtils.sanitizeErrorMessage(
                e.message ?: "Unknown error", 
                authToken
            )
            Result.failure(Exception(sanitizedMessage))
        }
    }
    
    suspend fun isFollowing(follower: String, following: String): Result<Boolean> {
        return try {
            val response = httpClient.get("https://api.github.com/users/$follower/following/$following") {
                header("Authorization", getAuthHeader())
                header("Accept", "application/vnd.github+json")
            }
            Result.success(response.status == HttpStatusCode.NoContent)
        } catch (e: Exception) {
            // 404 means not following
            Result.success(false)
        }
    }
    
    suspend fun getFollowers(username: String, page: Int = 1, perPage: Int = 100): Result<List<GitHubUser>> {
        return try {
            val response = httpClient.get("https://api.github.com/users/$username/followers") {
                header("Authorization", getAuthHeader())
                header("Accept", "application/vnd.github+json")
                url {
                    parameters.append("page", page.toString())
                    parameters.append("per_page", perPage.toString())
                }
            }
            Result.success(response.body())
        } catch (e: Exception) {
            val sanitizedMessage = SecurityUtils.sanitizeErrorMessage(
                e.message ?: "Unknown error", 
                authToken
            )
            Result.failure(Exception(sanitizedMessage))
        }
    }
    
    suspend fun getFollowing(username: String, page: Int = 1, perPage: Int = 100): Result<List<GitHubUser>> {
        return try {
            val response = httpClient.get("https://api.github.com/users/$username/following") {
                header("Authorization", getAuthHeader())
                header("Accept", "application/vnd.github+json")
                url {
                    parameters.append("page", page.toString())
                    parameters.append("per_page", perPage.toString())
                }
            }
            Result.success(response.body())
        } catch (e: Exception) {
            val sanitizedMessage = SecurityUtils.sanitizeErrorMessage(
                e.message ?: "Unknown error", 
                authToken
            )
            Result.failure(Exception(sanitizedMessage))
        }
    }
    
    suspend fun followUser(username: String): Result<Boolean> {
        return try {
            val response = httpClient.put("https://api.github.com/user/following/$username") {
                header("Authorization", getAuthHeader())
                header("Accept", "application/vnd.github+json")
            }
            
            when (response.status) {
                HttpStatusCode.NoContent -> Result.success(true)
                HttpStatusCode.Forbidden -> {
                    // User has disabled following on their profile
                    Result.failure(Exception("This user has disabled following on their profile"))
                }
                else -> Result.failure(Exception("Failed to follow user: ${response.status}"))
            }
        } catch (e: Exception) {
            val sanitizedMessage = SecurityUtils.sanitizeErrorMessage(
                e.message ?: "Unknown error", 
                authToken
            )
            Result.failure(Exception(sanitizedMessage))
        }
    }
    
    suspend fun hasPrivateActivity(username: String): Boolean {
        return try {
            // Users with private profiles return empty events even if they have repos
            val events = httpClient.get("https://api.github.com/users/$username/events") {
                header("Accept", "application/vnd.github+json")
            }.body<JsonArray>()
            
            val hasPrivate = events.isEmpty()
            println("GitHubApiClient: User $username has empty events: $hasPrivate (${events.size} events)")
            
            // If they have 0 events but we can still see their profile, likely private
            hasPrivate
        } catch (e: Exception) {
            println("GitHubApiClient: Error checking $username: ${e.message}")
            false
        }
    }
    
    suspend fun unfollowUser(username: String): Result<Boolean> {
        return try {
            val response = httpClient.delete("https://api.github.com/user/following/$username") {
                header("Authorization", getAuthHeader())
                header("Accept", "application/vnd.github+json")
            }
            Result.success(response.status == HttpStatusCode.NoContent)
        } catch (e: Exception) {
            val sanitizedMessage = SecurityUtils.sanitizeErrorMessage(
                e.message ?: "Unknown error", 
                authToken
            )
            Result.failure(Exception(sanitizedMessage))
        }
    }
    
    suspend fun getAllFollowers(username: String): Result<List<GitHubUser>> {
        val allFollowers = mutableListOf<GitHubUser>()
        var page = 1
        
        while (true) {
            val result = getFollowers(username, page)
            result.fold(
                onSuccess = { followers ->
                    if (followers.isEmpty()) return Result.success(allFollowers)
                    allFollowers.addAll(followers)
                    page++
                },
                onFailure = { return Result.failure(it) }
            )
        }
    }
    
    suspend fun getAllFollowing(username: String): Result<List<GitHubUser>> {
        val allFollowing = mutableListOf<GitHubUser>()
        var page = 1
        
        while (true) {
            val result = getFollowing(username, page)
            result.fold(
                onSuccess = { following ->
                    if (following.isEmpty()) return Result.success(allFollowing)
                    allFollowing.addAll(following)
                    page++
                },
                onFailure = { return Result.failure(it) }
            )
        }
    }
}
