package io.mohammedalaamorsi.followy.shared.data.oauth

/**
 * OAuth configuration for GitHub
 */
object GitHubOAuthConfig {
    // GitHub OAuth endpoints
    const val AUTHORIZE_URL = "https://github.com/login/oauth/authorize"
    const val TOKEN_URL = "https://github.com/login/oauth/access_token"
    
    // Required scopes for the app
    const val SCOPES = "user,user:follow"
    
    // You need to register your app at: https://github.com/settings/developers
    // Store your credentials in oauth.properties file (not committed to git)
    // In production, these would be stored securely on a backend server
    var clientId: String = ""
    var clientSecret: String = ""
    var redirectUri: String = "githubfollowy://oauth/callback"
}
