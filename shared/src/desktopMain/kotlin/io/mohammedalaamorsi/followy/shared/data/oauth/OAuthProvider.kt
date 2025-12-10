package io.mohammedalaamorsi.followy.shared.data.oauth

import io.mohammedalaamorsi.followy.shared.data.oauth.GitHubOAuthHandler

actual fun provideOAuthHandler(): GitHubOAuthHandler {
    return GitHubOAuthHandler()
}
