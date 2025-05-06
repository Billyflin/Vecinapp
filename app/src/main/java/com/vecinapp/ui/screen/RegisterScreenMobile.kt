package com.vecinapp.ui.screen

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

/**
 * Paso 1: pedir número, enviar SMS.
 * Si viene forceResendingToken != null, lo usamos para reenviar.
 */
@Composable
fun RegisterScreenMobile(
    forceResendingToken: PhoneAuthProvider.ForceResendingToken? = null,
    onVerificationSent: (verificationId: String, token: PhoneAuthProvider.ForceResendingToken) -> Unit
) {
    var phone by remember { mutableStateOf("") }
    val context = LocalContext.current
    val activity = context as Activity

    val callbacks = remember {
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // auto‐verificado
                Firebase.auth.signInWithCredential(credential)
                    .addOnFailureListener {
                        Toast.makeText(context, "Auto-login falló: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.e("PhoneAuth", "Error verificación: ${e.message}")
                Toast.makeText(context, "Error verificación: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                super.onCodeSent(verificationId, token)
                onVerificationSent(verificationId, token)
            }
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Ingresa tu número", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Teléfono (+569...)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true
        )
        Button(
            onClick = {
                val builder = PhoneAuthOptions.newBuilder(Firebase.auth)
                    .setPhoneNumber(phone.trim())
                    .setTimeout(60, TimeUnit.SECONDS)
                    .setActivity(activity)
                    .setCallbacks(callbacks)
                // si es reenvío, lo indicamos:
                forceResendingToken?.let { builder.setForceResendingToken(it) }
                PhoneAuthProvider.verifyPhoneNumber(builder.build())
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enviar código")
        }
    }
}

/**
 * Paso 2: pedir OTP y verificar.
 */
@Composable
fun OtpVerificationScreen(
    verificationId: String,
    forceResendingToken: PhoneAuthProvider.ForceResendingToken?,
    onVerified: () -> Unit,
    onResend: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Ingresa el código", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            label = { Text("Código OTP") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        Button(
            onClick = {
                val credential = PhoneAuthProvider.getCredential(verificationId, code.trim())
                Firebase.auth.signInWithCredential(credential)
                    .addOnSuccessListener { onVerified() }
                    .addOnFailureListener { e ->
                        Log.e("PhoneAuth", "OTP incorrecto: ${e.message}")
                        Toast.makeText(context, "Código inválido", Toast.LENGTH_LONG).show()
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Verificar")
        }
        Text(
            text = "Reenviar código",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(top = 8.dp)
                .clickable { onResend() }
        )
    }
}
