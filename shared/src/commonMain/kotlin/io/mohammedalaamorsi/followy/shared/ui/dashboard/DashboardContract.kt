package io.mohammedalaamorsi.followy.shared.ui.dashboard

import io.mohammedalaamorsi.followy.shared.data.models.GitHubUser
import io.mohammedalaamorsi.followy.shared.ui.base.UiEffect
import io.mohammedalaamorsi.followy.shared.ui.base.UiIntent
import io.mohammedalaamorsi.followy.shared.ui.base.UiState

sealed class DashboardIntent : UiIntent {
    data class LoadDashboard(val username: String) : DashboardIntent()
    data class FollowUser(val login: String) : DashboardIntent()
    data class UnfollowUser(val login: String) : DashboardIntent()
    data object ClearError : DashboardIntent()
}

data class DashboardUiState(
    val isLoading: Boolean = true,
    val followersNotFollowedBack: List<GitHubUser> = emptyList(),
    val followingNotFollowingBack: List<GitHubUser> = emptyList(),
    val allFollowing: List<GitHubUser> = emptyList(),
    val isFollowingInProgress: Map<String, Boolean> = emptyMap(),
    val restrictedUsers: Set<String> = emptySet(),
    val error: String? = null
) : UiState

sealed class DashboardEffect : UiEffect {
    data class ShowSnackbar(val message: String) : DashboardEffect()
    data class NavigateToProfile(val login: String, val isRestricted: Boolean) : DashboardEffect()
}
