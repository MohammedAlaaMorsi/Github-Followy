package io.mohammedalaamorsi.followy.shared.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import io.mohammedalaamorsi.followy.shared.data.models.AuthState
import io.mohammedalaamorsi.followy.shared.data.oauth.GitHubOAuthConfig
import io.mohammedalaamorsi.followy.shared.data.oauth.GitHubOAuthHandler

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    oauthHandler: GitHubOAuthHandler? = null,
    onLoginSuccess: (String, String) -> Unit
) {
    val authState by viewModel.authState.collectAsState()
    var tokenInput by remember { mutableStateOf("") }
    var showTokenInput by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            val successState = authState as AuthState.Success
            onLoginSuccess(successState.user.login, successState.token)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(16.dp)
                .widthIn(max = 400.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "GitHub Followy",
                    style = MaterialTheme.typography.headlineLarge
                )

                Text(
                    text = "Track your GitHub followers",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Main content based on state
                if (!showTokenInput) {
                    // Welcome message
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "🔐 Login with GitHub",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Click below to securely authenticate with your GitHub account. You'll be redirected to GitHub to authorize access.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { 
                            if (oauthHandler?.isOAuthSupported() == true) {
                                // Start OAuth flow
                                oauthHandler.startOAuthFlow(
                                    clientId = GitHubOAuthConfig.clientId,
                                    redirectUri = GitHubOAuthConfig.redirectUri,
                                    scopes = GitHubOAuthConfig.SCOPES,
                                    onSuccess = { code ->
                                        viewModel.exchangeOAuthCode(
                                            code = code,
                                            clientId = GitHubOAuthConfig.clientId,
                                            clientSecret = GitHubOAuthConfig.clientSecret,
                                            redirectUri = GitHubOAuthConfig.redirectUri
                                        )
                                    },
                                    onError = { error ->
                                        errorMessage = error
                                    }
                                )
                            } else {
                                // Fallback to token input
                                showTokenInput = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = authState !is AuthState.Loading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Login with GitHub",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    
                    TextButton(
                        onClick = { showTokenInput = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Use Personal Access Token instead")
                    }
                } else {
                    // Token input
                    OutlinedTextField(
                        value = tokenInput,
                        onValueChange = { tokenInput = it },
                        label = { Text("GitHub Token") },
                        placeholder = { Text("ghp_xxxxxxxxxxxx") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        enabled = authState !is AuthState.Loading,
                        supportingText = {
                            Text(
                                text = "Your token is encrypted and stored securely",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    )

                    Button(
                        onClick = { viewModel.authenticateWithToken(tokenInput) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = tokenInput.isNotBlank() && authState !is AuthState.Loading
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Login")
                    }
                    
                    TextButton(
                        onClick = { showTokenInput = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Back")
                    }
                }

                // Error message
                if (authState is AuthState.Error) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = (authState as AuthState.Error).message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                
                // OAuth error message
                errorMessage?.let { error ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall
                            )
                            TextButton(onClick = { errorMessage = null }) {
                                Text("Dismiss")
                            }
                        }
                    }
                }

                // Loading indicator
                if (authState is AuthState.Loading && !showTokenInput) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}
