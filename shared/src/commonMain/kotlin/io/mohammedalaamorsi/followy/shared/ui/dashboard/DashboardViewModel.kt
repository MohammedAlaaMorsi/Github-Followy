package io.mohammedalaamorsi.followy.shared.ui.dashboard

import androidx.lifecycle.viewModelScope
import io.mohammedalaamorsi.followy.shared.data.repository.GitHubRepository
import io.mohammedalaamorsi.followy.shared.domain.usecase.*
import io.mohammedalaamorsi.followy.shared.ui.base.BaseMviViewModel

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
                    val updatedList = uiState.value.followersNotFollowedBack.filter { it.login != login }
                    setState(uiState.value.copy(followersNotFollowedBack = updatedList))
                    setEffect(DashboardEffect.ShowSnackbar("Followed $login"))
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
                    val updatedFollowing = uiState.value.followingNotFollowingBack.filter { it.login != login }
                    val updatedAll = uiState.value.allFollowing.filter { it.login != login }
                    setState(uiState.value.copy(
                        followingNotFollowingBack = updatedFollowing,
                        allFollowing = updatedAll
                    ))
                    setEffect(DashboardEffect.ShowSnackbar("Unfollowed $login"))
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
}
