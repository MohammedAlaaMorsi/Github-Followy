package io.mohammedalaamorsi.followy.shared.ui.auth

import androidx.compose.runtime.Composable

/**
 * Platform-specific OAuth callback handler
 * This composable observes platform-specific OAuth callback events
 */
@Composable
expect fun HandleOAuthCallbacks(authViewModel: AuthViewModel)
