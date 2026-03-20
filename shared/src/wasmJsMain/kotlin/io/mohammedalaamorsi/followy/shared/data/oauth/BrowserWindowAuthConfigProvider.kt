package io.mohammedalaamorsi.followy.shared.data.oauth

import kotlinx.browser.window

/**
 * Implementation of [GitHubAuthConfigProvider] that retrieves secrets from the browser's window object.
 * This ensures that secrets are not hardcoded in the Kotlin source/Wasm binary.
 */
class BrowserWindowAuthConfigProvider : GitHubAuthConfigProvider {
    override val clientId: String
        get() = getJsValue("clientId")?.let { it.toString() } ?: "".also {
            println("BrowserWindowAuthConfigProvider: clientId is missing!")
        }
        
    override val clientSecret: String
        get() = getJsValue("clientSecret")?.let { it.toString() } ?: "".also {
            println("BrowserWindowAuthConfigProvider: clientSecret is missing!")
        }
        
    override val redirectUri: String
        get() = getJsValue("redirectUri")?.let { it.toString() } ?: window.location.origin
}

private fun getJsValue(name: String): kotlin.js.JsAny? = js("window[name]")
