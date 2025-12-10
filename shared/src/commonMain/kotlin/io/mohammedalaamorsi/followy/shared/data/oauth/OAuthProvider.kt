package io.mohammedalaamorsi.followy.shared.data.oauth

/**
 * Provides platform-specific OAuth handler
 */
expect fun provideOAuthHandler(): GitHubOAuthHandler?
