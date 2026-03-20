package io.mohammedalaamorsi.followy.shared.ui.profile

import androidx.lifecycle.viewModelScope
import io.mohammedalaamorsi.followy.shared.data.models.GitHubUser
import io.mohammedalaamorsi.followy.shared.data.repository.GitHubRepository
import io.mohammedalaamorsi.followy.shared.domain.usecase.*
import io.mohammedalaamorsi.followy.shared.ui.base.BaseMviViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

    // Legacy support BRIDGE
    val profileState = uiState.map { state ->
        when {
            state.isLoading -> ProfileState.Loading
            state.error != null -> ProfileState.Error(state.error)
            state.user != null -> ProfileState.Success(state.user, state.isFollowing, state.isRestricted)
            else -> ProfileState.Loading
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileState.Loading)

    val isProcessing = uiState.map { it.isProcessing }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val errorMessage = uiState.map { it.error }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Bridge functions for legacy UI
    fun loadProfileLegacy(username: String, currentUsername: String, isRestricted: Boolean = false) = 
        sendIntent(ProfileIntent.LoadProfile(username, currentUsername, isRestricted))
    fun toggleFollowLegacy(username: String, currentUsername: String, onComplete: (Boolean) -> Unit = {}) = 
        sendIntent(ProfileIntent.ToggleFollow(username, currentUsername))
    fun clearErrorLegacy() = sendIntent(ProfileIntent.ClearError)
}

// Support for old ProfileState during refactor
sealed class ProfileState {
    data object Loading : ProfileState()
    data class Success(
        val user: io.mohammedalaamorsi.followy.shared.data.models.GitHubUser, 
        val isFollowing: Boolean, 
        val isRestricted: Boolean = false
    ) : ProfileState()
    data class Error(val message: String) : ProfileState()
}
