package io.mohammedalaamorsi.followy.shared.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

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
    val dashboardState by viewModel.dashboardState.collectAsState()
    val isFollowingUser by viewModel.isFollowingUser.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val restrictedUsers by viewModel.restrictedUsers.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableStateOf(initialTab) }
    val coroutineScope = rememberCoroutineScope()
    
    // Show error message in snackbar
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Long
            )
            viewModel.clearErrorLegacy()
        }
    }
    
    // Load dashboard only once when first created, not on every recomposition
    LaunchedEffect(username) {
        // Only load if we don't have data yet
        if (dashboardState is DashboardState.Loading) {
            coroutineScope.launch {
                viewModel.loadDashboardLegacy(username)
            }
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
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = dashboardState) {
                is DashboardState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                is DashboardState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(onClick = { viewModel.loadDashboardLegacy(username) }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                
                is DashboardState.Success -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = { 
                                    Text("Follow Back\n (${state.followersNotFollowedBack.size})")
                                }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = { 
                                    Text("Not Following\n (${state.followingNotFollowingBack.size})")
                                }
                            )
                            Tab(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                text = { 
                                    Text("Following\n (${state.allFollowing.size})")
                                }
                            )
                        }
                        
                        Box(modifier = Modifier.fillMaxSize()) {
                            when (selectedTab) {
                                0 -> UserList(
                                    users = state.followersNotFollowedBack,
                                    actionText = "Follow",
                                    onAction = { user ->
                                        viewModel.followUserLegacy(user.login)
                                    },
                                    isProcessing = isFollowingUser,
                                    restrictedUsers = restrictedUsers,
                                    onUserClick = { username, isRestricted ->
                                        onUserClick(username, isRestricted, selectedTab)
                                    },
                                    onRefresh = {
                                        coroutineScope.launch {
                                            viewModel.loadDashboardLegacy(username)
                                        }
                                    }
                                )
                                1 -> UserList(
                                    users = state.followingNotFollowingBack,
                                    actionText = "Unfollow",
                                    onAction = { user ->
                                        viewModel.unfollowUserLegacy(user.login)
                                    },
                                    isProcessing = isFollowingUser,
                                    restrictedUsers = restrictedUsers,
                                    onUserClick = { username, isRestricted ->
                                        onUserClick(username, isRestricted, selectedTab)
                                    },
                                    onRefresh = {
                                        coroutineScope.launch {
                                            viewModel.loadDashboardLegacy(username)
                                        }
                                    }
                                )
                                2 -> UserList(
                                    users = state.allFollowing,
                                    actionText = "Unfollow",
                                    onAction = { user ->
                                        viewModel.unfollowUserLegacy(user.login)
                                    },
                                    isProcessing = isFollowingUser,
                                    restrictedUsers = restrictedUsers,
                                    onUserClick = { username, isRestricted ->
                                        onUserClick(username, isRestricted, selectedTab)
                                    },
                                    onRefresh = {
                                        coroutineScope.launch {
                                            viewModel.loadDashboardLegacy(username)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
