package com.vecinapp.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

/* …imports… */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isSenior      : Boolean,
    darkMode      : Boolean,
    dynamicColors : Boolean,
    onSeniorChange: suspend (Boolean) -> Unit,
    onDarkChange  : suspend (Boolean) -> Unit,
    onDynamicChange: suspend (Boolean) -> Unit,
    onBack        : () -> Unit,
    onLoggedOut   : () -> Unit = {},
) {
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padd ->
        Column(
            Modifier.padding(padd).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            /* ─── Apariencia ─── */
            Text("Apariencia", style = MaterialTheme.typography.titleMedium)

            // Dark mode
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Tema oscuro", Modifier.weight(1f))
                Switch(
                    checked = darkMode,
                    onCheckedChange = { scope.launch { onDarkChange(it) } }
                )
            }
            // Dynamic color
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Color dinámico (Monet)", Modifier.weight(1f))
                Switch(
                    checked = dynamicColors,
                    onCheckedChange = { scope.launch { onDynamicChange(it) } }
                )
            }

            /* ─── Modo visual ─── */
            Text("Modo visual", style = MaterialTheme.typography.titleMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick  = { scope.launch { onSeniorChange(true) } },
                    enabled  = !isSenior, modifier = Modifier.weight(1f)
                ) { Text("Senior") }

                Button(
                    onClick  = { scope.launch { onSeniorChange(false) } },
                    enabled  = isSenior,  modifier = Modifier.weight(1f)
                ) { Text("Normal") }
            }

            /* ─── Cerrar sesión ─── */
            Button(
                onClick = { Firebase.auth.signOut(); onLoggedOut() },
                colors  = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor   = MaterialTheme.colorScheme.onError
                )
            ) {
                Icon(Icons.Default.Logout, null); Spacer(Modifier.width(8.dp)); Text("Cerrar sesión")
            }
        }
    }
}
