package io.mohammedalaamorsi.followy.shared.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow

/**
 * Secure token storage using DataStore
 * Platform-specific encryption is handled by implementations
 */
expect class SecureTokenStorage(dataStore: DataStore<Preferences>?) {
    suspend fun saveToken(token: String)
    suspend fun getToken(): String?
    fun getTokenFlow(): Flow<String?>
    suspend fun clearToken()
    suspend fun hasToken(): Boolean
    fun getTokenAge(): Long
    
    fun encryptToken(token: String): String
    fun decryptToken(encryptedToken: String): String
}
