package com.vecinapp.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vecinapp.ui.viewmodel.MainViewModel

@Composable
fun ProfileCompletionScreen(
    onDone: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val auth by viewModel.authState.collectAsState()
    val saving = remember { mutableStateOf(false) }

    var name by remember { mutableStateOf(auth.user?.name ?: "") }
    var ageTxt by remember { mutableStateOf("") }
    var isSenior by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.run { spacedBy(20.dp, Alignment.CenterVertically) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Completa tu perfil", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = ageTxt,
            onValueChange = { ageTxt = it.filter { c -> c.isDigit() } },
            label = { Text("Edad") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(
                checked = isSenior,
                onCheckedChange = { isSenior = it }
            )
            Text("Soy adulto mayor")
        }

        Button(
            onClick = {
                Log.e("ProfileCompletionScreen", auth.user.toString())
                val uid = auth.user?.id ?: return@Button onDone()
                saving.value = true
                viewModel.updateUserProfile(
                    userId = uid,
                    displayName = name,
                    photoUri = null,
                    age = ageTxt.toIntOrNull() ?: 0,
                    isComplete = true
                )
                onDone()
            },
            enabled = name.isNotBlank() && ageTxt.isNotBlank() && !saving.value,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (saving.value) CircularProgressIndicator(Modifier.size(24.dp))
            else Text("Guardar")
        }
    }
}
