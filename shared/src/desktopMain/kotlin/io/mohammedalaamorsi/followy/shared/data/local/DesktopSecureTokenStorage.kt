package io.mohammedalaamorsi.followy.shared.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.mohammedalaamorsi.followy.shared.data.local.SecureTokenStorage
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Desktop-specific secure token storage with AES encryption
 */
class DesktopSecureTokenStorage(
    dataStore: DataStore<Preferences>
) : SecureTokenStorage(dataStore) {
    
    private val secretKey: SecretKey by lazy {
        // In production, use a proper key management system
        val keyString = "GithubFollowyKey2024SecureKey!" // 32 bytes
        SecretKeySpec(keyString.toByteArray().copyOf(32), "AES")
    }
    
    override fun encryptToken(token: String): String {
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val encrypted = cipher.doFinal(token.toByteArray())
            
            // Combine IV and encrypted data
            val combined = iv + encrypted
            Base64.getEncoder().encodeToString(combined)
        } catch (e: Exception) {
            Base64.getEncoder().encodeToString(token.toByteArray())
        }
    }
    
    override fun decryptToken(encryptedToken: String): String {
        return try {
            val combined = Base64.getDecoder().decode(encryptedToken)
            val iv = combined.copyOfRange(0, 12)
            val encrypted = combined.copyOfRange(12, combined.size)
            
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val gcmSpec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
            val decrypted = cipher.doFinal(encrypted)
            String(decrypted)
        } catch (e: Exception) {
            String(Base64.getDecoder().decode(encryptedToken))
        }
    }
}
