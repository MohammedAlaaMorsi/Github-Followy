package io.mohammedalaamorsi.followy.shared.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    username: String,
    initialTab: Int = 0,
    onLogout: () -> Unit,
    onSettingsClick: () -> Unit,
    onUserClick: (String, Boolean, Int) -> Unit = { _, _, _ -> }
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableStateOf(initialTab) }
    var showMenu by remember { mutableStateOf(false) }
    
    // Handle Side Effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is DashboardEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is DashboardEffect.NavigateToProfile -> {
                    onUserClick(effect.login, effect.isRestricted, selectedTab)
                }
            }
        }
    }
    
    // Initial Load
    LaunchedEffect(username) {
        if (state.followersNotFollowedBack.isEmpty() && state.followingNotFollowingBack.isEmpty()) {
            viewModel.sendIntent(DashboardIntent.LoadDashboard(username))
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("@$username") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("App Settings") },
                                onClick = { 
                                    showMenu = false
                                    onSettingsClick()
                                },
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
                            )
                            Divider()
                            DropdownMenuItem(
                                text = { Text("Logout") },
                                onClick = { 
                                    showMenu = false
                                    onLogout()
                                },
                                leadingIcon = { 
                                    Icon(
                                        imageVector = Icons.Default.Logout,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = state.error!!, color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.sendIntent(DashboardIntent.LoadDashboard(username)) }) {
                            Text("Retry")
                        }
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Follow Back\n(${state.followersNotFollowedBack.size})") }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Not Following\n(${state.followingNotFollowingBack.size})") }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("Following\n(${state.allFollowing.size})") }
                        )
                    }
                    
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (selectedTab) {
                            0 -> UserList(
                                users = state.followersNotFollowedBack,
                                actionText = "Follow",
                                onAction = { user -> viewModel.sendIntent(DashboardIntent.FollowUser(user.login)) },
                                isProcessing = state.isFollowingInProgress,
                                restrictedUsers = state.restrictedUsers,
                                onUserClick = { login, isRestricted -> onUserClick(login, isRestricted, 0) },
                                onRefresh = { viewModel.sendIntent(DashboardIntent.LoadDashboard(username)) }
                            )
                            1 -> UserList(
                                users = state.followingNotFollowingBack,
                                actionText = "Unfollow",
                                onAction = { user -> viewModel.sendIntent(DashboardIntent.UnfollowUser(user.login)) },
                                isProcessing = state.isFollowingInProgress,
                                restrictedUsers = state.restrictedUsers,
                                onUserClick = { login, isRestricted -> onUserClick(login, isRestricted, 1) },
                                onRefresh = { viewModel.sendIntent(DashboardIntent.LoadDashboard(username)) }
                            )
                            2 -> UserList(
                                users = state.allFollowing,
                                actionText = "Unfollow",
                                onAction = { user -> viewModel.sendIntent(DashboardIntent.UnfollowUser(user.login)) },
                                isProcessing = state.isFollowingInProgress,
                                restrictedUsers = state.restrictedUsers,
                                onUserClick = { login, isRestricted -> onUserClick(login, isRestricted, 2) },
                                onRefresh = { viewModel.sendIntent(DashboardIntent.LoadDashboard(username)) }
                            )
                        }
                    }
                }
            }
        }
    }
}
