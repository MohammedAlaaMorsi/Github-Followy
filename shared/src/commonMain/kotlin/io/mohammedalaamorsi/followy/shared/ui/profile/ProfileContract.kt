package io.mohammedalaamorsi.followy.shared.ui.profile

import io.mohammedalaamorsi.followy.shared.data.models.GitHubUser
import io.mohammedalaamorsi.followy.shared.ui.base.UiEffect
import io.mohammedalaamorsi.followy.shared.ui.base.UiIntent
import io.mohammedalaamorsi.followy.shared.ui.base.UiState

sealed class ProfileIntent : UiIntent {
    data class LoadProfile(val username: String, val currentUsername: String, val isRestricted: Boolean = false) : ProfileIntent()
    data class ToggleFollow(val username: String, val currentUsername: String) : ProfileIntent()
    data object ClearError : ProfileIntent()
}

data class ProfileUiState(
    val isLoading: Boolean = true,
    val user: GitHubUser? = null,
    val isFollowing: Boolean = false,
    val isRestricted: Boolean = false,
    val isProcessing: Boolean = false,
    val error: String? = null
) : UiState

sealed class ProfileEffect : UiEffect {
    data class ShowSnackbar(val message: String) : ProfileEffect()
    data object NavigateBack : ProfileEffect()
}
