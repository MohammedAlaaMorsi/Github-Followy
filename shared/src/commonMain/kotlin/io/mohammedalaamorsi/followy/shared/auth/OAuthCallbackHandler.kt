package io.mohammedalaamorsi.followy.shared.auth

import androidx.compose.runtime.Composable
import io.mohammedalaamorsi.followy.shared.ui.auth.AuthViewModel

/**
 * Handle platform-specific OAuth callbacks (e.g., deep links or browser redirects)
 */
@Composable
expect fun HandleOAuthCallbacks(authViewModel: AuthViewModel)
