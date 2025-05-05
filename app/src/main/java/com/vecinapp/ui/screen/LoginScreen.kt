package com.vecinapp.ui.screen

import android.app.Activity
import android.util.Log
import android.widget.Toast
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.vecinapp.R
import com.vecinapp.presentation.GoogleSignInButton

private const val TAG = "Auth"

@Composable
fun LoginScreen(
    onSignInSuccess: () -> Unit
) {
    val context = LocalContext.current

    /* ---------- One‑Tap Client & Request ---------- */
    val oneTapClient = Identity.getSignInClient(context)
    val signInRequest = BeginSignInRequest.builder()
        .setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .setFilterByAuthorizedAccounts(false)
                .build()
        )
        .setAutoSelectEnabled(false)                // ← deja ver el diálogo y los errores
        .build()

    /* ---------- Launcher for IntentSender ---------- */
    val launcher =
        rememberLauncherForActivityResult(StartIntentSenderForResult()) { result ->
            Log.d(TAG, "IntentSender resultCode=${result.resultCode}")
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                    val token = credential.googleIdToken
                    val email = credential.id
                    Log.d(TAG, "Credential received. email=$email token=${token != null}")
                    token?.let { firebaseAuthWithGoogle(it, onSignInSuccess) }
                } catch (e: Exception) {
                    Log.e(TAG, "getSignInCredentialFromIntent error: ${e.localizedMessage}")
                }
            }
        }

    /* ---------------- UI ---------------- */
    val cs = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(cs.primary, cs.secondary))),
        contentAlignment = Alignment.Center
    ) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Image(
                painterResource(R.drawable.icon_text),
                contentDescription = "Logo",
                colorFilter = ColorFilter.tint(cs.onPrimary),
                modifier = Modifier.size(320.dp)
            )

            Spacer(Modifier.height(26.dp))

            /* ---------- Google One‑Tap ---------- */
            GoogleSignInButton(
                onClick = {
                    Log.d(TAG, "BeginSignIn …")
                    oneTapClient.beginSignIn(signInRequest)
                        .addOnSuccessListener { res ->
                            Log.d(TAG, "beginSignIn success – launching IntentSender")
                            val request = IntentSenderRequest.Builder(
                                res.pendingIntent.intentSender
                            ).build()
                            launcher.launch(request)
                        }
                        .addOnFailureListener { e ->
                            val code = (e as? ApiException)?.statusCode
                            Log.e(
                                TAG,
                                "beginSignIn failed code=$code msg=${e.localizedMessage}"
                            )
                        }
                }
            )

            Spacer(Modifier.height(16.dp))

            /* ---------- Invitado ---------- */
            Button(
                onClick = {
                    Log.d(TAG, "Anonymous sign‑in …")
                    firebaseAnonymousAuth(onSignInSuccess)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = cs.onPrimary,
                    contentColor = cs.primary
                )
            ) {
                Text("Ingresar como Invitado")
            }

            Spacer(Modifier.height(16.dp))

            /* ---------- Invitado ---------- */
            Button(
                onClick = {
                    Log.d(TAG, "Anonymous sign‑in …")
                    Toast.makeText(context, "No implementado por que cobran", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = cs.onPrimary,
                    contentColor = cs.primary
                )
            ) {
                Text("Ingresar con Numero de Celular")
            }
        }
    }
}

/* ---------- Firebase helpers ---------- */

private fun firebaseAnonymousAuth(onSuccess: () -> Unit) {
    FirebaseAuth.getInstance().signInAnonymously()
        .addOnSuccessListener {
            Log.d(TAG, "Anonymous auth OK uid=${it.user?.uid}")
            onSuccess()
        }
        .addOnFailureListener { e ->
            Log.e(TAG, "Anon auth error: ${e.localizedMessage}")
        }
}

private fun firebaseAuthWithGoogle(idToken: String, onSuccess: () -> Unit) {
    val credential = GoogleAuthProvider.getCredential(idToken, null)
    FirebaseAuth.getInstance().signInWithCredential(credential)
        .addOnSuccessListener {
            Log.d(TAG, "Google auth OK uid=${it.user?.uid}")
            onSuccess()
        }
        .addOnFailureListener { e ->
            Log.e(TAG, "Google auth error: ${e.localizedMessage}")
        }
}
