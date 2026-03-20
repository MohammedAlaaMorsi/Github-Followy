package io.mohammedalaamorsi.followy.shared.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock

/**
 * Web-specific token storage using browser's localStorage
 * Note: This is not as secure as native implementations but suitable for web
 */
actual class SecureTokenStorage actual constructor() {
    private val TOKEN_KEY = "github_token"
    private val TOKEN_TIMESTAMP_KEY = "github_token_timestamp"
    
    // JS interop for localStorage
    private fun getLocalStorage() = kotlinx.browser.window.localStorage
    
    actual suspend fun saveToken(token: String) {
        val encrypted = encryptToken(token)
        getLocalStorage().setItem(TOKEN_KEY, encrypted)
        getLocalStorage().setItem(TOKEN_TIMESTAMP_KEY, kotlinx.datetime.Clock.System.now().toEpochMilliseconds().toString())
    }
    
    actual suspend fun getToken(): String? {
        val encrypted = getLocalStorage().getItem(TOKEN_KEY) as? String
        return encrypted?.let { decryptToken(it) }
    }
    
    actual fun getTokenFlow(): Flow<String?> {
        return flow {
            emit(getToken())
        }
    }
    
    actual suspend fun clearToken() {
        getLocalStorage().removeItem(TOKEN_KEY)
        getLocalStorage().removeItem(TOKEN_TIMESTAMP_KEY)
    }
    
    actual suspend fun hasToken(): Boolean {
        return getLocalStorage().getItem(TOKEN_KEY) != null
    }
    
    actual fun getTokenAge(): Long {
        val timestampStr = getLocalStorage().getItem(TOKEN_TIMESTAMP_KEY) as? String
        val timestamp = timestampStr?.toLongOrNull() ?: 0L
        return if (timestamp > 0) {
            Clock.System.now().toEpochMilliseconds() - timestamp
        } else {
            0
        }
    }
    
    /**
     * Basic encryption for web (Base64 encoding with simple XOR)
     * Note: This is NOT secure - just obfuscation. For production, use proper crypto libraries
     */
    actual fun encryptToken(token: String): String {
        // Simple XOR with a key (NOT SECURE - just for demo)
        val key = "GitHubFollowy2024"
        val encrypted = token.mapIndexed { index, char ->
            (char.code xor key[index % key.length].code).toChar()
        }.joinToString("")
        return encoded(encrypted)
    }
    
    actual fun decryptToken(encryptedToken: String): String {
        val decoded = decoded(encryptedToken)
        val key = "GitHubFollowy2024"
        return decoded.mapIndexed { index, char ->
            (char.code xor key[index % key.length].code).toChar()
        }.joinToString("")
    }
    
    private fun encoded(str: String): String {
        // Simple Base64-like encoding
        return str.encodeToByteArray().joinToString(",") { it.toString() }
    }
    
    private fun decoded(str: String): String {
        return str.split(",").map { it.toInt().toByte() }.toByteArray().decodeToString()
    }
}
