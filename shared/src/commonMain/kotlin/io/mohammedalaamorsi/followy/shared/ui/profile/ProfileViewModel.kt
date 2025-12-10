package io.mohammedalaamorsi.followy.shared.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import io.mohammedalaamorsi.followy.shared.data.models.GitHubUser
import io.mohammedalaamorsi.followy.shared.data.repository.GitHubRepository

sealed class ProfileState {
    data object Loading : ProfileState()
    data class Success(val user: GitHubUser, val isFollowing: Boolean, val isRestricted: Boolean = false) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

class ProfileViewModel(
    private val repository: GitHubRepository
) : ViewModel() {
    
    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()
    
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    fun loadProfile(username: String, currentUsername: String, isRestricted: Boolean = false) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            
            // Get user details
            repository.getUser(username).fold(
                onSuccess = { user ->
                    // Check if current user is following this user
                    repository.isFollowing(currentUsername, username).fold(
                        onSuccess = { isFollowing ->
                            // Use the passed isRestricted value from dashboard
                            _profileState.value = ProfileState.Success(user, isFollowing, isRestricted)
                        },
                        onFailure = { error ->
                            _profileState.value = ProfileState.Error(
                                error.message ?: "Failed to check follow status"
                            )
                        }
                    )
                },
                onFailure = { error ->
                    _profileState.value = ProfileState.Error(
                        error.message ?: "Failed to load profile"
                    )
                }
            )
        }
    }
    
    fun toggleFollow(username: String, currentUsername: String, onComplete: (Boolean) -> Unit = {}) {
        val currentState = _profileState.value
        if (currentState !is ProfileState.Success) return
        
        viewModelScope.launch {
            _isProcessing.value = true
            
            val result = if (currentState.isFollowing) {
                repository.unfollowUser(username)
            } else {
                repository.followUser(username)
                }
            result.fold(
                onSuccess = { success ->
                    if (success) {
                        _profileState.value = currentState.copy(
                            isFollowing = !currentState.isFollowing
                        )
                        onComplete(true)
                    }
                    _isProcessing.value = false
                },
                onFailure = { error ->
                    _isProcessing.value = false
                    // Check if it's a 403 forbidden (user disabled following)
                    if (error.message?.contains("disabled following") == true) {
                        _profileState.value = currentState.copy(isRestricted = true)
                    }
                    _errorMessage.value = error.message ?: "Failed to update follow status"
                    onComplete(false)
                }
            )
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
}
