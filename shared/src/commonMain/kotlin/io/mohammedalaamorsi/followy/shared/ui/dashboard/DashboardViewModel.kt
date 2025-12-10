package io.mohammedalaamorsi.followy.shared.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import io.mohammedalaamorsi.followy.shared.data.models.GitHubUser
import io.mohammedalaamorsi.followy.shared.data.repository.GitHubRepository

sealed class DashboardState {
    data object Loading : DashboardState()
    data class Success(
        val followersNotFollowedBack: List<GitHubUser>,
        val followingNotFollowingBack: List<GitHubUser>
    ) : DashboardState()
    data class Error(val message: String) : DashboardState()
}

class DashboardViewModel(
    private val repository: GitHubRepository
) : ViewModel() {
    
    private val _dashboardState = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()
    
    private val _isFollowingUser = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val isFollowingUser: StateFlow<Map<String, Boolean>> = _isFollowingUser.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    // Session-based tracking (cleared on app restart)
    private val _restrictedUsers = MutableStateFlow<Set<String>>(emptySet())
    val restrictedUsers: StateFlow<Set<String>> = _restrictedUsers.asStateFlow()
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    fun loadDashboard(username: String) {
        viewModelScope.launch {
            _dashboardState.value = DashboardState.Loading
            
            repository.analyzeFollowRelationships(username).fold(
                onSuccess = { (followersNotFollowedBack, followingNotFollowingBack) ->
                    // Check which users have private activity BEFORE updating UI
                    println("DashboardViewModel: Checking ${followersNotFollowedBack.size} users for private activity")
                    val restricted = mutableSetOf<String>()
                    followersNotFollowedBack.forEach { user ->
                        if (repository.hasPrivateActivity(user.login)) {
                            println("DashboardViewModel: User ${user.login} has private activity")
                            restricted.add(user.login)
                        }
                    }
                    println("DashboardViewModel: Found ${restricted.size} restricted users: $restricted")
                    _restrictedUsers.value = restricted
                    
                    // Now update the UI state after restrictions are set
                    _dashboardState.value = DashboardState.Success(
                        followersNotFollowedBack = followersNotFollowedBack,
                        followingNotFollowingBack = followingNotFollowingBack
                    )
                },
                onFailure = { error ->
                    _dashboardState.value = DashboardState.Error(
                        error.message ?: "Failed to load dashboard"
                    )
                }
            )
        }
    }
    
    fun followUser(username: String, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _isFollowingUser.value = _isFollowingUser.value + (username to true)
            
            repository.followUser(username).fold(
                onSuccess = { success ->
                    if (success) {
                        // Refresh dashboard after following
                        val currentState = _dashboardState.value
                        if (currentState is DashboardState.Success) {
                            // Remove user from followersNotFollowedBack list
                            val updatedFollowers = currentState.followersNotFollowedBack
                                .filter { it.login != username }
                            _dashboardState.value = currentState.copy(
                                followersNotFollowedBack = updatedFollowers
                            )
                        }
                    }
                    _isFollowingUser.value = _isFollowingUser.value - username
                    onComplete(success)
                },
                onFailure = { error ->
                    _isFollowingUser.value = _isFollowingUser.value - username
                    // Track users who have disabled following (session-based, cleared on restart)
                    if (error.message?.contains("disabled following") == true) {
                        _restrictedUsers.value = _restrictedUsers.value + username
                    }
                    _errorMessage.value = error.message ?: "Failed to follow user"
                    onComplete(false)
                }
            )
        }
    }
    fun unfollowUser(username: String, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _isFollowingUser.value = _isFollowingUser.value + (username to true)
            
            repository.unfollowUser(username).fold(
                onSuccess = { success ->
                    if (success) {
                        // Refresh dashboard after unfollowing
                        val currentState = _dashboardState.value
                        if (currentState is DashboardState.Success) {
                            // Remove user from followingNotFollowingBack list
                            val updatedFollowing = currentState.followingNotFollowingBack
                                .filter { it.login != username }
                            _dashboardState.value = currentState.copy(
                                followingNotFollowingBack = updatedFollowing
                            )
                        }
                    }
                    _isFollowingUser.value = _isFollowingUser.value - username
                    onComplete(success)
                },
                onFailure = { error ->
                    _isFollowingUser.value = _isFollowingUser.value - username
                    _errorMessage.value = error.message ?: "Failed to unfollow user"
                    onComplete(false)
                }
            )
        }
    }
}
