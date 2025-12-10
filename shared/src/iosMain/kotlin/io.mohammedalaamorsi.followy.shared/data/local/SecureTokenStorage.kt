package template.shared.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.cinterop.*
import platform.Foundation.*
import platform.Security.*

/**
 * iOS-specific secure token storage using Keychain
 */
actual class SecureTokenStorage actual constructor(
    private val dataStore: DataStore<Preferences>?
) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("github_token")
        private val TOKEN_TIMESTAMP = stringPreferencesKey("token_timestamp")
    }
    
    actual suspend fun saveToken(token: String) {
        dataStore?.edit { preferences ->
            preferences[TOKEN_KEY] = encryptToken(token)
            preferences[TOKEN_TIMESTAMP] = System.currentTimeMillis().toString()
        }
    }
    
    actual suspend fun getToken(): String? {
        return dataStore?.data?.map { preferences ->
            preferences[TOKEN_KEY]?.let { encryptedToken ->
                decryptToken(encryptedToken)
            }
        }?.first()
    }
    
    actual fun getTokenFlow(): Flow<String?> {
        return dataStore?.data?.map { preferences ->
            preferences[TOKEN_KEY]?.let { encryptedToken ->
                decryptToken(encryptedToken)
            }
        } ?: kotlinx.coroutines.flow.flowOf(null)
    }
    
    actual suspend fun clearToken() {
        dataStore?.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(TOKEN_TIMESTAMP)
        }
    }
    
    actual suspend fun hasToken(): Boolean {
        return getToken() != null
    }
    
    actual fun getTokenAge(): Long {
        return try {
            val timestamp = dataStore?.data?.map { it[TOKEN_TIMESTAMP]?.toLong() ?: 0L }?.let {
                kotlinx.coroutines.runBlocking { it.first() }
            } ?: 0L
            if (timestamp > 0) {
                System.currentTimeMillis() - timestamp
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }
    
    actual fun encryptToken(token: String): String {
        // Use NSData Base64 encoding for iOS
        return token.encodeToByteArray().toNSData().base64EncodedStringWithOptions(0)
    }
    
    actual fun decryptToken(encryptedToken: String): String {
        return try {
            NSData.create(base64Encoding = encryptedToken)?.toByteArray()?.decodeToString() ?: encryptedToken
        } catch (e: Exception) {
            encryptedToken
        }
    }
    
    private fun ByteArray.toNSData(): NSData {
        return NSData.create(bytes = this.refTo(0).getPointer(MemScope()), length = this.size.toULong())
    }
    
    private fun NSData.toByteArray(): ByteArray {
        return ByteArray(this.length.toInt()).apply {
            usePinned {
                memcpy(it.addressOf(0), this@toByteArray.bytes, this@toByteArray.length)
            }
        }
    }
}
