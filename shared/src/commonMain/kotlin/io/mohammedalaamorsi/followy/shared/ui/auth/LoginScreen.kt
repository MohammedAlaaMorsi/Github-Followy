package io.mohammedalaamorsi.followy.shared.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.mohammedalaamorsi.followy.shared.data.oauth.GitHubAuthConfigProvider
import io.mohammedalaamorsi.followy.shared.data.oauth.GitHubOAuthConfig
import io.mohammedalaamorsi.followy.shared.data.oauth.GitHubOAuthHandler
import org.koin.compose.koinInject

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    oauthHandler: GitHubOAuthHandler? = null,
    configProvider: GitHubAuthConfigProvider = koinInject(),
    onLoginSuccess: (String, String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var manualCode by remember { mutableStateOf("") }
    var showManualEntry by remember { mutableStateOf(false) }
    
    // Handle Navigation Side Effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AuthEffect.NavigateToDashboard -> {
                    onLoginSuccess(effect.user.login, state.token ?: "")
                }
                is AuthEffect.ShowError -> {
                    errorMessage = effect.message
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        // Automatically check for OAuth callback code (especially for Web)
        val code = oauthHandler?.checkForCallback()
        if (code != null) {
            viewModel.sendIntent(AuthIntent.ExchangeOAuthCode(
                code = code,
                clientId = configProvider.clientId,
                clientSecret = configProvider.clientSecret,
                redirectUri = configProvider.redirectUri
            ))
        }
        
        // Listen for async callbacks (Deep Links on Android, results from Desktop server)
        oauthHandler?.callbackFlow?.collect { callbackData ->
            var extractedCode = callbackData
            if (extractedCode.contains("code=")) {
                extractedCode = extractedCode.substringAfter("code=")
            }
            if (extractedCode.contains("&")) {
                extractedCode = extractedCode.substringBefore("&")
            }
            
            viewModel.sendIntent(AuthIntent.ExchangeOAuthCode(
                code = extractedCode,
                clientId = configProvider.clientId,
                clientSecret = configProvider.clientSecret,
                redirectUri = configProvider.redirectUri
            ))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.surface
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 400.dp)
                .clip(RoundedCornerShape(24.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Logo and Title
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(20.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "GH",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }
                
                Text(
                    text = "GitHub Followy",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Track your GitHub followers\nand following relationships",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                // Login Button
                Button(
                    onClick = { 
                        if (oauthHandler?.isOAuthSupported() == true) {
                            oauthHandler.startOAuthFlow(
                                clientId = configProvider.clientId,
                                redirectUri = configProvider.redirectUri,
                                scopes = GitHubOAuthConfig.SCOPES,
                                onSuccess = { code ->
                                    viewModel.sendIntent(AuthIntent.ExchangeOAuthCode(
                                        code = code,
                                        clientId = configProvider.clientId,
                                        clientSecret = configProvider.clientSecret,
                                        redirectUri = configProvider.redirectUri
                                    ))
                                },
                                onError = { error ->
                                    errorMessage = error
                                }
                            )
                        } else {
                            errorMessage = "OAuth not supported on this platform"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !state.isLoading,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(imageVector = Icons.Default.Code, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Login with GitHub")
                    }
                }
                
                // Manual Entry
                if (showManualEntry) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = manualCode,
                            onValueChange = { manualCode = it },
                            label = { Text("Enter Authorization Code") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        Button(
                            onClick = {
                                if (manualCode.isNotBlank()) {
                                    val extractedCode = manualCode.trim()
                                        .substringAfter("code=")
                                        .substringBefore("&")
                                    
                                    viewModel.sendIntent(AuthIntent.ExchangeOAuthCode(
                                        code = extractedCode,
                                        clientId = configProvider.clientId,
                                        clientSecret = configProvider.clientSecret,
                                        redirectUri = configProvider.redirectUri
                                    ))
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = manualCode.isNotBlank() && !state.isLoading
                        ) {
                            Text("Complete Login")
                        }
                    }
                }

                TextButton(onClick = { showManualEntry = !showManualEntry }) {
                    Text(if (showManualEntry) "Hide Manual Entry" else "Trouble logging in? Try Manual Entry")
                }

                // Error messages
                val finalError = state.loginError ?: errorMessage
                if (finalError != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            text = finalError,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
