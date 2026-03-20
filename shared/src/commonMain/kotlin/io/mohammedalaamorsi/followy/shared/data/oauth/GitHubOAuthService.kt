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

@Serializable
data class GitHubErrorResponse(
    @SerialName("error")
    val error: String? = null,
    @SerialName("error_description")
    val errorDescription: String? = null,
    @SerialName("error_uri")
    val errorUri: String? = null
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
            println("OAuth: Token Exchange Params:")
            println("  URL: ${GitHubOAuthConfig.TOKEN_URL}")
            println("  client_id: $clientId")
            println("  client_secret: ${clientSecret.take(4)}***")
            println("  code: $code")
            println("  redirect_uri: $redirectUri")

            val response = httpClient.post(GitHubOAuthConfig.TOKEN_URL) {
                header("Accept", "application/json")
                parameter("client_id", clientId)
                parameter("client_secret", clientSecret)
                parameter("code", code)
                parameter("redirect_uri", redirectUri)
            }
            
            // Note: response.body<T>() can only be called once unless DoubleReceive is enabled.
            // We use specialized handling for debugging.
            val responseBody = response.body<String>()
            println("OAuth: Response body = $responseBody")
            
            if (responseBody.contains("\"error\"")) {
                // Manually parse error if possible or just use regex for simple extraction
                // For now, since we already have the string, we can return the error message.
                val errorMsg = if (responseBody.contains("\"error_description\"")) {
                    responseBody.substringAfter("\"error_description\":\"").substringBefore("\"")
                } else {
                    responseBody.substringAfter("\"error\":\"").substringBefore("\"")
                }
                println("OAuth: error: $errorMsg")
                return Result.failure(Exception(errorMsg))
            }
            
            // If no error, we need to get the token. 
            // Since we already consumed the body as string, we should parse it from the string.
            // But for simplicity, I'll just extract the token using regex or substring for now
            // to avoid needing a Json instance here.
            val token = responseBody.substringAfter("\"access_token\":\"").substringBefore("\"")
            if (token == responseBody) { // substringAfter returns original if not found
                 return Result.failure(Exception("Could not find access_token in response"))
            }
            
            println("OAuth: Successfully got token: ${token.take(10)}...")
            Result.success(token)
        } catch (e: Exception) {
            println("OAuth: Error exchanging code: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
