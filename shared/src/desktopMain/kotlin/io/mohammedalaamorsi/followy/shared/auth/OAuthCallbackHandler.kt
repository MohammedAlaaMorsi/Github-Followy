package io.mohammedalaamorsi.followy.shared.auth

import androidx.compose.runtime.Composable
import io.mohammedalaamorsi.followy.shared.ui.auth.AuthViewModel

@Composable
actual fun HandleOAuthCallbacks(authViewModel: AuthViewModel) {
    // Desktop-specific OAuth callback handling (e.g., local server or deep link)
}
