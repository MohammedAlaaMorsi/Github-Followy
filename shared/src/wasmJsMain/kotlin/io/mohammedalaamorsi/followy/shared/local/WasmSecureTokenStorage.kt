package io.mohammedalaamorsi.followy.shared.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.mohammedalaamorsi.followy.shared.data.local.SecureTokenStorage

/**
 * Wasm-specific secure token storage
 */
class WasmSecureTokenStorage(
    dataStore: DataStore<Preferences>
) : SecureTokenStorage(dataStore) {
    
    override fun encryptToken(token: String): String {
        // Wasm: Similar to JS, rely on browser security
        return token.reversed().chunked(2).joinToString("-")
    }
    
    override fun decryptToken(encryptedToken: String): String {
        return try {
            encryptedToken.split("-").joinToString("").reversed()
        } catch (e: Exception) {
            encryptedToken
        }
    }
}
