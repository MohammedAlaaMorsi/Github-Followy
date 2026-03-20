package io.mohammedalaamorsi.followy.shared.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Desktop-specific secure token storage using AES encryption
 */
actual class SecureTokenStorage actual constructor() {
    private var dataStore: DataStore<Preferences>? = null
    
    fun setDataStore(dataStore: DataStore<Preferences>) {
        this.dataStore = dataStore
    }
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("github_token")
        private val TOKEN_TIMESTAMP = stringPreferencesKey("token_timestamp")
        private const val KEY_STRING = "GitHubFollowyDesktop2024Secure" // In production, use proper key management
    }
    
    private val cipher by lazy {
        Cipher.getInstance("AES/GCM/NoPadding")
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
        } ?: flowOf(null)
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
                runBlocking { it.first() }
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
        return try {
            val key = KEY_STRING.toByteArray().copyOf(32) // Use 256-bit key
            val spec = SecretKeySpec(key, "AES")
            
            cipher.init(Cipher.ENCRYPT_MODE, spec)
            val iv = cipher.iv
            val encrypted = cipher.doFinal(token.toByteArray())
            
            // Combine IV and encrypted data
            val combined = iv + encrypted
            Base64.getEncoder().encodeToString(combined)
        } catch (e: Exception) {
            // Fallback to base64 if encryption fails
            Base64.getEncoder().encodeToString(token.toByteArray())
        }
    }
    
    actual fun decryptToken(encryptedToken: String): String {
        return try {
            val combined = Base64.getDecoder().decode(encryptedToken)
            val iv = combined.copyOfRange(0, 12) // GCM standard IV size
            val encrypted = combined.copyOfRange(12, combined.size)
            
            val key = KEY_STRING.toByteArray().copyOf(32)
            val spec = SecretKeySpec(key, "AES")
            val gcmSpec = GCMParameterSpec(128, iv)
            
            cipher.init(Cipher.DECRYPT_MODE, spec, gcmSpec)
            val decrypted = cipher.doFinal(encrypted)
            String(decrypted)
        } catch (e: Exception) {
            // Fallback to base64 decode if decryption fails
            try {
                String(Base64.getDecoder().decode(encryptedToken))
            } catch (e2: Exception) {
                encryptedToken
            }
        }
    }
}
