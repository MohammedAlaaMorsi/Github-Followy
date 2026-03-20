package io.mohammedalaamorsi.followy.shared.ui.profile

import io.mohammedalaamorsi.followy.shared.data.repository.GitHubRepository
import io.mohammedalaamorsi.followy.shared.domain.usecase.*
import io.mohammedalaamorsi.followy.shared.ui.base.BaseMviViewModel

class ProfileViewModel(
    private val repository: GitHubRepository,
    private val followUserUseCase: FollowUserUseCase,
    private val unfollowUserUseCase: UnfollowUserUseCase
) : BaseMviViewModel<ProfileUiState, ProfileIntent, ProfileEffect>(ProfileUiState()) {

    override suspend fun handleIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.LoadProfile -> loadProfile(intent.username, intent.currentUsername, intent.isRestricted)
            is ProfileIntent.ToggleFollow -> toggleFollow(intent.username, intent.currentUsername)
            is ProfileIntent.ClearError -> setState(uiState.value.copy(error = null))
        }
    }

    private suspend fun loadProfile(username: String, currentUsername: String, isRestricted: Boolean) {
        setState(uiState.value.copy(isLoading = true, error = null))
        
        repository.getUser(username).fold(
            onSuccess = { user ->
                repository.isFollowing(currentUsername, username).fold(
                    onSuccess = { isFollowing ->
                        setState(uiState.value.copy(
                            isLoading = false,
                            user = user,
                            isFollowing = isFollowing,
                            isRestricted = isRestricted
                        ))
                    },
                    onFailure = { error ->
                        setState(uiState.value.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to check status"
                        ))
                    }
                )
            },
            onFailure = { error ->
                setState(uiState.value.copy(
                    isLoading = false,
                    error = error.message ?: "Failed to load profile"
                ))
            }
        )
    }

    private suspend fun toggleFollow(username: String, currentUsername: String) {
        val currentState = uiState.value
        if (currentState.user == null) return
        
        setState(uiState.value.copy(isProcessing = true))
        
        val result = if (currentState.isFollowing) {
            unfollowUserUseCase(username)
        } else {
            followUserUseCase(username)
        }
        
        result.fold(
            onSuccess = { success ->
                if (success) {
                    setState(uiState.value.copy(
                        isProcessing = false,
                        isFollowing = !currentState.isFollowing
                    ))
                    setEffect(ProfileEffect.ShowSnackbar("Updated follow status"))
                } else {
                    setState(uiState.value.copy(isProcessing = false))
                }
            },
            onFailure = { error ->
                setState(uiState.value.copy(
                    isProcessing = false,
                    error = error.message ?: "Failed to update"
                ))
                if (error.message?.contains("disabled following") == true) {
                    setState(uiState.value.copy(isRestricted = true))
                }
            }
        )
    }
}
