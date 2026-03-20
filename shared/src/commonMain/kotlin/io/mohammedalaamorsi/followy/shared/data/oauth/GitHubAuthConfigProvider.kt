package io.mohammedalaamorsi.followy.shared.data.oauth

/**
 * Interface to provide OAuth configuration without hardcoding secrets.
 */
interface GitHubAuthConfigProvider {
    val clientId: String
    val clientSecret: String
    val redirectUri: String
}
