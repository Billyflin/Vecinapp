// file: com/vecinapp/ui/screen/LoginScreen.kt
package com.vecinapp.ui.screen

import android.app.Activity
import android.util.Log
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
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
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
import com.google.firebase.auth.PhoneAuthProvider
import com.vecinapp.R
import com.vecinapp.data.repository.AuthRepository
import com.vecinapp.presentation.GoogleSignInButton
import kotlinx.coroutines.launch

private sealed class LoginStep {
    data object Choice : LoginStep()
    data object PhoneInput : LoginStep()
    data class Otp(val verificationId: String, val token: PhoneAuthProvider.ForceResendingToken) :
        LoginStep()
}

@Composable
fun LoginScreen(
    onSignInSuccess: () -> Unit,
    onProfileIncomplete: () -> Unit
) {
    var step by remember { mutableStateOf<LoginStep>(LoginStep.Choice) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Initialize AuthManager
    val authManager = remember { AuthRepository(context) }

    // One-Tap Google setup
// Inside the LoginScreen composable
    val launcher = rememberLauncherForActivityResult(StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            runCatching {
                val cred =
                    Identity.getSignInClient(context).getSignInCredentialFromIntent(result.data)
                cred.googleIdToken?.let { idToken ->
                    isLoading = true
                    scope.launch {
                        authManager.firebaseAuthWithGoogle(idToken)
                            .onSuccess { user ->
                                // Check if profile is complete
                                if (authManager.isProfileComplete(user.uid)) {
                                    // Profile is complete, go to dashboard
                                    isLoading = false
                                    onSignInSuccess()
                                } else {
                                    // Profile is incomplete, go to profile completion
                                    isLoading = false
                                    onProfileIncomplete()
                                }
                            }
                            .onFailure { e ->
                                isLoading = false
                                snackbarHostState.showSnackbar("Error: ${e.message}")
                                Log.e("Auth", "Google auth error: ${e.message}")
                            }
                    }
                }
            }.onFailure {
                isLoading = false
                scope.launch {
                    snackbarHostState.showSnackbar("Error: ${it.message}")
                }
                Log.e("Auth", "One-Tap error: ${it.localizedMessage}")
            }
        }
    }

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
        when (val s = step) {
            is LoginStep.Choice -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                // Logo
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

                // Google Sign In
                GoogleSignInButton(
                    onClick = {
                        isLoading = true
                        authManager.beginGoogleSignIn(
                            onSuccess = { intentSenderRequest ->
                                isLoading = false
                                launcher.launch(intentSenderRequest)
                            },
                            onFailure = { e ->
                                isLoading = false
                                scope.launch {
                                    snackbarHostState.showSnackbar("Error: ${e.message}")
                                }
                                Log.e("Auth", "One-Tap failed: ${e.localizedMessage}")
                            }
                        )
                    }
                )

                Spacer(Modifier.height(16.dp))

                // SMS Sign In
                OutlinedButton(
                    onClick = { step = LoginStep.PhoneInput },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Call,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.padding(horizontal = 8.dp))
                    Text("Ingresar con SMS")
                }

                Spacer(Modifier.height(16.dp))

                // Anonymous Sign In
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
                                    Log.e("Auth", "Anonymous auth error: ${e.message}")
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
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.padding(horizontal = 8.dp))
                    Text("Ingresar como Invitado", fontWeight = FontWeight.Medium)
                }
            }

            // Phone Input Step
            is LoginStep.PhoneInput -> {
                RegisterScreenMobile(
                    authManager = authManager,
                    forceResendingToken = null,
                    onVerificationSent = { verificationId, token ->
                        step = LoginStep.Otp(verificationId, token)
                    },
                    onError = { errorMessage ->
                        scope.launch {
                            snackbarHostState.showSnackbar(errorMessage)
                        }
                    },
                    onCancel = {
                        step = LoginStep.Choice
                    }
                )
            }

            // OTP Verification Step
            is LoginStep.Otp -> {
                OtpVerificationScreen(
                    authManager = authManager,
                    verificationId = s.verificationId,
                    forceResendingToken = s.token,
                    onVerified = {
                        // Successfully authenticated with SMS
                        onSignInSuccess()
                    },
                    onResend = {
                        // Go back to phone input with the same token
                        step = LoginStep.PhoneInput
                    },
                    onError = { errorMessage ->
                        scope.launch {
                            snackbarHostState.showSnackbar(errorMessage)
                        }
                    },
                    onCancel = {
                        step = LoginStep.Choice
                    }
                )
            }
        }

        // Loading indicator
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
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

        // Snackbar for errors
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) { data ->
            Snackbar(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                snackbarData = data
            )
        }
    }
}

// Note: You'll need to update RegisterScreenMobile and OtpVerificationScreen
// to accept the AuthManager parameter and use it instead of direct Firebase calls