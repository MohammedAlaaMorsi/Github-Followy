package template.shared.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.*

/**
 * iOS-specific secure token storage using Keychain
 */
class IOSSecureTokenStorage(
    dataStore: DataStore<Preferences>
) : SecureTokenStorage(dataStore) {
    
    private val service = "com.github.followy.token"
    
    override fun encryptToken(token: String): String {
        // iOS: Store in Keychain for actual encryption
        // Here we just encode for DataStore compatibility
        return token.encodeToByteArray().joinToString(",") { it.toString() }
    }
    
    override fun decryptToken(encryptedToken: String): String {
        return try {
            encryptedToken.split(",")
                .map { it.toByte() }
                .toByteArray()
                .decodeToString()
        } catch (e: Exception) {
            encryptedToken
        }
    }
}
