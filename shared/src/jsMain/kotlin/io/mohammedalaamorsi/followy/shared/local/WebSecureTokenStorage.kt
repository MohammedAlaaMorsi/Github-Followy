package io.mohammedalaamorsi.followy.shared.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.mohammedalaamorsi.followy.shared.data.local.SecureTokenStorage

/**
 * Web-specific secure token storage
 * Uses browser's built-in security for DataStore
 */
class WebSecureTokenStorage(
    dataStore: DataStore<Preferences>
) : SecureTokenStorage(dataStore) {
    
    override fun encryptToken(token: String): String {
        // Web: Browser handles encryption via HTTPS
        // We'll obfuscate at minimum
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
