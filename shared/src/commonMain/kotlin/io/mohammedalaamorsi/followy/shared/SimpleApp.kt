package io.mohammedalaamorsi.followy.shared

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SimpleApp() {
    var isLoggedIn by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf("login") }
    
    if (isLoggedIn) {
        DashboardScreen(onLogout = { isLoggedIn = false })
    } else {
        LoginScreen(onLoginSuccess = { isLoggedIn = true })
    }
}

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "GitHub Followy",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Track your GitHub followers and following relationships",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onLoginSuccess,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Login with GitHub")
                }
                
                OutlinedButton(
                    onClick = onLoginSuccess,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continue with Demo")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(onLogout: () -> Unit) {
    val mockUsers = listOf(
        MockUser("user1", "User One", "https://picsum.photos/seed/user1/50/50.jpg"),
        MockUser("user2", "User Two", "https://picsum.photos/seed/user2/50/50.jpg"),
        MockUser("user3", "User Three", "https://picsum.photos/seed/user3/50/50.jpg")
    )
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("GitHub Followy") },
            actions = {
                Button(onClick = onLogout) {
                    Text("Logout")
                }
            }
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(mockUsers) { user ->
                UserCard(user = user)
            }
        }
    }
}

@Composable
fun UserCard(user: MockUser) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = user.name,
                style = MaterialTheme.typography.titleMedium
            )
            
            Button(
                onClick = { /* Handle follow/unfollow */ }
            ) {
                Text("Follow")
            }
        }
    }
}

data class MockUser(
    val login: String,
    val name: String,
    val avatarUrl: String
)
