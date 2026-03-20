package io.mohammedalaamorsi.followy.shared.utils

/**
 * Security utilities for handling sensitive data
 */
object SecurityUtils {
    
    /**
     * Validate GitHub token format
     */
    fun isValidGitHubToken(token: String): Boolean {
        // GitHub tokens are typically 40+ characters
        if (token.length < 40) return false
        
        // Check for valid characters (alphanumeric and underscore)
        if (!token.matches(Regex("^[a-zA-Z0-9_]+$"))) return false
        
        // GitHub token formats:
        // - Classic tokens: 'ghp_'
        // - Fine-grained tokens: 'github_pat_'
        // - OAuth tokens: 'gho_'
        // - User-to-Server tokens (GitHub Apps): 'ghu_'
        // - Server-to-Server tokens (GitHub Apps): 'ghs_'
        // - Old format: 40 hex characters
        return token.startsWith("ghp_") || 
               token.startsWith("github_pat_") ||
               token.startsWith("gho_") ||
               token.startsWith("ghu_") ||
               token.startsWith("ghs_") ||
               token.matches(Regex("^[a-f0-9]{40}$")) // Old format
    }
    
    /**
     * Mask token for logging (shows only first and last 4 characters)
     */
    fun maskToken(token: String): String {
        if (token.length <= 8) return "****"
        return "${token.take(4)}...${token.takeLast(4)}"
    }
    
    /**
     * Clear sensitive string from memory (best effort)
     */
    fun clearSensitiveString(value: String): String {
        // Return empty string and hope GC picks it up
        // Note: This is not foolproof in Kotlin, but it's a best practice
        return ""
    }
    
    /**
     * Validate that token is not expired (check age)
     * Returns true if token is less than 1 year old
     */
    fun isTokenAgeValid(tokenAgeMs: Long?): Boolean {
        if (tokenAgeMs == null) return true
        val oneYearInMs = 365L * 24 * 60 * 60 * 1000
        return tokenAgeMs < oneYearInMs
    }
    
    /**
     * Sanitize error messages to not leak tokens
     */
    fun sanitizeErrorMessage(message: String, token: String?): String {
        if (token == null) return message
        return message.replace(token, "***TOKEN***")
    }
}
