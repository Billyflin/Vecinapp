// file: com/vecinapp/ui/screen/SettingsScreen.kt
package com.vecinapp.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vecinapp.PreferencesManager
import com.vecinapp.UserPreferences
import com.vecinapp.auth.AuthManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    authManager: AuthManager,
    prefs: PreferencesManager,
    onBack: () -> Unit
) {
    /* ------------------------------------------------ pref-flow */
    val prefsState by prefs.preferencesFlow.collectAsState(
        initial = UserPreferences(darkMode = false, dynamicColor = true, showNavBar = true)
    )

    /* ------------------------------------------------ perfil-flow */
    val firebaseUser = authManager.currentUser.collectAsState(null).value
    val profileState by firebaseUser?.uid?.let { uid ->
        authManager.profile(uid).collectAsState(null)
    } ?: remember { mutableStateOf(null) }

    /* ------------------------------------------------ estados locales */
    val scope = rememberCoroutineScope()
    val snackbarHost = remember { SnackbarHostState() }
    var saving by remember { mutableStateOf(false) }

    var displayName by remember(profileState?.displayName) {
        mutableStateOf(profileState?.displayName.orEmpty())
    }
    var ageText by remember(profileState?.age) {
        mutableStateOf(profileState?.age?.toString().orEmpty())
    }

    /* ⚠️ especificamos explícitamente el tipo de state para evitar el
       “Type mismatch Required: Uri? Found: Any?” */
    var photoUri by remember<Uri?>(profileState?.photoUrl) {
        mutableStateOf(profileState?.photoUrl)
    }

    /* ------------------------------------------------ launcher imagen */
    val pickImage = rememberLauncherForActivityResult(GetContent()) { uri: Uri? ->
        uri?.let { photoUri = it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {

            /* ----------------------------- CUENTA ----------------------------- */
            Card(Modifier.fillMaxWidth()) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Cuenta", style = MaterialTheme.typography.titleMedium)

                    /* -------- foto -------- */
                    Box {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(photoUri ?: firebaseUser?.photoUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { pickImage.launch("image/*") },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    /* -------- nombre -------- */
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("Nombre") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    /* -------- edad -------- */
                    OutlinedTextField(
                        value = ageText,
                        onValueChange = { if (it.all(Char::isDigit)) ageText = it },
                        label = { Text("Edad") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    /* -------- botón guardar -------- */
                    Button(
                        onClick = {
                            val age = ageText.toIntOrNull()
                            if (displayName.isBlank() || age == null || firebaseUser == null) {
                                scope.launch {
                                    snackbarHost.showSnackbar("Revisa los datos ingresados")
                                }
                                return@Button
                            }

                            scope.launch {
                                saving = true
                                try {
                                    /* 1. subir foto si corresponde */
                                    val remoteUrl: Uri? = photoUri?.let { uri ->
                                        if (uri.scheme == "content") {
                                            authManager.uploadProfilePhoto(uri).getOrNull()
                                        } else uri
                                    }

                                    /* 2. actualizar perfil */
                                    val res = authManager.updateUserProfile(
                                        userId = firebaseUser.uid,
                                        displayName = displayName,
                                        photoUri = remoteUrl,
                                        age = age
                                    )

                                    if (res.isSuccess) {
                                        snackbarHost.showSnackbar("Perfil actualizado")
                                    } else {
                                        snackbarHost.showSnackbar(
                                            res.exceptionOrNull()?.localizedMessage
                                                ?: "Error al guardar"
                                        )
                                    }
                                } finally {
                                    saving = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !saving
                    ) {
                        if (saving) CircularProgressIndicator(
                            Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        ) else Text("Guardar")
                    }
                }
            }

            /* ---------------------- PREFERENCIAS APP ------------------------- */
            Card(Modifier.fillMaxWidth()) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Preferencias de la app", style = MaterialTheme.typography.titleMedium)

                    PreferenceToggle(
                        label = "Modo oscuro",
                        checked = prefsState.darkMode
                    ) { scope.launch { prefs.updateDarkMode(it) } }

                    PreferenceToggle(
                        label = "Color dinámico",
                        checked = prefsState.dynamicColor
                    ) { scope.launch { prefs.updateDynamicColor(it) } }

                    PreferenceToggle(
                        label = "Mostrar barra de navegación",
                        checked = prefsState.showNavBar
                    ) { scope.launch { prefs.updateShowNavBar(it) } }
                }
            }

            /* ------------------------- SIGN-OUT ------------------------------ */
            Button(
                onClick = {
                    authManager.signOut()
                    onBack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar sesión")
            }
        }
    }
}

/* ---------- toggle reutilizable ---------- */
@Composable
private fun PreferenceToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontWeight = FontWeight.Medium)
        Switch(checked, onCheckedChange)
    }
}
