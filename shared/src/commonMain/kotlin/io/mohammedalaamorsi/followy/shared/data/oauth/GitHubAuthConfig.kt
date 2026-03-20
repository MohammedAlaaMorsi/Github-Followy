package io.mohammedalaamorsi.followy.shared.data.oauth

/**
 * Basic implementation of [GitHubAuthConfigProvider].
 */
data class GitHubAuthConfig(
    override val clientId: String,
    override val clientSecret: String,
    override val redirectUri: String
) : GitHubAuthConfigProvider
