package io.mohammedalaamorsi.followy.shared.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    username: String,
    currentUsername: String,
    isRestricted: Boolean = false,
    onNavigateBack: () -> Unit,
    onFollowToggle: (String, Boolean) -> Unit = { _, _ -> }
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Initial Load
    LaunchedEffect(username) {
        viewModel.sendIntent(ProfileIntent.LoadProfile(username, currentUsername, isRestricted))
    }
    
    // Handle Effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ProfileEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                    onFollowToggle(username, state.isFollowing)
                }
                is ProfileEffect.NavigateBack -> onNavigateBack()
            }
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.error != null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = state.error!!, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { viewModel.sendIntent(ProfileIntent.LoadProfile(username, currentUsername)) }) {
                        Text("Retry")
                    }
                }
            }
        } else if (state.user != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AsyncImage(
                    model = state.user!!.avatarUrl,
                    contentDescription = "Avatar",
                    modifier = Modifier.size(120.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                
                Text(text = "@${state.user!!.login}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                
                state.user!!.name?.let { Text(text = it, style = MaterialTheme.typography.titleLarge) }
                
                state.user!!.bio?.let {
                    Text(text = it, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(horizontal = 16.dp))
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatItem("Followers", state.user!!.followers ?: 0)
                    StatItem("Following", state.user!!.following ?: 0)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (state.isRestricted) {
                    Button(onClick = { }, enabled = false, modifier = Modifier.fillMaxWidth(0.8f)) {
                        Text("Unavailable")
                    }
                    Text(text = "This user has disabled following", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Button(
                        onClick = { viewModel.sendIntent(ProfileIntent.ToggleFollow(username, currentUsername)) },
                        enabled = !state.isProcessing,
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        if (state.isProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Icon(if (state.isFollowing) Icons.Default.PersonRemove else Icons.Default.PersonAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (state.isFollowing) "Unfollow" else "Follow")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
