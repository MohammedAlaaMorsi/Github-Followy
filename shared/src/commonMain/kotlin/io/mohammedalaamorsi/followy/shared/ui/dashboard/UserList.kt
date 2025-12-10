package io.mohammedalaamorsi.followy.shared.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import io.mohammedalaamorsi.followy.shared.data.models.GitHubUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserList(
    users: List<GitHubUser>,
    actionText: String,
    onAction: (GitHubUser) -> Unit,
    isProcessing: Map<String, Boolean>,
    restrictedUsers: Set<String> = emptySet(),
    onUserClick: (String, Boolean) -> Unit = { _, _ -> },
    onRefresh: () -> Unit = {}
) {
    var isRefreshing by remember { mutableStateOf(false) }
    
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
        },
        modifier = Modifier.fillMaxSize()
    ) {
        LaunchedEffect(isRefreshing) {
            if (isRefreshing) {
                onRefresh()
                delay(1000)
                isRefreshing = false
            }
        }
        
        if (users.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No users to display",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(users, key = { it.id }) { user ->
                    val isRestricted = restrictedUsers.contains(user.login)
                    UserCard(
                        user = user,
                        actionText = actionText,
                        onAction = { onAction(user) },
                        isProcessing = isProcessing[user.login] ?: false,
                        isRestricted = isRestricted,
                        onClick = { onUserClick(user.login, isRestricted) }
                    )
                }
            }
        }
    }
}

@Composable
fun UserCard(
    user: GitHubUser,
    actionText: String,
    onAction: () -> Unit,
    isProcessing: Boolean,
    isRestricted: Boolean = false,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                AsyncImage(
                    model = user.avatarUrl,
                    contentDescription = "Avatar of ${user.login}",
                    modifier = Modifier.size(48.dp)
                )
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = user.name ?: user.login,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "@${user.login}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            if (isRestricted) {
                Button(
                    onClick = { },
                    enabled = false,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("Unavailable")
                }
            } else if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(start = 8.dp)
                )
            } else {
                Button(
                    onClick = onAction,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(actionText)
                }
            }
        }
    }
}
