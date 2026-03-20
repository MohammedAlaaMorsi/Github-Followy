package io.mohammedalaamorsi.followy.shared.ui.dashboard

import androidx.lifecycle.viewModelScope
import io.mohammedalaamorsi.followy.shared.data.repository.GitHubRepository
import io.mohammedalaamorsi.followy.shared.domain.usecase.*
import io.mohammedalaamorsi.followy.shared.ui.base.BaseMviViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val repository: GitHubRepository,
    private val getDashboardDataUseCase: GetDashboardDataUseCase,
    private val followUserUseCase: FollowUserUseCase,
    private val unfollowUserUseCase: UnfollowUserUseCase
) : BaseMviViewModel<DashboardUiState, DashboardIntent, DashboardEffect>(DashboardUiState()) {

    override suspend fun handleIntent(intent: DashboardIntent) {
        when (intent) {
            is DashboardIntent.LoadDashboard -> loadDashboard(intent.username)
            is DashboardIntent.FollowUser -> followUser(intent.login)
            is DashboardIntent.UnfollowUser -> unfollowUser(intent.login)
            is DashboardIntent.ClearError -> setState(uiState.value.copy(error = null))
        }
    }

    private suspend fun loadDashboard(username: String) {
        setState(uiState.value.copy(isLoading = true, error = null))
        
        getDashboardDataUseCase(username).fold(
            onSuccess = { data ->
                // Basic restricted check (can be refined to its own UseCase)
                val restricted = mutableSetOf<String>()
                data.followersNotFollowedBack.forEach { user ->
                    if (repository.hasPrivateActivity(user.login)) {
                        restricted.add(user.login)
                    }
                }
                
                setState(uiState.value.copy(
                    isLoading = false,
                    followersNotFollowedBack = data.followersNotFollowedBack,
                    followingNotFollowingBack = data.followingNotFollowingBack,
                    allFollowing = data.allFollowing,
                    restrictedUsers = restricted
                ))
            },
            onFailure = { error ->
                setState(uiState.value.copy(
                    isLoading = false,
                    error = error.message ?: "Failed to load dashboard"
                ))
            }
        )
    }

    private suspend fun followUser(login: String) {
        updateActionLoading(login, true)
        
        followUserUseCase(login).fold(
            onSuccess = { success ->
                if (success) {
                    // Update state to remove followed user from list
                    val updatedList = uiState.value.followersNotFollowedBack.filter { it.login != login }
                    setState(uiState.value.copy(followersNotFollowedBack = updatedList))
                }
                updateActionLoading(login, false)
            },
            onFailure = { error ->
                updateActionLoading(login, false)
                setState(uiState.value.copy(
                    error = error.message ?: "Failed to follow user",
                    restrictedUsers = uiState.value.restrictedUsers + login
                ))
            }
        )
    }

    private suspend fun unfollowUser(login: String) {
        updateActionLoading(login, true)
        
        unfollowUserUseCase(login).fold(
            onSuccess = { success ->
                if (success) {
                    val updatedList = uiState.value.followingNotFollowingBack.filter { it.login != login }
                    setState(uiState.value.copy(followingNotFollowingBack = updatedList))
                }
                updateActionLoading(login, false)
            },
            onFailure = { error ->
                updateActionLoading(login, false)
                setState(uiState.value.copy(error = error.message ?: "Failed to unfollow user"))
            }
        )
    }

    private fun updateActionLoading(login: String, isLoading: Boolean) {
        val updatedMap = uiState.value.isFollowingInProgress.toMutableMap()
        if (isLoading) updatedMap[login] = true else updatedMap.remove(login)
        setState(uiState.value.copy(isFollowingInProgress = updatedMap))
    }

    // Legacy support BRIDGE (for DashboardScreen.kt refactor)
    val dashboardState = uiState.map { state ->
        when {
            state.isLoading -> DashboardState.Loading
            state.error != null -> DashboardState.Error(state.error)
            else -> DashboardState.Success(
                followersNotFollowedBack = state.followersNotFollowedBack,
                followingNotFollowingBack = state.followingNotFollowingBack,
                allFollowing = state.allFollowing
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardState.Loading)

    val isFollowingUser = uiState.map { it.isFollowingInProgress }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val errorMessage = uiState.map { it.error }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val restrictedUsers = uiState.map { it.restrictedUsers }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    // Bridge functions for legacy UI
    fun loadDashboardLegacy(username: String) = sendIntent(DashboardIntent.LoadDashboard(username))
    fun followUserLegacy(username: String) = sendIntent(DashboardIntent.FollowUser(username))
    fun unfollowUserLegacy(username: String) = sendIntent(DashboardIntent.UnfollowUser(username))
    fun clearErrorLegacy() = sendIntent(DashboardIntent.ClearError)
}

// Support for old DashboardState during refactor
sealed class DashboardState {
    data object Loading : DashboardState()
    data class Success(
        val followersNotFollowedBack: List<io.mohammedalaamorsi.followy.shared.data.models.GitHubUser>,
        val followingNotFollowingBack: List<io.mohammedalaamorsi.followy.shared.data.models.GitHubUser>,
        val allFollowing: List<io.mohammedalaamorsi.followy.shared.data.models.GitHubUser>
    ) : DashboardState()
    data class Error(val message: String) : DashboardState()
}
