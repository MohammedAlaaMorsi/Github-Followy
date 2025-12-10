package io.mohammedalaamorsi.followy.shared.data.oauth

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubTokenResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("token_type")
    val tokenType: String,
    @SerialName("scope")
    val scope: String
)

/**
 * Service to exchange OAuth code for access token
 */
class GitHubOAuthService(private val httpClient: HttpClient) {
    
    suspend fun exchangeCodeForToken(
        code: String,
        clientId: String,
        clientSecret: String,
        redirectUri: String
    ): Result<String> {
        return try {
            println("OAuth: Exchanging code for token...")
            println("OAuth: Code = $code")
            println("OAuth: ClientID = $clientId")
            println("OAuth: RedirectURI = $redirectUri")
            
            val response = httpClient.post(GitHubOAuthConfig.TOKEN_URL) {
                header("Accept", "application/json")
                parameter("client_id", clientId)
                parameter("client_secret", clientSecret)
                parameter("code", code)
                parameter("redirect_uri", redirectUri)
            }
            
            println("OAuth: Response status = ${response.status}")
            val tokenResponse: GitHubTokenResponse = response.body()
            println("OAuth: Successfully got token: ${tokenResponse.accessToken.take(10)}...")
            Result.success(tokenResponse.accessToken)
        } catch (e: Exception) {
            println("OAuth: Error exchanging code: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
