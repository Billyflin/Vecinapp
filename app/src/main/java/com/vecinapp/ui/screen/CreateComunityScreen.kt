package com.vecinapp.ui.screen

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vecinapp.community.CommunityManager
import com.vecinapp.domain.model.Community
import kotlinx.coroutines.launch

@Composable
fun CreateComunityScreen(
    onNavigateBack: () -> Unit,
    onCommunityCreated: (Community) -> Unit = {},
    isLoading: Boolean = false,
    error: String? = null
) {
    val context = LocalContext.current
    val communityManager = remember { CommunityManager(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var communityName by remember { mutableStateOf("") }
    var communityDescription by remember { mutableStateOf("") }
    var communityAddress by remember { mutableStateOf("") }
    var communityImageUrl by remember { mutableStateOf<String?>(null) }
    var isPublic by remember { mutableStateOf(true) }

    // Mostrar error si existe
    LaunchedEffect(error) {
        error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header con botón de retroceso
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver"
                )
            }

            Text(
                text = "Crear comunidad",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Imagen de la comunidad
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { /* Implementar selección de imagen */ },
                contentAlignment = Alignment.Center
            ) {
                if (communityImageUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(communityImageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Imagen de la comunidad",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Apartment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(48.dp)
                    )
                }

                // Botón para cambiar la imagen
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { /* Implementar selección de imagen */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Cambiar imagen",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Formulario
            OutlinedTextField(
                value = communityName,
                onValueChange = { communityName = it },
                label = { Text("Nombre de la comunidad") },
                placeholder = { Text("Ej: Residencial Los Pinos") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Apartment,
                        contentDescription = null
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = communityDescription,
                onValueChange = { communityDescription = it },
                label = { Text("Descripción") },
                placeholder = { Text("Describe brevemente tu comunidad") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = communityAddress,
                onValueChange = { communityAddress = it },
                label = { Text("Dirección") },
                placeholder = { Text("Dirección de la comunidad") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { /* Implementar selección en mapa */ }) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Seleccionar en mapa"
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tipo de comunidad (pública o privada)
            Text(
                text = "Tipo de comunidad",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CommunityTypeOption(
                    title = "Pública",
                    description = "Cualquiera puede encontrar y solicitar unirse",
                    isSelected = isPublic,
                    modifier = Modifier.weight(1f)
                ) {
                    isPublic = true
                }

                CommunityTypeOption(
                    title = "Privada",
                    description = "Solo se puede unir con invitación o código",
                    isSelected = !isPublic,
                    modifier = Modifier.weight(1f)
                ) {
                    isPublic = false
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botón de creación
            Button(
                onClick = {
                    // Validar campos
                    when {
                        communityName.isBlank() -> {
                            scope.launch {
                                snackbarHostState.showSnackbar("El nombre de la comunidad es obligatorio")
                            }
                        }
                        communityAddress.isBlank() -> {
                            scope.launch {
                                snackbarHostState.showSnackbar("La dirección es obligatoria")
                            }
                        }
                        else -> {
                            // Crear comunidad
                            val newCommunity = Community(
                                id = "", // Se asignará en el CommunityManager
                                name = communityName,
                                description = communityDescription.takeIf { it.isNotBlank() },
                                address = communityAddress,
                                isPublic = isPublic,
                                imageUrl = communityImageUrl,
                                members = listOf(),
                                creatorId = "", // Se asignará en el CommunityManager
                                announcements = listOf(),
                                events = listOf(),
                                proposals = listOf(),
                                createdAt = com.google.firebase.Timestamp.now()
                            )

                            // Llamar al callback para crear la comunidad
                            onCommunityCreated(newCommunity)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Crear comunidad")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Host de Snackbar para mostrar errores
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            SnackbarHost(hostState = snackbarHostState)
        }
    }
}

@Composable
private fun CommunityTypeOption(
    title: String,
    description: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

