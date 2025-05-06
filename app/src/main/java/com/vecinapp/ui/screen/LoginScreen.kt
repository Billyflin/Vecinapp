// file: com/vecinapp/ui/screen/LoginScreen.kt
package com.vecinapp.ui.screen

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthProvider
import com.vecinapp.R
import com.vecinapp.presentation.GoogleSignInButton

private sealed class LoginStep {
    object Choice : LoginStep()
    object PhoneInput : LoginStep()
    data class Otp(val verificationId: String, val token: PhoneAuthProvider.ForceResendingToken) :
        LoginStep()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onSignInSuccess: () -> Unit
) {
    var step by remember { mutableStateOf<LoginStep>(LoginStep.Choice) }
    val context = LocalContext.current

    // One-Tap Google setup
    val oneTapClient = Identity.getSignInClient(context)
    val signInRequest = BeginSignInRequest.builder()
        .setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .setFilterByAuthorizedAccounts(false)
                .build()
        )
        .setAutoSelectEnabled(false)
        .build()
    val launcher = rememberLauncherForActivityResult(StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            runCatching {
                val cred = oneTapClient.getSignInCredentialFromIntent(result.data)
                cred.googleIdToken?.let { idToken ->
                    firebaseAuthWithGoogle(idToken, onSignInSuccess)
                }
            }.onFailure { Log.e("Auth", "One-Tap error: ${it.localizedMessage}") }
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

            // 1️⃣ Elección de método
            is LoginStep.Choice -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(R.drawable.icon_only),
                    contentDescription = "Logo",
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
                    modifier = Modifier.size(200.dp)
                )
                Spacer(Modifier.height(24.dp))

                // Google
                GoogleSignInButton(onClick = {
                    oneTapClient.beginSignIn(signInRequest)
                        .addOnSuccessListener { res ->
                            launcher.launch(
                                IntentSenderRequest.Builder(res.pendingIntent.intentSender).build()
                            )
                        }
                        .addOnFailureListener {
                            Log.e("Auth", "One-Tap failed: ${it.localizedMessage}")
                        }
                })
                Spacer(Modifier.height(16.dp))

                // SMS
                OutlinedButton(
                    onClick = { step = LoginStep.PhoneInput },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Ingresar con SMS")
                }
                Spacer(Modifier.height(16.dp))

                // Invitado
                Button(
                    onClick = { firebaseAnonymousAuth(onSignInSuccess) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Ingresar como Invitado")
                }
            }

            // 2️⃣ Paso: ingresar teléfono y enviar código
            is LoginStep.PhoneInput -> {
                RegisterScreenMobile(
                    forceResendingToken = null,
                    onVerificationSent = { verificationId, token ->
                        step = LoginStep.Otp(verificationId, token)
                    }
                )
            }

            // 3️⃣ Paso: OTP y verificación
            is LoginStep.Otp -> {
                OtpVerificationScreen(
                    verificationId = s.verificationId,
                    forceResendingToken = s.token,
                    onVerified = {
                        // autenticado por SMS
                        onSignInSuccess()
                    },
                    onResend = {
                        // volvemos a pedir el código, usando el mismo token
                        step = LoginStep.PhoneInput
                    }
                )
            }
        }
    }
}

private fun firebaseAnonymousAuth(onSuccess: () -> Unit) {
    FirebaseAuth.getInstance().signInAnonymously()
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { Log.e("Auth", "Anon error: ${it.message}") }
}

private fun firebaseAuthWithGoogle(idToken: String, onSuccess: () -> Unit) {
    val cred = GoogleAuthProvider.getCredential(idToken, null)
    FirebaseAuth.getInstance().signInWithCredential(cred)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { Log.e("Auth", "GoogleAuth error: ${it.message}") }
}
