package io.mohammedalaamorsi.followy.shared.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Web-specific token storage using browser's localStorage
 * Note: This is not as secure as native implementations but suitable for web
 */
actual class SecureTokenStorage actual constructor(
    private val dataStore: DataStore<Preferences>?
) {
    private val TOKEN_KEY = "github_token"
    private val TOKEN_TIMESTAMP_KEY = "github_token_timestamp"
    
    // In-memory storage for web (localStorage would be better but requires JS interop)
    private var cachedToken: String? = null
    private var tokenTimestamp: Long = 0
    
    actual suspend fun saveToken(token: String) {
        val encrypted = encryptToken(token)
        cachedToken = encrypted
        tokenTimestamp = System.currentTimeMillis()
    }
    
    actual suspend fun getToken(): String? {
        return cachedToken?.let { decryptToken(it) }
    }
    
    actual fun getTokenFlow(): Flow<String?> {
        return flow {
            emit(getToken())
        }
    }
    
    actual suspend fun clearToken() {
        cachedToken = null
        tokenTimestamp = 0
    }
    
    actual suspend fun hasToken(): Boolean {
        return cachedToken != null
    }
    
    actual fun getTokenAge(): Long {
        return if (tokenTimestamp > 0) {
            System.currentTimeMillis() - tokenTimestamp
        } else {
            0
        }
    }
    
    /**
     * Basic encryption for web (Base64 encoding with simple XOR)
     * Note: This is NOT secure - just obfuscation. For production, use proper crypto libraries
     */
    protected actual fun encryptToken(token: String): String {
        // Simple XOR with a key (NOT SECURE - just for demo)
        val key = "GitHubFollowy2024"
        val encrypted = token.mapIndexed { index, char ->
            (char.code xor key[index % key.length].code).toChar()
        }.joinToString("")
        return encoded(encrypted)
    }
    
    protected actual fun decryptToken(encryptedToken: String): String {
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
