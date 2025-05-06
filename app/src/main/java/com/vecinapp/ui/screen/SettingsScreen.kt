package com.vecinapp.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isSenior: Boolean,
    darkMode: Boolean,
    dynamicColors: Boolean,
    onSeniorChange: suspend (Boolean) -> Unit,
    onDarkChange: suspend (Boolean) -> Unit,
    onDynamicChange: suspend (Boolean) -> Unit,
    onLinkPhone: () -> Unit,       // ← callback para iniciar flujo de vinculación
    onBack: () -> Unit,
    onLoggedOut: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val user = Firebase.auth.currentUser
    val phone = user?.phoneNumber



    Column(
        Modifier
            .padding(16.dp), verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        if (isSenior) TopAppBar(title = { Text("Ajustes") }, navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
        })
        /* ─── Apariencia ─── */
        Text("Apariencia", style = MaterialTheme.typography.titleMedium)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Tema oscuro", Modifier.weight(1f))
            Switch(
                checked = darkMode, onCheckedChange = { scope.launch { onDarkChange(it) } })
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Color dinámico (Monet)", Modifier.weight(1f))
            Switch(
                checked = dynamicColors,
                onCheckedChange = { scope.launch { onDynamicChange(it) } })
        }

        /* ─── Modo visual ─── */
        Text("Modo visual", style = MaterialTheme.typography.titleMedium)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { scope.launch { onSeniorChange(true) } },
                enabled = !isSenior,
                modifier = Modifier.weight(1f)
            ) { Text("Senior") }

            Button(
                onClick = { scope.launch { onSeniorChange(false) } },
                enabled = isSenior,
                modifier = Modifier.weight(1f)
            ) { Text("Normal") }
        }

        /* ─── Cuenta / Teléfono ─── */
        Text("Cuenta", style = MaterialTheme.typography.titleMedium)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Call, contentDescription = null)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = phone ?: "No has vinculado un teléfono",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(4.dp))
                TextButton(onClick = onLinkPhone) {
                    Text(
                        if (phone != null) "Actualizar teléfono"
                        else "Vincular teléfono"
                    )
                }
            }
        }

        /* ─── Cerrar sesión ─── */
        Button(
            onClick = { Firebase.auth.signOut(); onLoggedOut() },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            )
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Cerrar sesión")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    Surface(color = Color.White) {
        SettingsScreen(
            isSenior = true,
            darkMode = false,
            dynamicColors = true,
            onSeniorChange = {},
            onDarkChange = {},
            onDynamicChange = {},
            onLinkPhone = {},
            onBack = {},
            onLoggedOut = {},
        )
    }
}

