// file: com/vecinapp/ui/screen/LoginScreen.kt
package com.vecinapp.ui.screen

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.identity.Identity
import com.vecinapp.R
import com.vecinapp.auth.AuthManager
import com.vecinapp.presentation.GoogleSignInButton
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onSignInSuccess: () -> Unit,
    onProfileIncomplete: () -> Unit,
    authManager: AuthManager,
    snackbarHostState: SnackbarHostState,
    onSmsVerification: () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    /* ----- token handler ----- */
    /* ----- token handler ----- */
    fun handleToken(idToken: String) {
        isLoading = true
        scope.launch {
            // 1️⃣  autenticamos con Google
            val result = authManager.firebaseAuthWithGoogle(idToken)

            result.fold(
                onSuccess = { user ->
                    // 2️⃣  ya estamos dentro de una corrutina: podemos llamar a funciones suspend
                    val complete = authManager.isProfileComplete(user.uid)

                    if (complete) onSignInSuccess()     // → Dashboard
                    else onProfileIncomplete() // → wizard de perfil
                },
                onFailure = { e ->
                    snackbarHostState.showSnackbar("Error: ${e.message}")
                }
            )

            isLoading = false
        }
    }


    /* ----- One-Tap launcher ----- */
    val launcher =
        rememberLauncherForActivityResult(StartIntentSenderForResult()) { r ->
            if (r.resultCode == Activity.RESULT_OK) {
                val idToken = Identity
                    .getSignInClient(context)
                    .getSignInCredentialFromIntent(r.data)
                    .googleIdToken
                idToken?.let(::handleToken)
            }
        }

    /* ---------------- UI ---------------- */
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.icon_text),
                contentDescription = "Logo",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
                modifier = Modifier.padding(16.dp)
            )

            Spacer(Modifier.height(32.dp))

            Text(
                "Bienvenido a VecinApp",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Conecta con tu comunidad",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            /* ----- Google One-Tap ----- */
            GoogleSignInButton(
                onClick = {
                    scope.launch {
                        isLoading = true
                        authManager.getGoogleOneTapIntent()
                            .onSuccess { launcher.launch(it) }
                            .onFailure { e ->
                                snackbarHostState.showSnackbar("Error: ${e.message}")
                            }
                        isLoading = false
                    }
                }
            )

            Spacer(Modifier.height(16.dp))

            /* ----- SMS ----- */
            OutlinedButton(
                onClick = onSmsVerification,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Call, null, Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Ingresar con SMS")
            }

            Spacer(Modifier.height(16.dp))

            /* ----- Invitado ----- */
            Button(
                onClick = {
                    isLoading = true
                    scope.launch {
                        authManager.signInAnonymously()
                            .onSuccess {
                                isLoading = false
                                onSignInSuccess()
                            }
                            .onFailure { e ->
                                isLoading = false
                                snackbarHostState.showSnackbar("Error: ${e.message}")
                            }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Ingresar como Invitado", fontWeight = FontWeight.Medium)
            }
        }

        /* ----- Loading overlay ----- */
        AnimatedVisibility(isLoading, enter = fadeIn(), exit = fadeOut()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}
