// file: com/vecinapp/ui/screen/SettingsScreen.kt
package com.vecinapp.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vecinapp.PreferencesManager
import com.vecinapp.UserPreferences
import com.vecinapp.auth.AuthManager
import com.vecinapp.auth.UserProfile
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
    val firebaseUserState =
        authManager.currentUser.collectAsState(initial = null)
    val firebaseUser = firebaseUserState.value

// Obtener el perfil del usuario si el uid es válido
    val profileFlow = if (firebaseUser != null) {
        authManager.profile.collectAsState(initial = null)
    } else {
        remember { mutableStateOf<UserProfile?>(null) }
    }
    val profileState = profileFlow.value


    /* ------------------------------------------------ estados locales */
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var saving by remember { mutableStateOf(false) }
    var editMode by remember { mutableStateOf(false) }

    var displayName by remember(profileState?.displayName) {
        mutableStateOf(profileState?.displayName.orEmpty())
    }
    var ageText by remember(profileState?.age) {
        mutableStateOf(profileState?.age?.toString().orEmpty())
    }

    var photoUri by remember(profileState?.photoUrl) {
        mutableStateOf<Uri?>(profileState?.photoUrl)
    }

    /* ------------------------------------------------ launcher imagen */
    val pickImage = rememberLauncherForActivityResult(GetContent()) { uri: Uri? ->
        uri?.let { photoUri = it }
    }

    // Contenedor principal que ocupa todo el espacio disponible
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            /* ----------------------------- PERFIL ----------------------------- */
            ProfileSection(
                displayName = displayName,
                photoUri = photoUri,
                firebaseUser = firebaseUser,
                editMode = editMode,
                onEditModeChange = { editMode = it },
                onPickImage = { pickImage.launch("image/*") }
            )

            /* ----------------------------- FORMULARIO EDICIÓN ----------------------------- */
            AnimatedVisibility(
                visible = editMode,
                enter = fadeIn(spring(stiffness = Spring.StiffnessLow)),
                exit = fadeOut(spring(stiffness = Spring.StiffnessLow))
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Editar perfil",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = { Text("Nombre") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = ageText,
                            onValueChange = {
                                if (it.all(Char::isDigit) || it.isEmpty()) ageText = it
                            },
                            label = { Text("Edad") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val age = ageText.toIntOrNull()
                                    if (displayName.isBlank() || age == null || firebaseUser == null) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Revisa los datos ingresados")
                                        }
                                        return@Button
                                    }

                                    scope.launch {
                                        saving = true
                                        try {
                                            val remoteUrl: Uri? = photoUri?.let { uri ->
                                                if (uri.scheme == "content") {
                                                    authManager.uploadProfilePhoto(uri).getOrNull()
                                                } else uri
                                            }

                                            val res = authManager.updateUserProfile(
                                                userId = firebaseUser.uid,
                                                displayName = displayName,
                                                photoUri = remoteUrl,
                                                age = age
                                            )

                                            if (res.isSuccess) {
                                                snackbarHostState.showSnackbar("Perfil actualizado")
                                                editMode = false
                                            } else {
                                                snackbarHostState.showSnackbar(
                                                    res.exceptionOrNull()?.localizedMessage
                                                        ?: "Error al guardar"
                                                )
                                            }
                                        } finally {
                                            saving = false
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !saving
                            ) {
                                if (saving) {
                                    CircularProgressIndicator(
                                        Modifier.size(18.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Guardar")
                                }
                            }

                            FilledTonalButton(
                                onClick = { editMode = false },
                                modifier = Modifier.weight(1f),
                                enabled = !saving
                            ) {
                                Text("Cancelar")
                            }
                        }
                    }
                }
            }

            /* ---------------------- PREFERENCIAS APP ------------------------- */
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "Preferencias",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    PreferenceItem(
                        title = "Modo oscuro",
                        icon = Icons.Default.DarkMode,
                        checked = prefsState.darkMode,
                        onCheckedChange = { scope.launch { prefs.updateDarkMode(it) } }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    PreferenceItem(
                        title = "Color dinámico",
                        icon = Icons.Default.Brush,
                        checked = prefsState.dynamicColor,
                        onCheckedChange = { scope.launch { prefs.updateDynamicColor(it) } }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    PreferenceItem(
                        title = "Mostrar barra de navegación",
                        icon = Icons.Default.Navigation,
                        checked = prefsState.showNavBar,
                        onCheckedChange = { scope.launch { prefs.updateShowNavBar(it) } }
                    )
                }
            }

            /* ------------------------- INFORMACIÓN -------------------------- */
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    Text(
                        "Esta aplicación está en desarrollo. Algunas funciones pueden no estar disponibles.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(16.dp))

            /* ------------------------- SIGN-OUT ------------------------------ */
            Button(
                onClick = {
                    authManager.signOut()
                    onBack()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null
                    )
                    Text("Cerrar sesión")
                }
            }
        }
    }
}

@Composable
private fun ProfileSection(
    displayName: String,
    photoUri: Uri?,
    firebaseUser: Any?,
    editMode: Boolean,
    onEditModeChange: (Boolean) -> Unit,
    onPickImage: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photoUri ?: firebaseUser?.let { (it as? Any)?.toString() })
                        .crossfade(true)
                        .build(),
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(8.dp)
                        .clickable(onClick = onPickImage)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Cambiar foto",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Text(
                text = displayName.ifEmpty { "Usuario" },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )

            FilledTonalButton(
                onClick = { onEditModeChange(!editMode) },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(if (editMode) "Cancelar edición" else "Editar perfil")
            }
        }
    }
}

@Composable
private fun PreferenceItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}