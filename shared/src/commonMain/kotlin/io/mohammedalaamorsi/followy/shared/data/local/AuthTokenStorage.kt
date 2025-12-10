package io.mohammedalaamorsi.followy.shared.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking

/**
 * Secure persistent storage for auth token using DataStore
 * Replaces in-memory storage with encrypted persistent storage
 */
object AuthTokenStorage {
    private var secureStorage: SecureTokenStorage? = null
    
    fun initialize(storage: SecureTokenStorage) {
        secureStorage = storage
    }
    
    suspend fun saveToken(token: String) {
        secureStorage?.saveToken(token)
    }
    
    suspend fun getToken(): String? {
        return secureStorage?.getToken()
    }
    
    fun getTokenFlow(): Flow<String?> {
        return secureStorage?.getTokenFlow() ?: flowOf(null)
    }
    
    suspend fun clearToken() {
        secureStorage?.clearToken()
    }
    
    suspend fun hasToken(): Boolean {
        return secureStorage?.hasToken() ?: false
    }
    
    // Blocking version for init checks
    fun getTokenBlocking(): String? {
        return runBlocking {
            secureStorage?.getToken()
        }
    }
}
