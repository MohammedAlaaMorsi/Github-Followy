package template.shared.data.oauth

actual fun provideOAuthHandler(): GitHubOAuthHandler {
    return GitHubOAuthHandler()
}
