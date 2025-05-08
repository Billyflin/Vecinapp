// ui/screen/LoginScreen.kt
package com.vecinapp.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vecinapp.ui.viewmodel.MainViewModel

@Composable
fun LoginScreen(
    onSuccess: () -> Unit,
    onProfileIncomplete: () -> Unit,
    vm: MainViewModel = hiltViewModel()
) {
    val auth by vm.authState.collectAsState()
    val phone by vm.phoneState.collectAsState()

    var number by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }

    if (auth.isLoggedIn) {
        if (auth.user?.isProfileComplete == true) onSuccess() else onProfileIncomplete()
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { /* dispara One Tap y pasa idToken */ },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Continuar con Google") }

        OutlinedTextField(
            value = number,
            onValueChange = { number = it },
            label = { Text("Número de teléfono") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        if (phone.codeSent) {
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("Código SMS") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }

        val ctx = LocalContext.current
        Button(
            onClick = {
                if (phone.codeSent)
                    vm.verifyCode(phone.verificationId ?: return@Button, code)
                else vm.startPhone(number, ctx)
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (phone.codeSent) "Verificar" else "Enviar código") }

        auth.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        phone.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
