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
import io.mohammedalaamorsi.followy.shared.data.models.AuthState
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
    val authState by viewModel.authState.collectAsState()
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var manualCode by remember { mutableStateOf("") }
    var showManualEntry by remember { mutableStateOf(false) }
    
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            val successState = authState as AuthState.Success
            onLoginSuccess(successState.user.login, successState.token)
        }
    }

    LaunchedEffect(Unit) {
        // Automatically check for OAuth callback code (especially for Web)
        val code = oauthHandler?.checkForCallback()
        if (code != null) {
            println("Launcher: Found OAuth callback code in URL: $code")
            viewModel.exchangeOAuthCode(
                code = code,
                clientId = configProvider.clientId,
                clientSecret = configProvider.clientSecret,
                redirectUri = configProvider.redirectUri
            )
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
                    ),
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 400.dp)
                .clip(RoundedCornerShape(24.dp)),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 12.dp,
                pressedElevation = 8.dp
            ),
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
                    textAlign = TextAlign.Center,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2f
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Login Button
                Button(
                    onClick = { 
                        if (oauthHandler?.isOAuthSupported() == true) {
                            // Start OAuth flow
                            oauthHandler.startOAuthFlow(
                                clientId = configProvider.clientId,
                                redirectUri = configProvider.redirectUri,
                                scopes = GitHubOAuthConfig.SCOPES,
                                onSuccess = { code ->
                                    viewModel.exchangeOAuthCode(
                                        code = code,
                                        clientId = configProvider.clientId,
                                        clientSecret = configProvider.clientSecret,
                                        redirectUri = configProvider.redirectUri
                                    )
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
                        .height(56.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    enabled = authState !is AuthState.Loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 3.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Login with GitHub",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
                
                // Manual Code Entry Fallback (Mostly for Desktop)
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
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        Button(
                            onClick = {
                                if (manualCode.isNotBlank()) {
                                    // Robust code extraction
                                    var extractedCode = manualCode.trim()
                                    if (extractedCode.contains("code=")) {
                                        extractedCode = extractedCode.substringAfter("code=")
                                    }
                                    if (extractedCode.contains("&")) {
                                        extractedCode = extractedCode.substringBefore("&")
                                    }
                                    
                                    println("Manual Login: Extracted code: $extractedCode")
                                    viewModel.exchangeOAuthCode(
                                        code = extractedCode,
                                        clientId = configProvider.clientId,
                                        clientSecret = configProvider.clientSecret,
                                        redirectUri = configProvider.redirectUri
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = manualCode.isNotBlank() && authState !is AuthState.Loading,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Complete Login")
                        }
                    }
                }

                TextButton(
                    onClick = { showManualEntry = !showManualEntry }
                ) {
                    Text(
                        text = if (showManualEntry) "Hide Manual Entry" else "Trouble logging in? Try Manual Entry",
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                // Security Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🔒 Secure Authentication",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "We use GitHub OAuth for secure login. Your data is never stored on our servers.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
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
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
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
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = { errorMessage = null },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Dismiss")
                            }
                        }
                    }
                }
            }
        }
    }
}
