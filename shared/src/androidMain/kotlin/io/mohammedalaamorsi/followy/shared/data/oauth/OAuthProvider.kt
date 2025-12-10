package io.mohammedalaamorsi.followy.shared.data.oauth

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

private var androidContext: Context? = null
private var oauthHandlerInstance: GitHubOAuthHandler? = null

// Shared flow for OAuth callbacks that persists across Activity recreations
private val _oauthCallbackFlow = MutableSharedFlow<Uri>(replay = 0, extraBufferCapacity = 1)
val oauthCallbackFlow: SharedFlow<Uri> = _oauthCallbackFlow.asSharedFlow()

/**
 * Initialize OAuth with Android context
 * Call this from your Application or Activity
 */
fun initOAuth(context: Context) {
    androidContext = context.applicationContext
    oauthHandlerInstance = GitHubOAuthHandler(androidContext!!)
}

actual fun provideOAuthHandler(): GitHubOAuthHandler? {
    return oauthHandlerInstance
}

/**
 * Get the current OAuth handler instance for callback handling
 */
fun getOAuthHandler(): GitHubOAuthHandler? {
    return oauthHandlerInstance
}

/**
 * Handle OAuth callback from MainActivity
 * This emits the callback URI to a shared flow that the UI can observe
 */
fun handleOAuthCallback(uri: Uri) {
    _oauthCallbackFlow.tryEmit(uri)
}
